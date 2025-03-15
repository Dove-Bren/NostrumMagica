package com.smanzana.nostrummagica.entity.tasks;

import java.util.EnumSet;
import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.golem.MagicGolemEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

import net.minecraft.entity.ai.goal.Goal.Flag;

public class GolemTask extends Goal {

	private static final Random rand = new Random();
	private static final double RANGE_SQR = 225.0;
	
	private MagicGolemEntity golem;
	private boolean melee;
	private boolean range;
	private boolean aux;
	
	private int meleeCooldown;
	private int rangeCooldown;
	private int auxCooldown;

	private boolean running;
	private int updateCooldown;
	private int strafeTime;
	private boolean strafeClockwise;
	private boolean strafeBack;
	
	public GolemTask(MagicGolemEntity golem) {
		this.golem = golem;
	
		meleeCooldown = 0;
		rangeCooldown = 0;
		auxCooldown = 0;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	public void initStance(boolean melee, boolean range, boolean aux) {
		this.melee = melee;
		this.range = range;
		this.aux = aux;
	}
	
	@Override
	public boolean canUse() {
		if (!golem.isAlive())
			return false;
		
		if (golem.getTarget() == null)
			return false;
		
		LivingEntity owner = golem.getOwner();
		if (owner == null)
			return true;
		
		double distOwner = 0;
		if (DimensionUtils.SameDimension(owner, golem)) {
			distOwner = owner.distanceToSqr(golem);
		}
		
		if (distOwner > 900.0) {
			// Too far. Don't engage
			return false;
		}
		return true;
	}

	@Override
	public boolean canContinueToUse() {
		return running;
	}
	
	@Override
	public void tick() {
		running = run();
	}
	
	@Override
	public void start() {
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
//		if (golem.getOwner() == null) {
//			return false;
//		}
		
		if (!melee && !range && !aux) {
			return false;
		}
		
		LivingEntity owner = golem.getOwner();
		if (owner == null)
			owner = golem;
		double distOwner = 0;
		if (DimensionUtils.SameDimension(owner, golem)) {
			distOwner = owner.distanceToSqr(golem);
		}
		
		if (distOwner > 900.0) {
			// Too far. Don't engage
			return false;
		}
		
		boolean inMelee = false;
		boolean inRange = false;
		LivingEntity target = golem.getTarget();

		boolean done = false;
		boolean again = true;
		
		boolean ownerCritical = owner.getHealth() < (owner.getMaxHealth() * .35f);
		
		// First, we try to move.
		
		// Else don't execute task
		if (target != null && target.isAlive()) {
			if (!pathTo(target)) {
				return false;
			}
		} else {
			pathTo(owner);
				
			done = true;
			again = false;
		}
		
		// Does not check done so we can do even when target is dead
		if (aux && auxCooldown <= 0 && golem.tickCount > 100) {
			// Can do aux skill if not in melee
			if (!inMelee || ownerCritical) {
				// Figure out who to do it to.
				// Usually do ourselves, but have a chance to aid master first
				LivingEntity first = golem;
				LivingEntity second = owner;
				if (ownerCritical) {
					first = owner;
					second = golem;
				} else if(NostrumMagica.rand.nextFloat() < .2f) {
					first = owner;
					second = golem;
				}
				boolean should = golem.shouldDoBuff(first);
				if (should && first == owner) {
					should = distOwner <= RANGE_SQR;
				}
				
				if (should) {
					golem.doBuffTask(first);
				} else {
					should = golem.shouldDoBuff(second);
					if (should && second == owner)
						should = distOwner <= RANGE_SQR;
					
					if (should)
						golem.doBuffTask(second);
				}
				
				if (should) {
					auxCooldown = 20 * 5 * (1 + GolemTask.rand.nextInt(3));
					done = true;
				}
			}
		}

		if (target != null && target.isAlive()) {
			double distTarget = target.position().distanceTo(golem.position());
			
			double meleeRange = (double)(golem.getBbWidth() * 2.0F * golem.getBbWidth() * 2.0F);
			if (distTarget < meleeRange) {
				inMelee = true;
			}
			if (distTarget < RANGE_SQR) {
				inRange = true;
			}
			
			if (!done && !inMelee && range && inRange && rangeCooldown <= 0) {
				// Can we do a ranged attack?
				if (golem.canSee(target)) {
					golem.doRangeTask(target);
					rangeCooldown = 20 * 3 * (1 + GolemTask.rand.nextInt(3));
					done = true;
				}
			}
			
			if (!done && melee && inMelee && meleeCooldown <= 0) {
				golem.doMeleeTask(target);
				meleeCooldown = 20 * 1;
				done = true;
			}
		}
		
		return again;
	}
	
	private boolean pathTo(LivingEntity target) {
		if (target == null || !target.isAlive())
			return false;
		
		// If we're melee and !inMelee, move
		// Else if we're range and inMelee, move
		// Else if we're range and !inRange, move
		// Else if we're !melee && !range, move to owner
		// Else don't execute task
		boolean success = false;
		if (melee) {
			if (updateCooldown > 0)
        		updateCooldown--;
        	
        	if (updateCooldown > 0 && !golem.getNavigation().isDone())
    			return true;
			
        	//golem.getNavigator().clearPath();
			success = golem.getNavigation().moveTo(target, 1.0);
			if (success) {
				updateCooldown = 15;
			}
		} else if (range) {
			
			success = true;
			double dist = golem.distanceToSqr(target.getX(), target.getBoundingBox().minY, target.getZ());

            if (dist <= RANGE_SQR - 25.0 && golem.canSee(target))
            {
            	golem.getNavigation().stop();
                ++this.strafeTime;
            }
            else
            {
                this.strafeTime = -1;
            	if (updateCooldown > 0)
            		updateCooldown--;
            	
            	if (updateCooldown > 0 && !golem.getNavigation().isDone())
        			return true;
            	
            	golem.getNavigation().stop();
            	golem.getNavigation().moveTo(target, 1.0);
                this.updateCooldown = 5;
            }

            if (this.strafeTime >= 20)
            {
                if ((double) golem.getRandom().nextFloat() < 0.3D)
                {
                    this.strafeClockwise = !this.strafeClockwise;
                }

                if ((double) golem.getRandom().nextFloat() < 0.3D)
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

                golem.getMoveControl().strafe(this.strafeBack ? -0.5F : 0.5F, this.strafeClockwise ? 0.5F : -0.5F);
                golem.lookAt(target, 30.0F, 30.0F);
            }
            else
            {
            	golem.getLookControl().setLookAt(target, 30.0F, 30.0F);
            }
			
		}
		return success;
		
	}
	
}
