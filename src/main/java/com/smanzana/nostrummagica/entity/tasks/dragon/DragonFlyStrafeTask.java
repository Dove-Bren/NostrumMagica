package com.smanzana.nostrummagica.entity.tasks.dragon;

import com.smanzana.nostrummagica.entity.dragon.EntityDragon;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.goal.Goal;

public class DragonFlyStrafeTask<T extends EntityDragon> extends Goal {

	protected final T dragon;
	protected final float maxAttackDistance;
	private int seeTime;
	private boolean strafingClockwise;
	private boolean strafingBackwards;
	private int strafingTime;
	
	public DragonFlyStrafeTask(T dragon, float maxDistance) {
		this.dragon = dragon;
		this.maxAttackDistance = maxDistance * maxDistance;
		this.setMutexFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
	}
	
	@Override
	public boolean isInterruptible() {
		return true;
	}
	
	@Override
	public boolean shouldExecute() {
		return dragon.getAttackTarget() != null;
	}
	
	@Override
	public boolean shouldContinueExecuting() {
		return shouldExecute();
	}
	
	@Override
	public void startExecuting() {
		super.startExecuting();
	}
	
	@Override
	public void tick() {
		LivingEntity entitylivingbase = this.dragon.getAttackTarget();

        if (entitylivingbase != null)
        {
            double d0 = this.dragon.getDistanceSq(entitylivingbase.posX, entitylivingbase.getBoundingBox().minY, entitylivingbase.posZ);
            boolean flag = this.dragon.getEntitySenses().canSee(entitylivingbase);
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
                this.dragon.getNavigator().clearPath();
                ++this.strafingTime;
            }
            else
            {
                this.dragon.getNavigator().tryMoveToEntityLiving(entitylivingbase, 1);
                this.strafingTime = -1;
            }

            if (this.strafingTime >= 20)
            {
                if ((double)this.dragon.getRNG().nextFloat() < 0.3D)
                {
                    this.strafingClockwise = !this.strafingClockwise;
                }

                if ((double)this.dragon.getRNG().nextFloat() < 0.3D)
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

                this.dragon.getMoveHelper().strafe(this.strafingBackwards ? -0.5F : 0.5F, this.strafingClockwise ? 0.5F : -0.5F);
                this.dragon.faceEntity(entitylivingbase, 30.0F, 30.0F);
            }
            else
            {
                this.dragon.getLookHelper().setLookPositionWithEntity(entitylivingbase, 30.0F, 30.0F);
            }
        }
	}
	
}
