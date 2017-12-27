package com.smanzana.nostrummagica.entity;

import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.ai.EntityAIBase;

public class GolemTask extends EntityAIBase {

	private static final Random rand = new Random();
	private static final double RANGE_SQR = 225.0;
	
	private EntityGolem golem;
	private boolean melee;
	private boolean range;
	private boolean aux;
	
	private int meleeCooldown;
	private int rangeCooldown;
	private int auxCooldown;

	private boolean running;
	private int strafeTime;
	private boolean strafeClockwise;
	private boolean strafeBack;
	
	public GolemTask(EntityGolem golem, boolean melee, boolean range, boolean aux) {
		this.golem = golem;
		this.melee = melee;
		this.range = range;
		this.aux = aux;
		
		meleeCooldown = 0;
		rangeCooldown = 0;
		auxCooldown = 0;
	}
	
	@Override
	public boolean shouldExecute() {
		return true;
	}

	@Override
	public boolean continueExecuting() {
		return running;
	}
	
	@Override
	public void updateTask() {
		running = run();
	}
	
	@Override
	public void startExecuting() {
		meleeCooldown = 0;
		rangeCooldown = 0;
		strafeTime = 0;
		running = run();
	}
	
	private boolean run() {
		/**
		 * First, check if distance to owner is too far.
		 *   - If so, do nothing
		 * Check our aux cooldown.
		 *   - If 0, cast aux and reset randomly
		 * 
		 * Do we not have a target?
		 *   - Do none of the below
		 *   
		 * Are we melee?
		 *   - Move towards melee range of target
		 *   - If within melee range & cooldown is expired, attack
		 *     - If within, don't continue
		 * Are we range?
		 *   - If not melee, move in range
		 *   - if in range and cooldown is expired, do range attack
		 */
		if (meleeCooldown > 0)
			meleeCooldown--;
		if (rangeCooldown > 0)
			rangeCooldown--;
		if (auxCooldown > 0)
			auxCooldown--;
		
		// If dist > some, return false;
		if (golem.getOwner() == null)
			return false;
		
		if (!melee && !range && !aux)
			return false;
		
		EntityLivingBase owner = golem.getOwner();
		double distOwner = 0;
		if (owner.dimension == golem.dimension) {
			distOwner = owner.getDistanceSqToEntity(golem);
		}
		
		if (distOwner > 900.0) {
			// Too far. Don't engage
			return false;
		}
		
		boolean inMelee = false;
		boolean inRange = false;
		EntityLivingBase target = golem.getAttackTarget();
		double distTarget = target.getPositionVector().distanceTo(golem.getPositionVector());
		
		if (distTarget < 1)
			inMelee = true;
		if (distTarget < RANGE_SQR)
			inRange = true;

		boolean done = false;
		
		// First, we try to move.
		
		// Else don't execute task
		if (!target.isDead) {
			if (!pathTo(target))
				return false;
		} else {
			pathTo(owner);
				
			done = true;
		}
		
		// Does not check done so we can do even when target is dead
		if (aux && auxCooldown <= 0 && golem.ticksExisted > 100) {
			// Can do aux skill if not in melee
			if (!inMelee) {
				// Figure out who to do it to. First think of ourselves
				if (golem.shouldDoBuff(golem)) {
					golem.doBuffTask(golem);
				} else if (distOwner < 15) {
					golem.doBuffTask(owner);
				}
				
				auxCooldown = 20 * 5 * (1 + GolemTask.rand.nextInt(3));
				done = true;
			}
		}
		
		if (!done && !inMelee && range && inRange && rangeCooldown <= 0) {
			// Can we do a ranged attack?
			if (golem.canEntityBeSeen(target)) {
				golem.doRangeTask(target);
				rangeCooldown = 20 * 3 * (1 + GolemTask.rand.nextInt(3));
				done = true;
			}
		}
		
		if (!done && melee && inMelee && meleeCooldown <= 0) {
			golem.doMeleeTask(golem);
			meleeCooldown = 20 * 1;
			done = true;
		}
		
		return true;
	}
	
	private boolean pathTo(EntityLivingBase target) {
		// If we're melee and !inMelee, move
		// Else if we're range and inMelee, move
		// Else if we're range and !inRange, move
		// Else if we're !melee && !range, move to owner
		// Else don't execute task
		boolean success = false;
		if (melee) {
			success = (null != golem.getNavigator().getPathToEntityLiving(target));
		} else if (range) {
			
			double dist = golem.getDistanceSq(target.posX, target.getEntityBoundingBox().minY, target.posZ);

            if (dist <= RANGE_SQR)
            {
            	golem.getNavigator().clearPathEntity();
                ++this.strafeTime;
            }
            else
            {
            	golem.getNavigator().tryMoveToEntityLiving(target, 1.0);
                this.strafeTime = -1;
            }

            if (this.strafeTime >= 20)
            {
                if ((double) golem.getRNG().nextFloat() < 0.3D)
                {
                    this.strafeClockwise = !this.strafeClockwise;
                }

                if ((double) golem.getRNG().nextFloat() < 0.3D)
                {
                    this.strafeBack = !this.strafeBack;
                }

                this.strafeTime = 0;
            }

            if (this.strafeTime > -1)
            {
                if (dist > (double)(RANGE_SQR * 0.75F))
                {
                    this.strafeBack = false;
                }
                else if (dist < (double)(RANGE_SQR * 0.25F))
                {
                    this.strafeBack = true;
                }

                golem.getMoveHelper().strafe(this.strafeBack ? -0.5F : 0.5F, this.strafeClockwise ? 0.5F : -0.5F);
                golem.faceEntity(target, 30.0F, 30.0F);
            }
            else
            {
            	golem.getLookHelper().setLookPositionWithEntity(target, 30.0F, 30.0F);
            }
			
		}
		return success;
		
	}
	
}
