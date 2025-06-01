package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.listener.PlayerListener.Event;
import com.smanzana.nostrummagica.listener.PlayerListener.IGenericListener;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

public class WaveShape extends AreaShape {
	
	public class WaveShapeInstance extends SpellShape.SpellShapeInstance implements IGenericListener {
		
		private static final float FULL_BOX_LEN = 1;

		private final Level world;
		private final Vec3 origin;
		private final Vec3 dir;
		private final Vec3 normal;
		private final float length;
		private final int duration;
		private final boolean gravity;
		protected final SpellCharacteristics characteristics;
		protected final SpellShapeProperties properties;
		
		protected int aliveTicks;
		private boolean dead;
		protected double y;
		protected int lastCacheTicks = -1;
		protected final List<AABB> lastCacheBoxes = new ArrayList<>();
		protected final Set<LivingEntity> affectedEnts = new HashSet<>();;
		protected final Set<BlockPos> affectedLocs = new HashSet<>();
		
		public WaveShapeInstance(ISpellState state, Level world, Vec3 pos, Vec3 dir, float length, boolean gravity, SpellShapeProperties properties, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.duration = (int)Math.ceil(5 * length);
			this.properties = properties;
			this.length = length;
			this.origin = pos;
			this.dir = dir.multiply(1, 0, 1).normalize(); // horizontal only
			this.characteristics = characteristics;
			this.gravity = gravity;
			
			this.normal = this.dir.cross(new Vec3(0, 1, 0)).normalize(); // the left/right of our direction
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Register timer for life and for effects
			NostrumMagica.playerListener.registerTimer(this, 0, 1); // every tick
			
			doEffect();
		}
		
		@Override
		public boolean onEvent(Event type, LivingEntity entity, Object empty) {
			// Assume it's all time
			//if (type == Event.TIME)
			
			if (dead)
				return true;
			
			aliveTicks++;
			if (aliveTicks >= duration) { // 20 seconds
				this.dead = true;
				return true;
			}
			
			tick();
			doEffect();
			
			return false;
		}
		
		protected float getLengthProg() {
			return ((float) this.aliveTicks / 5f) / (length); // 1 block length per second
		}
		
		protected List<AABB> getCurrentHitBoxes() {
			// use cache if valid
			if (lastCacheTicks == -1 || lastCacheTicks != this.aliveTicks) {
				
				// Draw a line of bounding boxes along the vector that represents the actaul progress ray of the cone.
				// AABB size grows up to FULL_BOX_LEN, and we insert multiple so that there's decent overlap between
				// boxes for a somewhat-consistent collision behavior.
				
				// figure out how big each box should be
				final float prog = getLengthProg();
				final float boxLen;
				if (prog < .1f) {
					boxLen = Mth.lerp(prog, FULL_BOX_LEN/2f, FULL_BOX_LEN);
				} else {
					boxLen = FULL_BOX_LEN;
				}
				
				// ideal number of boxes is1 + (length from origin / (boxLen / 2)), but we have ints so make boundaries be halfway between segments
				// So instead of 2 boxes at .5(boxLen), go to 2 at .25(boxLen) and 3 at .75(boxLen) instead of 1 
				final float progDist = prog * length;
				final float lenPerBox = FULL_BOX_LEN / 2;
				final int numBoxes = 1 + (int) ((progDist + lenPerBox/2) / lenPerBox);
				
				final Vec3 center = this.origin.add(dir.scale(progDist)).add(0, y, 0);
				
				lastCacheBoxes.clear();
				for (int i = 0; i < numBoxes; i++) {
					// How far left/right should we be? For 1 box we just are in the center but for 2+ we should have one on both
					// extremes and then some in the middle to equally divide the space.
					// for 1: 0
					// for 2+: -1, ..., 1
					// EX 3: -1, 0, 1          (+2/2 ea)
					// EX 4: -1, -1/3, 1/3, 1  (+2/3 ea)
					// EX 5: -1, -.5, 0, .5, 1
					final float sideScale = numBoxes == 1 ? 0f : (-1f + (i * (2f / (float)(numBoxes - 1))));
					final Vec3 boxCenter = center.add(normal.scale(sideScale * (progDist / Mth.SQRT_OF_TWO) * .9));
					lastCacheBoxes.add(AABB.ofSize(boxCenter, boxLen, 3, boxLen));
				}
				
				this.lastCacheTicks = this.aliveTicks;
			}
			return lastCacheBoxes;
			
		}
		
		protected boolean isInArea(LivingEntity entity) {
			final List<AABB> bounds = this.getCurrentHitBoxes();
			final AABB entBounds = entity.getBoundingBox();
			for (AABB bound : bounds) {
				if (entBounds.intersects(bound)) {
					return true;
				}
			}

			return false;
		}

		protected boolean isInArea(Level world, BlockPos pos) {
			final List<AABB> bounds = this.getCurrentHitBoxes();
			final AABB blockBounds = AABB.unitCubeFromLowerCorner(Vec3.atLowerCornerOf(pos));
			for (AABB bound : bounds) {
				if (blockBounds.intersects(bound)) {
					return true;
				}
			}

			return false;
		}
		
		/**
		 * Checks if ent is even eligible, whether it's in the AoE or not
		 * @param ent
		 * @return
		 */
		protected boolean canAffectEnt(LivingEntity ent) {
			return !this.affectedEnts.contains(ent);
		}
		
		protected boolean canAffectPos(BlockPos pos) {
			return !this.affectedLocs.contains(pos);
		}
		
		protected void tick() {
			if (gravity) {
				final float prog = getLengthProg();
				final float progDist = prog * length;
				Vec3 center = this.origin.add(dir.scale(progDist)).add(0, y, 0);
				BlockPos pos = new BlockPos(center);
				if (this.world.isEmptyBlock(pos)) {
					if (!world.isEmptyBlock(pos.below())) {
						y -= Mth.frac(this.origin.y + y);
					} else {
						y--;
					}
				} else {
					if (world.isEmptyBlock(pos.below())) {
						y--;
					} else {
						y++; // move up by default if either dir looks bad
					}
				}
			}
			
			// Recalc center and do collision checking
			final List<AABB> bounds = this.getCurrentHitBoxes();
			
			Set<SpellLocation> locations = new HashSet<>();
			Set<LivingEntity> ents = new HashSet<>();
			
			for (AABB bound : bounds) {
				
				if (affectsBlocks(properties)) {
					BlockPos.betweenClosedStream(bound)
						.filter(this::canAffectPos)
						.filter(p -> isInArea(world, p))
						// And now work on them
						.map(p -> new SpellLocation(world, p.immutable()))
						.forEach(locations::add);
					;
				}
				
				if (affectsEntities(properties)) {
					world.getEntitiesOfClass(LivingEntity.class, bound).stream()
						.filter(this::canAffectEnt)
						.filter(this::isInArea)
						.forEach(ents::add)
					;
				}
			}
			
			if (!locations.isEmpty()) {
				locations.stream().map(l -> l.selectedBlockPos).forEach(affectedLocs::add);
			}
			
			if (!ents.isEmpty()) {
				this.affectedEnts.addAll(ents);
			}
			
			if (!locations.isEmpty() || !ents.isEmpty()) {
				TriggerData data = new TriggerData(
						Lists.newArrayList(ents),
						Lists.newArrayList(locations)
						);
				this.trigger(data, 1f, true);
			}
		}

		protected void doEffect() {
			final float prog = getLengthProg();
			final float progDist = prog * length;
			final Vec3 center = this.origin.add(dir.scale(progDist)).add(0, y, 0);
			final float progWidth = .75f * progDist;
			
			for (int i = 0; i < (int)progDist + 1; i++) {
				final float scale = ((NostrumMagica.rand.nextFloat() * 2f) - 1f) * progWidth;
				final Vec3 particlePos = center.add(normal.scale(scale));
				
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						particlePos.x,
						particlePos.y,
						particlePos.z,
						0,
						40, 10, // lifetime + jitter
						new Vec3(0, .3, 0).add(dir.scale(.1)), new Vec3(0, .1, 0)
						).gravity(.6f).color(characteristics.getElement().getColor())
					);
			}
			
			// Bounds visualization
//			final List<AABB> bounds = this.getCurrentHitBoxes();
//			for (AABB aabb : bounds) {
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						1,
//						aabb.minX,
//						aabb.minY,
//						aabb.minZ,
//						0,
//						10, 0, // lifetime + jitter
//						new Vec3(0, .2, 0), new Vec3(0, .1, 0)
//						).color(0xFF00FFFF)
//					);
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						1,
//						aabb.maxX,
//						aabb.minY,
//						aabb.minZ,
//						0,
//						10, 0, // lifetime + jitter
//						new Vec3(0, .2, 0), new Vec3(0, .1, 0)
//						).color(0xFF00FFFF)
//					);
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						1,
//						aabb.maxX,
//						aabb.minY,
//						aabb.maxZ,
//						0,
//						10, 0, // lifetime + jitter
//						new Vec3(0, .2, 0), new Vec3(0, .1, 0)
//						).color(0xFF00FFFF)
//					);
//				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
//						1,
//						aabb.minX,
//						aabb.minY,
//						aabb.maxZ,
//						0,
//						10, 0, // lifetime + jitter
//						new Vec3(0, .2, 0), new Vec3(0, .1, 0)
//						).color(0xFF00FFFF)
//					);
//			}
			
			// Spawn a border one
			for (int i = -1; i <= 1; i+=2) {
				final float scale = i * progWidth;
				final Vec3 particlePos = center.add(normal.scale(scale));
				
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						particlePos.x,
						particlePos.y,
						particlePos.z,
						0,
						40, 10, // lifetime + jitter
						new Vec3(0, .3, 0), new Vec3(0, .1, 0)
						).gravity(.6f).color(characteristics.getElement().getColor())
					);
			}
		}
	}

	private static String ID = "wave";
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.MANI_DUST, 1),
			ReagentItem.CreateStack(ReagentType.SKY_ASH, 1),
			ReagentItem.CreateStack(ReagentType.SPIDER_SILK, 1)));
	
	public static final SpellShapeProperty<Float> LENGTH = new FloatSpellShapeProperty("length", 5f, 7f, 10f);
	public static final SpellShapeProperty<Boolean> GRAVITY = new BooleanSpellShapeProperty("gravity");
	
	protected WaveShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		baseProperties.addProperty(LENGTH).addProperty(GRAVITY, true).addProperty(SpellShapeSelector.PROPERTY, SpellShapeSelector.ENTITIES);
	}
	
	public WaveShape() {
		this(ID);
	}
	
	protected float getLength(SpellShapeProperties properties) {
		return properties.getValue(LENGTH);
	}
	
	protected boolean getGravity(SpellShapeProperties properties) {
		return properties.getValue(GRAVITY);
	}

	@Override
	public WaveShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw,
			SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final Vec3 lookDir = Vec3.directionFromRotation(pitch, yaw);
		final Vec3 lookEnd = location.shooterPosition.add(lookDir);
		final Vec3 startPos;
		HitResult result = RayTrace.raytrace(location.world, entity, location.shooterPosition, lookEnd, (e) -> false);
		if (result.getType() == HitResult.Type.MISS) {
			startPos = lookEnd;
		} else {
			startPos = result.getLocation();
		}
		return new WaveShapeInstance(state, location.world, startPos, lookDir,
				getLength(properties), getGravity(properties), properties, characteristics);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.WATER_BUCKET);
	}

	public static NonNullList<ItemStack> costsLength = null;
	public static NonNullList<ItemStack> costsGravity = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (costsLength == null) {
			costsLength = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Blocks.DIAMOND_BLOCK),
				new ItemStack(NostrumItems.crystalMedium, 1)
				);
			costsGravity = NonNullList.of(ItemStack.EMPTY,
					new ItemStack(Items.DRAGON_BREATH),
					ItemStack.EMPTY
					);
		}
		return property == LENGTH ? costsLength : 
			property == GRAVITY ? costsGravity :
				super.getPropertyItemRequirements(property);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		final float length = getLength(properties);
		return 25 + (int) (3 * length);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 2;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return false;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
//		final float radius = getRadius(properties) - .25f;
//		builder.add(new SpellShapePreviewComponent.Disk(location.hitPosition.add(0, .5, 0), (float) radius));
		return super.addToPreview(builder, state, entity, location, pitch, yaw, properties, characteristics);
	}

	public SpellShapeProperties makeProps(float length, boolean gravity) {
		return this.getDefaultProperties().setValue(LENGTH, length).setValue(GRAVITY, gravity);
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, this.affectsEntities(params), this.affectsBlocks(params));
	}

}
