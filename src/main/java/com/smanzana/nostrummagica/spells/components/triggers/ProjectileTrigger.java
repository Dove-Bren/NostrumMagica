package com.smanzana.nostrummagica.spells.components.triggers;

import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.entity.EntitySpellProjectile;
import com.smanzana.nostrummagica.entity.IEntityTameable;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.Spell.SpellState;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.Projectiles;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;

/**
 * Projectile. Does no tracking, etc. Instead, spawns a projectile entity and
 * waits for it to collide.
 * Other is always set to current self
 * @author Skyler
 *
 */
public class ProjectileTrigger extends SpellTrigger {
	
	public class ProjectileTriggerInstance extends SpellTrigger.SpellTriggerInstance {

		private World world;
		private Vec3d pos;
		private float pitch;
		private float yaw;
		private boolean atMax;
		
		public ProjectileTriggerInstance(SpellState state, World world, Vec3d pos, float pitch, float yaw, boolean atMax) {
			super(state);
			this.world = world;
			this.pos = pos;
			this.pitch = pitch;
			this.yaw = yaw;
			this.atMax = atMax;
		}
		
		@Override
		public void init(EntityLivingBase caster) {
			// Do a little more work of getting a good vector for things
			// that aren't players
			final Vec3d dir;
			if (caster instanceof EntityLiving && ((EntityLiving) caster).getAttackTarget() != null) {
				EntityLiving ent = (EntityLiving) caster  ;
				dir = ent.getAttackTarget().getPositionVector().addVector(0.0, ent.height / 2.0, 0.0)
						.subtract(caster.posX, caster.posY + caster.getEyeHeight(), caster.posZ);
			} else {
				dir = ProjectileTrigger.getVectorForRotation(pitch, yaw);
			}
			
			final ProjectileTriggerInstance self = this;
			
			caster.getServer().addScheduledTask(new Runnable() {

				@Override
				public void run() {
					EntitySpellProjectile projectile = new EntitySpellProjectile(self,
							getState().getSelf(),
							world,
							pos.xCoord, pos.yCoord, pos.zCoord,
							dir,
							5.0f, PROJECTILE_RANGE);
					
					projectile.setFilter((ent) -> {
						
						if (ent == null) {
							return false;
						}
						
						if (ent == getState().getSelf()) {
							return false;
						}
						
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
						
						if (Projectiles.getShooter(ent) == getState().getSelf()) {
							return false;
						}
						
						return true;
					});
					
					world.spawnEntityInWorld(projectile);
			
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
		
		public void onFizzle(BlockPos lastPos) {
			if (atMax)
				onProjectileHit(lastPos);
			else
				getState().triggerFail();
		}
		
		public EMagicElement getElement() {
			// Return element on next shape
			return getState().getNextElement();
		}
	}

	private static final String TRIGGER_KEY = "trigger_projectile";
	private static ProjectileTrigger instance = null;
	
	public static ProjectileTrigger instance() {
		if (instance == null)
			instance = new ProjectileTrigger();
		
		return instance;
	}
	
	private ProjectileTrigger() {
		super(TRIGGER_KEY);
	}

	private static final double PROJECTILE_RANGE = 30.0;
	
	@Override
	public int getManaCost() {
		return 30;
	}

	@Override
	public SpellTriggerInstance instance(SpellState state, World world, Vec3d pos, float pitch, float yaw, SpellPartParam params) {
		// We use param's flip to indicate whether we should continue on max range
		boolean atMax = false;
		if (params != null)
			atMax = params.flip;
		
		// Add direction
		pos = new Vec3d(pos.xCoord, pos.yCoord + state.getSelf().getEyeHeight(), pos.zCoord);
		return new ProjectileTriggerInstance(state, world, pos, pitch, yaw, atMax);
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
		
		return list;
	}

	@Override
	public String getDisplayName() {
		return "Projectile";
	}

	@Override
	public ItemStack getCraftItem() {
		return new ItemStack(Items.BOW, 1, OreDictionary.WILDCARD_VALUE);
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
