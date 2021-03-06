package com.smanzana.nostrummagica.entity.tasks;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;
import com.smanzana.nostrummagica.entity.dragon.EntityDragonFlying;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.EntityAIAttackMelee;

public class DragonMeleeAttackTask extends EntityAIAttackMelee {
	
	private static final int TOTAL_STALL = 20 * 5;
	
	protected int biteTick;
	protected int stallTicks;
	protected double reachSQR;
	
	public DragonMeleeAttackTask(EntityDragon dragon, double speedIn, boolean longMemoryIn) {
		this(dragon, speedIn, longMemoryIn, 30.0);
	}
	
	public DragonMeleeAttackTask(EntityDragon dragon, double speedIn, boolean longMemoryIn, double reachSQR) {
		super(dragon, speedIn, longMemoryIn);
		
		biteTick = 0;
		this.reachSQR = reachSQR;
	}
	
	@Override
	public boolean shouldExecute() {
		this.biteTick = Math.max(0, this.biteTick - 1);
		boolean flying = false;
		
		if (this.attacker instanceof EntityDragonFlying) {
			flying = ((EntityDragonFlying) this.attacker).isFlying();
		}
		
		if (flying && biteTick > 0) {
			return false;
		}
		
		return super.shouldExecute();
	}
	
	@Override
	public boolean continueExecuting() {
		boolean ret = super.continueExecuting();
		this.biteTick = Math.max(0, this.biteTick - 1);
		
		if (ret) {
			
			boolean flying = false;
			
			if (this.attacker instanceof EntityDragonFlying) {
				flying = ((EntityDragonFlying) this.attacker).isFlying();
			}
			
			if (flying && biteTick > 0) {
				ret = false;
			} else {
				if (this.attacker.getNavigator().noPath()) {
					stallTicks++;
					if (stallTicks > TOTAL_STALL) {
						ret = false;
					}
				} else {
					stallTicks = 0;
				}
			}
		}
		
		return ret;
	}

	@Override
	public void startExecuting() {
		super.startExecuting();
		stallTicks = 0;
	}
	
	@Override
	public void updateTask() {
		// Can't call super: Want to adjust tactics based on whether we're flying. >.<
		boolean flying = false;
		if (this.attacker instanceof EntityDragonFlying) {
			flying = ((EntityDragonFlying) this.attacker).isFlying();
		}
		
		//this.biteTick = Math.max(0, this.biteTick - 1);
		
		if (flying) {
			if (this.biteTick != 0) {
				// If flying, don't dart or attack the enemy until we can BITE
				return;
			}
		}
		
		super.updateTask();
	}
	
	@Override
	public boolean isInterruptible() {
		return true;
	}

	@Override
	protected double getAttackReachSqr(EntityLivingBase attackTarget) {
		return reachSQR;
	}
	
	@Override
	protected void checkAndPerformAttack(EntityLivingBase entity, double dist) {
		double reach = this.getAttackReachSqr(entity);
		boolean flying = false;
		
		if (this.attacker instanceof EntityDragonFlying) {
			flying = ((EntityDragonFlying) this.attacker).isFlying();
		}
		

		if (dist <= reach) {
			EntityDragon dragon = (EntityDragon) this.attacker;
			boolean attacked = false;
			if (this.biteTick <= 24) {
				// Bites we actually move closer!
				if (dist < 20.0) {
					dragon.bite(entity);
					this.biteTick = dragon.getRNG().nextInt(20 * 3) + (20 * 5);
					attacked = true;
				}
			} else if (!flying && this.attackTick <= 0) {
				dragon.slash(entity);
				attacked = true;
			}
			
			// In either case, reset attackTick
			if (attacked) {
				this.attackTick = (int) ((double) this.attackInterval * (1/attacker.getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getAttributeValue()));
			}
		}
		
	}
	
}
