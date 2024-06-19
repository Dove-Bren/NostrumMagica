package com.smanzana.nostrummagica.spell.component.shapes;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.SpellBulletEntity;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.item.ReagentItem;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell.ISpellState;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.SpellShapePartProperties;
import com.smanzana.nostrummagica.spell.preview.SpellShapePreview;
import com.smanzana.nostrummagica.util.RayTrace;
import com.smanzana.petcommand.api.PetFuncs;

import net.minecraft.block.Blocks;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

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

		private final World world;
		private final Vector3d pos;
		
		// Just initial parameters for setup
		private final float pitch;
		private final float yaw;
		private final boolean ignoreAllies;
		private final SpellCharacteristics characteristics;
		
		public SeekingBulletShapeInstance(ISpellState state, World world, Vector3d pos, float pitch, float yaw, boolean ignoreAllies, SpellCharacteristics characteristics) {
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
			final Vector3d start = this.getState().getSelf().getEyePosition(0f);
			final Vector3d dir = SeekingBulletShape.getVectorForRotation(pitch, yaw);
			LivingEntity target = FindTarget(this.getState().getSelf(), start, dir, this.ignoreAllies);
			
			// Get axis from where target is
			Direction.Axis axis = Direction.Axis.Y;
			Vector3d forwardDir = dir;
			if (target != null) {
				Vector3d vec = target.getPositionVec().subtract(getState().getSelf().getPositionVec());
				forwardDir = vec.normalize();
				axis = Direction.getFacingFromVector((float) vec.x, (float) vec.y, (float) vec.z).getAxis();
			}
			
			Vector3d startMotion;
			
			// For players, start with motion ortho to forward
			if (getState().getSelf() instanceof PlayerEntity) {
				startMotion = forwardDir
						.rotateYaw(30f * (NostrumMagica.rand.nextBoolean() ? 1 : -1))
						.rotatePitch(/*-15f*/ + NostrumMagica.rand.nextFloat() * 30f);
			}
			// For non-players, fire mostly up
			else {
				startMotion = (new Vector3d(0, 1, 0)).normalize()
						.rotateYaw(360f * NostrumMagica.rand.nextFloat());
			}
			
			startMotion = startMotion.scale(.4);
			
			SpellBulletEntity bullet = new SpellBulletEntity(NostrumEntityTypes.spellBullet, this, getState().getSelf(), target, axis);
			bullet.setMotion(startMotion);
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
			
			world.addEntity(bullet);
		}
		
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(new SpellLocation(world, pos)));
		}
		
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (null == NostrumMagica.resolveLivingEntity(entity)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList(NostrumMagica.resolveLivingEntity(entity)), null);
			}
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return this.characteristics.getElement();
		}
	}
	
	protected static final @Nullable LivingEntity FindTarget(LivingEntity self, Vector3d start, Vector3d direction, boolean ignoreAllies) {
		// Do a little more work of getting a good vector for things
		// that aren't players
		LivingEntity target;
		if (self instanceof MobEntity && ((MobEntity) self).getAttackTarget() != null) {
			MobEntity ent = (MobEntity) self;
			target = ent.getAttackTarget(); // We already know target
		} else {
			target = null; // Solve for target with raytrace
		}
		
		if (target == null && start != null && direction != null) {
			// Ray trace
			RayTraceResult mop = RayTrace.raytraceApprox(self.world, self, start, direction, MAX_DIST, (ent) -> {
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
	
	public SeekingBulletShape() {
		super(ID);
	}
	
	protected boolean getIgnoresAllies(SpellShapePartProperties properties) {
		return properties.flip;
	}

	@Override
	public int getManaCost(SpellShapePartProperties properties) {
		return 35;
	}

	@Override
	public SpellShapeInstance createInstance(ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapePartProperties params, SpellCharacteristics characteristics) {
		return new SeekingBulletShapeInstance(state, location.world, location.shooterPosition, pitch, yaw, getIgnoresAllies(params), characteristics);
	}

	// Copied from vanilla entity class
	public static final Vector3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public NonNullList<ItemStack> getReagents() {
		NonNullList<ItemStack> list = NonNullList.create();
		
		list.add(ReagentItem.CreateStack(ReagentType.MANI_DUST, 1));
		list.add(ReagentItem.CreateStack(ReagentType.SPIDER_SILK, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Seeking Bullet";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DISPENSER, 1);
	}

	@Override
	public boolean supportsBoolean() {
		return true;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public NonNullList<ItemStack> supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedBooleanName() {
		return I18n.format("modification.seeking_bullet.bool.name");
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
	@Override
	public int getWeight(SpellShapePartProperties properties) {
		return 1;
	}

	@Override
	public boolean shouldTrace(PlayerEntity player, SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public double getTraceRange(PlayerEntity player, SpellShapePartProperties params) {
		return MAX_DIST;
	}
	
	@Override
	public SpellShapeAttributes getAttributes(SpellShapePartProperties params) {
		return new SpellShapeAttributes(false, true, false);
	}

	@Override
	public boolean supportsPreview(SpellShapePartProperties params) {
		return true;
	}
	
	@Override
	public boolean addToPreview(SpellShapePreview builder, ISpellState state, SpellLocation location, float pitch, float yaw, SpellShapePartProperties properties, SpellCharacteristics characteristics) {
		final Vector3d start = state.getSelf().getEyePosition(0f);
		final Vector3d dir = SeekingBulletShape.getVectorForRotation(pitch, yaw);
		@Nullable LivingEntity target = FindTarget(state.getSelf(), start, dir, getIgnoresAllies(properties));
		if (target != null) {
			state.trigger(Lists.newArrayList(target), null);
			return true;
		} else {
			return false;
		}
	}
	
}
