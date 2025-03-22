package com.smanzana.nostrummagica.entity.tasks.dragon;

import java.util.EnumSet;

import com.smanzana.nostrummagica.entity.dragon.DragonEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class DragonFlyStrafeGoal<T extends DragonEntity> extends Goal {

	protected final T dragon;
	protected final float maxAttackDistance;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime;
	
	public DragonFlyStrafeGoal(T dragon, float maxDistance) {
		this.dragon = dragon;
		this.maxAttackDistance = maxDistance * maxDistance;
		this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean isInterruptable() {
		return true;
	}
	
	@Override
	public boolean canUse() {
		return dragon.getTarget() != null;
	}
	
	@Override
	public boolean canContinueToUse() {
		return canUse();
	}
	
	@Override
	public void start() {
		super.start();
	}
	
	@Override
	public void tick() {
		LivingEntity entitylivingbase = this.dragon.getTarget();

        if (entitylivingbase != null)
        {
            double d0 = this.dragon.distanceToSqr(entitylivingbase.getX(), entitylivingbase.getBoundingBox().minY, entitylivingbase.getZ());
            boolean flag = this.dragon.getSensing().hasLineOfSight(entitylivingbase);
            boolean flag1 = this.seeTime > 0;

            if (flag != flag1)
            {
                this.seeTime = 0;
            }

            if (flag)
            {
                ++this.seeTime;
            }
            else
            {
                --this.seeTime;
            }

            if (d0 <= (double)this.maxAttackDistance && this.seeTime >= 20)
            {
                this.dragon.getNavigation().stop();
                ++this.strafingTime;
            }
            else
            {
                this.dragon.getNavigation().moveTo(entitylivingbase, 1);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20)
            {
                if ((double)this.dragon.getRandom().nextFloat() < 0.3D)
                {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.dragon.getRandom().nextFloat() < 0.3D)
                {
                    this.strafingBackwards = !this.strafingBackwards;
                }

                this.strafingTime = 0;
            }

            if (this.strafingTime > -1)
            {
                if (d0 > (double)(this.maxAttackDistance * 0.75F))
                {
                    this.strafingBackwards = false;
                }
                else if (d0 < (double)(this.maxAttackDistance * 0.25F))
                {
                    //this.strafingBackwards = true;
                }

                this.dragon.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.dragon.lookAt(entitylivingbase, 30.0F, 30.0F);
            }
            else
            {
                this.dragon.getLookControl().setLookAt(entitylivingbase, 30.0F, 30.0F);
            }
        }
	}
	
}
