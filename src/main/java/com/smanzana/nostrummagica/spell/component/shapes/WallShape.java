package com.smanzana.nostrummagica.spell.component.shapes;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.FloatSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.core.NonNullList;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class WallShape extends AreaShape {
	
	private static class WallFacing {
		// Is this wall facing north/south? False means east/west.
		public final boolean northSouth;
		// Is this wall vertical? If not, it's lying down like when the user is facing up/down.
		public final boolean vertical;
		// Is this wall based off a grounded block? If not, it should be centered around the placement area.
		public final boolean grounded;
		public WallFacing(boolean northSouth, boolean vertical, boolean grounded) {
			this.northSouth = northSouth;
			this.vertical = vertical;
			this.grounded = grounded;
		}
	}
	private static final int WALL_HEIGHT = 3;
	
	public class WallShapeInstance extends AreaShape.AreaShapeInstance {
		
		private static final int TICK_RATE = 5;
		private static final int NUM_TICKS = (20 * 20) / TICK_RATE; // 20 seconds

		private final WallFacing facing;
		private final float radius;
		private final boolean lingering;
		private final SpellCharacteristics characteristics;
		private final SpellLocation instantLocation;
		
		private BoundingBox bounds;
		
		public WallShapeInstance(ISpellState state, SpellLocation location, WallFacing facing, float radius, boolean lingering, SpellShapeProperties properties, SpellCharacteristics characteristics) {
			super(state, location.world, new Vec3(location.hitBlockPos.getX() + .5, location.hitBlockPos.getY(), location.hitBlockPos.getZ() + .5), TICK_RATE, NUM_TICKS, 2*radius, true, affectsEntities(properties), affectsBlocks(properties), characteristics);
			this.radius = radius;
			this.facing = facing;
			this.characteristics = characteristics;
			this.lingering = lingering;
			this.instantLocation = location;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Figure out bounds
			this.bounds = MakeBounds(new BlockPos(this.pos), facing, radius);
			
			// If lingering, do regular Area spawning registering listeners etc.
			if (lingering) {
				super.spawn(caster); // Inits listening and stuff
			} else {
				// Figure out all affected now
				doInstantCheck();
			}
		}
		
		protected void doInstantCheck() {
			final ISpellState state = this.getState();
			
			if (!state.isPreview()) {
				//this.spawnShapeEffect(state.getCaster(), null, world, instantLocation, param, characteristics);
			}
			
			List<LivingEntity> ret = new ArrayList<>();
			
			if (this.affectsEnts) {
				final Vec3 center = new Vec3(instantLocation.hitBlockPos.getX() + .5, instantLocation.hitBlockPos.getY(), instantLocation.hitBlockPos.getZ() + .5);
				final float radiusEnts = this.radius + .5f;
				
				for (Entity entity : world.getEntities(null, 
						new AABB(center.x() - radiusEnts,
								center.y() - radiusEnts,
								center.z() - radiusEnts,
								center.x() + radiusEnts,
								center.y() + radiusEnts,
								center.z() + radiusEnts))) {
					LivingEntity living = NostrumMagica.resolveLivingEntity(entity);
					if (living != null) {
						if (this.isInArea(living)) {
							ret.add(living);
						}
					}
				}
			}
			
			List<SpellLocation> positions = new ArrayList<>();
			if (this.affectsGround) {
				for (int x = bounds.minX(); x <= bounds.maxX(); x++)
				for (int y = bounds.minY(); y <= bounds.maxY(); y++)
				for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
					positions.add(new SpellLocation(world, new BlockPos(x, y, z)));
				}
			}
			
			state.trigger(ret, positions, 1f, true);
		}
		
		@Override
		protected boolean isInArea(LivingEntity entity) {
			final float hRadius = entity.getBbWidth() / 2;
			final double entMinX = entity.getX() - hRadius;
			final double entMaxX = entity.getX() + hRadius;
			final double entMinZ = entity.getZ() - hRadius;
			final double entMaxZ = entity.getZ() + hRadius;
			final double entMinY = entity.getY();
			final double entMaxY = entity.getY() + entity.getBbHeight();
			return entMinX < (bounds.maxX()+1) && entMaxX > bounds.minX()
					&& entMinY < (bounds.maxY()+1) && entMaxY >= bounds.minY()
					&& entMinZ < (bounds.maxZ()+1) && entMaxZ > bounds.minZ()
					;
		}

		@Override
		protected boolean isInArea(Level world, BlockPos pos) {
			return bounds.isInside(pos);
		}

		@Override
		protected void doEffect() {
			final double diffX = (bounds.maxX()+1) - bounds.minX();
			final double diffZ = (bounds.maxZ()+1) - bounds.minZ();
			final double diffY = (bounds.maxY()+1) - bounds.minY();
			
			final double yVel = (diffY / (30f));
			for (int i = 0; i < radius/2 + 1; i++) {
				NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
						1,
						bounds.minX() + NostrumMagica.rand.nextFloat() * diffX,
						bounds.minY(),
						bounds.minZ() + NostrumMagica.rand.nextFloat() * diffZ,
						0, // pos + posjitter
						40, 10, // lifetime + jitter
						new Vec3(0, yVel, 0), null
						).color(characteristics.getElement().getColor()));
			}
			
			if (NostrumMagica.rand.nextBoolean()) {
				// corners
				
				for (int x : new int[] {bounds.minX(), bounds.maxX() + 1})
				for (int z : new int[] {bounds.minZ(), bounds.maxZ() + 1})
				{
					NostrumParticles.GLOW_ORB.spawn(world, new SpawnParams(
							1,
							x,
							bounds.minY(),
							z,
							0, // pos + posjitter
							40, 10, // lifetime + jitter
							new Vec3(0, yVel, 0), null
							).color(characteristics.getElement().getColor()));
				}
			}
		}
	}
	
	protected static final BoundingBox MakeBounds(BlockPos start, WallFacing facing, float radius) {
		final int minBlockX;
		final int maxBlockX;
		final int minBlockY;
		final int maxBlockY;
		final int minBlockZ;
		final int maxBlockZ;
		
		if (facing.vertical) {
			if (facing.northSouth) {
				minBlockX = start.getX();
				maxBlockX = start.getX();
				minBlockZ = (int) Math.floor(start.getZ() - radius);
				maxBlockZ = (int) Math.floor(start.getZ() + radius);
			} else {
				minBlockX = (int) Math.floor(start.getX() - radius);
				maxBlockX = (int) Math.floor(start.getX() + radius);
				minBlockZ = start.getZ();
				maxBlockZ = start.getZ();
			}
			
			if (facing.grounded) {
				minBlockY = start.getY();
				maxBlockY = start.getY() + (WALL_HEIGHT-1);
			} else {
				minBlockY = start.getY() - ((WALL_HEIGHT-1)/2);
				maxBlockY = start.getY() + ((WALL_HEIGHT-1)/2);
			}
		} else {
			if (facing.northSouth) {
				// use x as y
				minBlockX = start.getX() - ((WALL_HEIGHT-1)/2);
				maxBlockX = start.getX() + ((WALL_HEIGHT-1)/2);
				minBlockZ = (int) Math.floor(start.getZ() - radius);
				maxBlockZ = (int) Math.floor(start.getZ() + radius);
				minBlockY = start.getY();
				maxBlockY = start.getY();
			} else {
				// use z as y
				// range on x
				// y is just 1
				minBlockX = (int) Math.floor(start.getX() - radius);
				maxBlockX = (int) Math.floor(start.getX() + radius);
				minBlockZ = start.getZ() - ((WALL_HEIGHT-1)/2);
				maxBlockZ = start.getZ() + ((WALL_HEIGHT-1)/2);
				minBlockY = start.getY();
				maxBlockY = start.getY();
			}
		}
		
		return new BoundingBox(minBlockX, minBlockY, minBlockZ,
				maxBlockX, maxBlockY, maxBlockZ);
	}
	
	protected static final WallFacing MakeFacing(LivingEntity caster, Vec3 pos, float pitch, float yaw, boolean grounded) {
		// Get N/S or E/W from target positions
		final double dz = Math.abs(caster.getZ() - pos.z);
		final double dx = Math.abs(caster.getX() - pos.x);
		final boolean northsouth = dz < dx;
		
		// Get vertical based on target positions as well.
		// Can't use pitch as it may be several shapes in
		final double casterY = (caster.getY() + caster.getEyeHeight());
		final double dy = Math.abs(casterY - pos.y);
		final double dh = Math.sqrt(dz * dz + dx * dx);
		// We want to require a steeper angle when aiming down
		final boolean vertical;
		if (casterY <= pos.y) {
			vertical = dy < dh;
		} else {
			vertical = dy/4 < dh;
		}
		//System.out.println("dy: " + dy + "  dh: " + dh + " casterY: " + casterY + "   => " + (vertical ? "vertical" : "not"));
		
		return new WallFacing(northsouth, vertical, grounded);
	}

	private static final String ID = "wall";
	
	public static final SpellShapeProperty<Boolean> LINGER = new BooleanSpellShapeProperty("linger");
	public static final SpellShapeProperty<Float> RADIUS = new FloatSpellShapeProperty("radius", 1f, 2f, 3f, 0f);
	
	public WallShape() {
		super(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(LINGER).addProperty(RADIUS).addProperty(SpellShapeSelector.PROPERTY);
	}
	
	@Override
	public SpellShapeProperties getDefaultProperties() {
		// Wall wants to, by default, affect blocks
		return super.getDefaultProperties().setValue(SpellShapeSelector.PROPERTY, SpellShapeSelector.BLOCKS);
	}
	
	protected boolean isLingering(SpellShapeProperties properties) {
		return properties.getValue(LINGER);
	}
	
	protected float wallRadius(SpellShapeProperties properties) {
		return properties.getValue(RADIUS);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		final boolean isLingering = this.isLingering(properties);
		return isLingering ? 50 : 30;
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return NonNullList.of(ItemStack.EMPTY,
				ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1),
				ReagentItem.CreateStack(ReagentType.MANDRAKE_ROOT, 1));
	}

	@Override
	public WallShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw,
			SpellShapeProperties params, SpellCharacteristics characteristics) {
		// Determine facing based on actual hit position, but use selected pos (where we're looking) to determine if it's grounded
		WallFacing facing = MakeFacing(state.getCaster(), location.hitPosition, pitch, yaw, !location.world.isEmptyBlock(location.selectedBlockPos) && location.world.isEmptyBlock(location.selectedBlockPos.above()));
		final boolean isLingering = isLingering(params);
		final float radius = wallRadius(params);
		
		return new WallShapeInstance(state, location,
				facing, radius, isLingering, params, characteristics);
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.GLASS);
	}

	private static NonNullList<ItemStack> RADIUS_COSTS = null;
	private static NonNullList<ItemStack> LINGER_COSTS = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (RADIUS_COSTS == null) {
			RADIUS_COSTS = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.DIAMOND),
				new ItemStack(Items.EMERALD),
				new ItemStack(Blocks.GLASS)
				);
			LINGER_COSTS = NonNullList.of(ItemStack.EMPTY,
					ItemStack.EMPTY,
					new ItemStack(Items.DRAGON_BREATH)
					);
		}
		return property == RADIUS ? RADIUS_COSTS
				: property == LINGER ? LINGER_COSTS
				: super.getPropertyItemRequirements(property);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		final boolean isLingering = this.isLingering(properties);
		return isLingering ? 2 : 1;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return false;
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final float radius = wallRadius(properties);
		
		// Determine facing based on actual hit position, but use hitPos (where we'll actually place it) to determine if it's grounded
		WallFacing facing = MakeFacing(state.getCaster(), location.hitPosition, pitch, yaw, !location.world.isEmptyBlock(location.selectedBlockPos) && location.world.isEmptyBlock(location.selectedBlockPos.above()));
		BoundingBox bounds = MakeBounds(location.hitBlockPos, facing, radius);
		
		// Do block preview if affecting blocks at all
		if (this.affectsBlocks(properties)) {
			List<SpellLocation> positions = new ArrayList<>();
			for (int x = bounds.minX(); x <= bounds.maxX(); x++)
			for (int y = bounds.minY(); y <= bounds.maxY(); y++)
			for (int z = bounds.minZ(); z <= bounds.maxZ(); z++) {
				positions.add(new SpellLocation(location.world, new BlockPos(x, y, z)));
			}
			state.trigger(null, positions);
		} else {
			// Else do region box shape
			builder.add(new SpellShapePreviewComponent.Box(bounds));
		}
		
		return true;
	}
}
