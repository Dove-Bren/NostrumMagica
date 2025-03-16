package com.smanzana.nostrummagica.spell.component.shapes;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.SpellBulletEntity;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.BooleanSpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

/**
 * Projectile that slowly follows and then continues spell once it has collided.
 * This WANTS an entity to follow and doesn't go to empty positions.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class SeekingBulletShape extends SpellShape {
	
	public static final float MAX_DIST = 30f;
	
	public class SeekingBulletShapeInstance extends SpellShape.SpellShapeInstance {

		private final Level world;
		private final Vec3 pos;
		
		// Just initial parameters for setup
		private final float pitch;
		private final float yaw;
		private final boolean ignoreAllies;
		private final SpellCharacteristics characteristics;
		
		public SeekingBulletShapeInstance(ISpellState state, Level world, Vec3 pos, float pitch, float yaw, boolean ignoreAllies, SpellCharacteristics characteristics) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.ignoreAllies = ignoreAllies;
			this.characteristics = characteristics;
		}
		
		@Override
		public void spawn(LivingEntity caster) {
			final Vec3 start = this.getState().getSelf().getEyePosition(0f);
			final Vec3 dir = SeekingBulletShape.getVectorForRotation(pitch, yaw);
			LivingEntity target = FindTarget(this.getState().getSelf(), start, dir, this.ignoreAllies);
			
			// Get axis from where target is
			Direction.Axis axis = Direction.Axis.Y;
			Vec3 forwardDir = dir;
			if (target != null) {
				Vec3 vec = target.position().subtract(getState().getSelf().position());
				forwardDir = vec.normalize();
				axis = Direction.getNearest((float) vec.x, (float) vec.y, (float) vec.z).getAxis();
			}
			
			Vec3 startMotion;
			
			// For players, start with motion ortho to forward
			if (getState().getSelf() instanceof Player) {
				startMotion = forwardDir
						.yRot(30f * (NostrumMagica.rand.nextBoolean() ? 1 : -1))
						.xRot(/*-15f*/ + NostrumMagica.rand.nextFloat() * 30f);
			}
			// For non-players, fire mostly up
			else {
				startMotion = (new Vec3(0, 1, 0)).normalize()
						.yRot(360f * NostrumMagica.rand.nextFloat());
			}
			
			startMotion = startMotion.scale(.4);
			
			SpellBulletEntity bullet = new SpellBulletEntity(NostrumEntityTypes.spellBullet, this, getState().getSelf(), target, axis);
			bullet.setDeltaMovement(startMotion);
			//bullet.setVelocity(startMotion.x, startMotion.y, startMotion.z); client only :(
			
			bullet.setFilter((ent) -> {
				if (ent != null && getState().getSelf() != ent) {
					if (PetFuncs.GetOwner(ent) != null && PetFuncs.GetOwner(ent).equals(getState().getSelf())) {
						return false; // We own the target
					}
					if (PetFuncs.GetOwner(getState().getSelf()) != null && PetFuncs.GetOwner(getState().getSelf()).equals(ent)) {
						return false; // ent owns us
					}
				}
				
				return true;
			});
			
			world.addFreshEntity(bullet);
		}
		
		public void onProjectileHit(BlockPos pos) {
			// does not affect blocks
			getState().trigger(null, null);
		}
		
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(entity.blockPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null);
			}
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return this.characteristics.getElement();
		}
	}
	
	protected static final @Nullable LivingEntity FindTarget(LivingEntity self, Vec3 start, Vec3 direction, boolean ignoreAllies) {
		// Do a little more work of getting a good vector for things
		// that aren't players
		LivingEntity target;
		if (self instanceof Mob && ((Mob) self).getTarget() != null) {
			Mob ent = (Mob) self;
			target = ent.getTarget(); // We already know target
		} else {
			target = null; // Solve for target with raytrace
		}
		
		if (target == null && start != null && direction != null) {
			// Ray trace
			HitResult mop = RayTrace.raytraceApprox(self.level, self, start, direction, MAX_DIST, (ent) -> {
				if (self == ent) {
					return false;
				}
				
				if (ent != null) {
					
					if (ignoreAllies) {
						// Too strong?
						LivingEntity living = NostrumMagica.resolveLivingEntity(ent);
						if (living != null
								&& NostrumMagica.IsSameTeam(self, living)) {
							return false;
						}
						
						if (PetFuncs.GetOwner(ent) != null && PetFuncs.GetOwner(ent).equals(self)) {
							return false; // We own the target
						}
						if (PetFuncs.GetOwner(self) != null && PetFuncs.GetOwner(self).equals(ent)) {
							return false; // ent owns us
						}
					}
				}
				
				return true;
			}, .5);
			
			target = RayTrace.livingFromRaytrace(mop);
		}
		
		return target;
	}

	private static final String ID = "bullet";

	public static final SpellShapeProperty<Boolean> IGNORE_ALLIES = new BooleanSpellShapeProperty("ignore_allies");
	
	public SeekingBulletShape() {
		super(ID);
	}
	
	@Override
	protected void registerProperties() {
		super.registerProperties();
		this.baseProperties.addProperty(IGNORE_ALLIES);
	}
	
	protected boolean getIgnoresAllies(SpellShapeProperties properties) {
		return properties.getValue(IGNORE_ALLIES);
	}

	@Override
	public int getManaCost(SpellShapeProperties properties) {
		return 35;
	}

	@Override
	public SpellShapeInstance createInstance(ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties params, SpellCharacteristics characteristics) {
		return new SeekingBulletShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, getIgnoresAllies(params), characteristics);
	}

	// Copied from vanilla entity class
	public static final Vec3 getVectorForRotation(float pitch, float yaw) {
        float f = Mth.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.create();
		
		list.add(ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
		list.add(ReagentItem.CreateStack(ReagentType.SPIDER_SILK, 1));
		
		return list;
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DISPENSER, 1);
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
		return MAX_DIST;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapeProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}

	@Override
	public boolean supportsPreview(SpellShapeProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, LivingEntity entity, SpellLocation location, float pitch, float yaw, SpellShapeProperties properties, SpellCharacteristics characteristics) {
		final Vec3 start = state.getSelf().getEyePosition(0f);
		final Vec3 dir = SeekingBulletShape.getVectorForRotation(pitch, yaw);
		@Nullable LivingEntity target = FindTarget(state.getSelf(), start, dir, getIgnoresAllies(properties));
		if (target != null) {
			state.trigger(Lists.newArrayList(target), null);
			return true;
		} else {
			return false;
		}
	}

	public SpellShapeProperties makeProps(boolean affectAllies) {
		return this.getDefaultProperties().setValue(IGNORE_ALLIES, affectAllies);
	}
	
}
