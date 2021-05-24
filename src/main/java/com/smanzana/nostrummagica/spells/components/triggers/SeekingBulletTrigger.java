package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntitySpellBullet;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.RayTrace;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Projectile that slowly follows and then continues spell once it has collided.
 * This WANTS an entity to follow and doesn't go to empty positions.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class SeekingBulletTrigger extends SpellTrigger {
	
	public static final float MAX_DIST = 30f * 30f;
	
	public class SeekingBulletTriggerInstance extends SpellTrigger.SpellTriggerInstance {

		private World world;
		private Vec3d pos;
		
		// Just initial parameters for setup
		private float pitch;
		private float yaw;
		
		private EntityLivingBase target;
		
		public SeekingBulletTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3d dir;
			if (caster instanceof EntityLiving && ((EntityLiving) caster).getAttackTarget() != null) {
				EntityLiving ent = (EntityLiving) caster  ;
				target = ent.getAttackTarget(); // We already know target
				dir = null;
			} else {
				target = null; // Solve for target on main thread with raytrace
				dir = SeekingBulletTrigger.getVectorForRotation(pitch, yaw);
			}
			
			final SeekingBulletTriggerInstance self = this;
			
			caster.getServer().addScheduledTask(new Runnable() {

				@Override
				public void run() {
					if (target == null) {
						// Ray trace
						RayTraceResult mop = RayTrace.raytraceApprox(world, pos, dir, MAX_DIST, (ent) -> {
							if (getState().getSelf() == ent) {
								return false;
							}
							
							if (ent != null) {
								if (ent instanceof IEntityTameable) {
									if (getState().getSelf().getUniqueID().equals(((IEntityTameable) ent).getOwnerId())) {
										return false; // We own the target entity
									}
								}
								
								if (getState().getSelf() instanceof IEntityTameable) {
									if (ent.getUniqueID().equals(((IEntityTameable) getState().getSelf()).getOwnerId())) {
										return false; // We own the target entity
									}
								}
							}
							
							return true;
						}, .5);
						
						target = (mop.entityHit == null ? null : (mop.entityHit instanceof EntityLivingBase ? (EntityLivingBase)mop.entityHit : null));
					}
					
					// Get axis from where target is
					EnumFacing.Axis axis = EnumFacing.Axis.Y;
					Vec3d forwardDir = dir;
					if (target != null) {
						Vec3d vec = target.getPositionVector().subtract(caster.getPositionVector());
						forwardDir = vec.normalize();
						axis = EnumFacing.getFacingFromVector((float) vec.xCoord, (float) vec.yCoord, (float) vec.zCoord).getAxis();
					}
					
					// Start with motion ortho to forward
					Vec3d startMotion = forwardDir
							.rotateYaw(30f * (NostrumMagica.rand.nextBoolean() ? 1 : -1))
							.rotatePitch(-15f + NostrumMagica.rand.nextFloat() * 30f);
					startMotion = startMotion.scale(.4);
					
					EntitySpellBullet bullet = new EntitySpellBullet(self, getState().getSelf(), target, axis);
					bullet.motionX = startMotion.xCoord;
					bullet.motionY = startMotion.yCoord;
					bullet.motionZ = startMotion.zCoord;
					//bullet.setVelocity(startMotion.xCoord, startMotion.yCoord, startMotion.zCoord); client only :(
					
					bullet.setFilter((ent) -> {
						if (ent != null && getState().getSelf() != ent) {
							if (ent instanceof IEntityTameable) {
								if (getState().getSelf().getUniqueID().equals(((IEntityTameable) ent).getOwnerId())) {
									return false; // We own the target entity
								}
							}
							
							if (getState().getSelf() instanceof IEntityTameable) {
								if (ent.getUniqueID().equals(((IEntityTameable) getState().getSelf()).getOwnerId())) {
									return false; // We own the target entity
								}
							}
						}
						
						return true;
					});
					
					world.spawnEntityInWorld(bullet);
			
				}
			
			});
		}
		
		public void onProjectileHit(BlockPos pos) {
			getState().trigger(null, Lists.newArrayList(getState().getOther()), world, Lists.newArrayList(pos));
		}
		
		public void onProjectileHit(Entity entity) {
			if (entity == null) {
				onProjectileHit(new BlockPos(this.pos));
			}
			else if (!(entity instanceof EntityLivingBase)) {
				onProjectileHit(entity.getPosition());
			} else {
				getState().trigger(Lists.newArrayList((EntityLivingBase) entity), Lists.newArrayList(getState().getOther()), null, null);
			}
		}
	}

	private static final String TRIGGER_KEY = "trigger_bullet";
	private static SeekingBulletTrigger instance = null;
	
	public static SeekingBulletTrigger instance() {
		if (instance == null)
			instance = new SeekingBulletTrigger();
		
		return instance;
	}
	
	private SeekingBulletTrigger() {
		super(TRIGGER_KEY);
	}

	@Override
	public int getManaCost() {
		return 50;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		// Add direction
		pos = new Vec3d(pos.xCoord, pos.yCoord + state.getSelf().getEyeHeight(), pos.zCoord);
		return new SeekingBulletTriggerInstance(state, world, pos, pitch, yaw);
	}

	// Copied from vanilla entity class
	public static final Vec3d getVectorForRotation(float pitch, float yaw) {
        float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
    }

	@Override
	public List<ItemStack> getReagents() {
		List<ItemStack> list = new ArrayList<>(1);
		
		list.add(ReagentItem.instance().getReagent(ReagentType.MANI_DUST, 1));
		list.add(ReagentItem.instance().getReagent(ReagentType.SPIDER_SILK, 1));
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Seeking Bullet";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Blocks.DISPENSER, 1, OreDictionary.WILDCARD_VALUE);
	}

	@Override
	public boolean supportsBoolean() {
		return false;
	}

	@Override
	public float[] supportedFloats() {
		return null;
	}

	@Override
	public ItemStack[] supportedFloatCosts() {
		return null;
	}

	@Override
	public String supportedBooleanName() {
		return null;
	}

	@Override
	public String supportedFloatName() {
		return null;
	}
	
}
