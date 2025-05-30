package com.smanzana.nostrummagica.spell.component.shapes;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SeekerSpellSaucerEntity;
import com.smanzana.nostrummagica.entity.SpellProjectileEntity.ISpellProjectileShape;
import com.smanzana.nostrummagica.entity.SpellSaucerEntity;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.IntSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeSelector;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreviewComponent;
import com.smanzana.nostrummagica.util.Projectiles;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.nostrummagica.util.TargetLocation;

import net.minecraft.core.NonNullList;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.Lazy;

/**
 * Multiple Mini-disk projectile that seeks out the target and hits multiple things in its path
 * @author Skyler
 *
 */
public class BarrageShape extends SpellShape implements ISelectableShape {

	public static class BarrageShapeInstance extends SpellShapeInstance implements ISpellProjectileShape {

		private final Level world;
		private final Vec3 pos;
		private final float pitch;
		private final float yaw;
		private final boolean hitEnts;
		private final boolean hitBlocks;
		private final int count;
		private final SpellCharacteristics characteristics;
		
		public BarrageShapeInstance(ISpellState state, Level world, Vec3 pos, float pitch, float yaw, boolean hitEnts, boolean hitBlocks, int count, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.hitEnts = hitEnts;
			this.hitBlocks = hitBlocks;
			this.count = count;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final LivingEntity self = this.getState().getSelf();
			final TargetLocation target;
			final Vec3 dir;
			if (self instanceof Mob mob && mob.getTarget() != null) {
				// target gets to be simple
				target = new TargetLocation(mob.getTarget(), true);
				dir = mob.getTarget().position().add(0.0, mob.getTarget().getBbHeight() / 2.0, 0.0)
						.subtract(self.getX(), self.getY() + self.getEyeHeight(), self.getZ());
			} else if (getState().getTargetHint() != null && this.hitEnts) {
				target = new TargetLocation(getState().getTargetHint(), true);
				dir = Projectiles.getVectorForRotation(pitch, yaw);
			} else {
				dir = Projectiles.getVectorForRotation(pitch, yaw);
				final Vec3 start = self.getEyePosition();
				HitResult trace = RayTrace.raytrace(world, self, start, dir, (float) PROJECTILE_RANGE, hitEnts ? new RayTrace.OtherLiving(self) : e -> false);
				if (trace.getType() == HitResult.Type.BLOCK && hitBlocks) {
					target = new TargetLocation(Vec3.atCenterOf(RayTrace.blockPosFromResult(trace)));
				} else if (trace.getType() == HitResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null && hitEnts) {
					target = new TargetLocation(RayTrace.livingFromRaytrace(trace), true);
				} else {
					target = new TargetLocation(start.add(dir.normalize().scale(PROJECTILE_RANGE)));
				}
			}
			
			
			final float TO_RAD = Mth.PI / 180f;
			final float degSpan = 120f;
			final float degPer = (degSpan / (count-1));
			final float degStart = -degSpan/2;
			
			// 2 -> -60, 60  (-60 start, 120 per)
			// 3 -> -60, 0, 60 (-60 start, 60 per)
			// 4 -> -60, -20, 20, 60 (-60 start, 40 per)
			
			for (int i = 0; i < count; i++) {
				final Vec3 dirMod = dir.yRot(TO_RAD * (degStart + (i * degPer))).add(0, .25, 0).normalize();
				SpellSaucerEntity projectile = new SeekerSpellSaucerEntity(this,
						world, getState().getSelf(), pos.add(dir.scale(.25)).add(0, -.25, 0), dirMod,
						.4f + NostrumMagica.rand.nextFloat() * .4f, target 
						);
				
				world.addFreshEntity(projectile);
			}
		}

		@Override
		public void onProjectileHit(SpellLocation location) {
			if (hitBlocks) {
				getState().trigger(null, Lists.newArrayList(location), .3f, true);
			}
			// else ignore and let continue
		}
		
		@Override
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new SpellLocation(world, this.pos));
			}
			else if (NostrumMagica.resolveLivingEntity(entity) == null) {
				onProjectileHit(new SpellLocation(entity.level, entity.blockPosition()));
			} else if (hitEnts) {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null, .3f, true);
			}
		}

		@Override
		public EMagicElement getElement() {
			return characteristics.getElement();
		}

		@Override
		public void onProjectileEnd(Vec3 pos) {
			getState().triggerFail(new SpellLocation(world, pos));
		}
	}
	
	private static final String ID = "barrage";
	private static final double PROJECTILE_RANGE = 50.0;
	private static final Lazy<NonNullList<ItemStack>> REAGENTS = Lazy.of(() -> NonNullList.of(ItemStack.EMPTY, ReagentItem.CreateStack(ReagentType.BLACK_PEARL, 1), ReagentItem.CreateStack(ReagentType.SKY_ASH, 1)));
	public static final SpellShapeProperty<Integer> COUNT = new IntSpellShapeProperty("count", 3, 4, 5, 6);
	
	protected BarrageShape(String key) {
		super(key);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(SpellShapeSelector.PROPERTY).addProperty(COUNT);
	}
	
	public BarrageShape() {
		this(ID);
	}
	
	protected int getBarrageCount(SpellShapeProperties params) {
		return params.getValue(COUNT);
	}
	
	@Override
	public BarrageShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(params);
		final boolean hitBlocks = affectsBlocks(params);
		final int count = getBarrageCount(params);
		return new BarrageShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, hitEnts, hitBlocks, count, characteristics);
	}
	
	@Override
	public int getManaCost(SpellShapeProperties properties) {
		final int extraCount = getBarrageCount(properties) - COUNT.getDefault();
		return 40 + 5 * (extraCount);
	}

	@Override
	public NonNullList<ItemStack> getReagents() {
		return REAGENTS.get();
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.SPECTRAL_ARROW, 1);
	}
	
	private static NonNullList<ItemStack> COUNT_COSTS = null;
	@Override
	public <T> NonNullList<ItemStack> getPropertyItemRequirements(SpellShapeProperty<T> property) {
		if (COUNT_COSTS == null) {
			COUNT_COSTS = NonNullList.of(ItemStack.EMPTY,
				ItemStack.EMPTY,
				new ItemStack(Items.GLOWSTONE_DUST),
				new ItemStack(Items.SPECTRAL_ARROW),
				new ItemStack(Items.DRAGON_BREATH)
				);
		}
		return property == COUNT ? COUNT_COSTS
				: super.getPropertyItemRequirements(property);
	}

	@Override
	public int getWeight(SpellShapeProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(Player player, SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(Player player, SpellShapeProperties params) {
		return PROJECTILE_RANGE;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(true, true, true);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final boolean hitEnts = affectsEntities(properties);
		final boolean hitBlocks = affectsBlocks(properties);
		final Vec3 dir;
		final LivingEntity self = state.getSelf();
		if (self instanceof Mob && ((Mob) self).getTarget() != null) {
			Mob ent = (Mob) self  ;
			dir = ent.getTarget().position().add(0.0, ent.getBbHeight() / 2.0, 0.0)
					.subtract(self.getX(), self.getY() + self.getEyeHeight(), self.getZ());
		} else {
			dir = Projectiles.getVectorForRotation(pitch, yaw);
		}
		
		HitResult trace = RayTrace.raytrace(location.world, state.getSelf(), location.shooterPosition, dir, (float) PROJECTILE_RANGE, hitEnts ? new RayTrace.OtherLiving(state.getSelf()) : e -> false);
		if (trace.getType() == HitResult.Type.BLOCK && hitBlocks) {
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), Vec3.atCenterOf(RayTrace.blockPosFromResult(trace))));
			state.trigger(null, Lists.newArrayList(new SpellLocation(location.world, trace)));
			return true;
		} else if (trace.getType() == HitResult.Type.ENTITY && RayTrace.livingFromRaytrace(trace) != null && hitEnts) {
			final LivingEntity living = RayTrace.livingFromRaytrace(trace);
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), living.position().add(0, living.getBbHeight() / 2, 0)));
			state.trigger(Lists.newArrayList(living), null);
			return true;
		} else {
			final Vec3 dest = location.shooterPosition.add(dir.normalize().scale(PROJECTILE_RANGE));
			builder.add(new SpellShapePreviewComponent.Line(location.shooterPosition.add(0, -.25, 0), new Vec3((int) dest.x() + .5, (int) dest.y() + .5, (int) dest.z() + .5)));
			return true;
		}
	}
}
