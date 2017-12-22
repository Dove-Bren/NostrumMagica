package com.smanzana.nostrummagica.utils;

import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

public class RayTrace {
	
	public static Vec3 directionFromAngles(float pitch, float yaw) {
		float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static MovingObjectPosition raytrace(World world, Vec3 fromPos, float pitch,
			float yaw, float maxDistance, boolean onlyLiving) {
		if (world == null || fromPos == null)
			return null;
		
		return raytrace(world, fromPos, directionFromAngles(pitch, yaw), maxDistance, onlyLiving);
	}
	
	public static MovingObjectPosition raytrace(World world, Vec3 fromPos,
			Vec3 direction, float maxDistance, boolean onlyLiving) {
		Vec3 toPos;
		
		if (world == null || fromPos == null || direction == null)
			return null;
		
		double x = direction.xCoord * maxDistance;
		double y = direction.xCoord * maxDistance;
		double z = direction.xCoord * maxDistance;
		toPos = new Vec3(fromPos.xCoord + x, fromPos.yCoord + z, fromPos.zCoord + y);
		
		
		return raytrace(world, fromPos, toPos, onlyLiving);
	}

	public static MovingObjectPosition raytrace(World world, Vec3 fromPos, Vec3 toPos,
			boolean onlyLiving) {
		
        if (world == null || fromPos == null || toPos == null) {
        	return null;
        }
        
        MovingObjectPosition trace;
        
        // First, raytrace against blocks.
        // First we hit also will help us lower the range of our raytrace
        trace = world.rayTraceBlocks(fromPos, toPos, false, true, false);
        
        if (trace != null && trace.typeOfHit != MovingObjectPosition.MovingObjectType.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.hitVec;
        }
        
        
        // d0 is total range
        // d1 is range to block selected

        // vec3 is from pos
        // vec31 is direction
        // vec32 is toPos
        List<Entity> list = world.getEntitiesInAABBexcluding(null,
        		AxisAlignedBB.fromBounds(fromPos.xCoord, fromPos.yCoord, fromPos.zCoord, toPos.xCoord, toPos.yCoord, toPos.zCoord),
        		Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            public boolean apply(Entity p_apply_1_)
            {
                return p_apply_1_.canBeCollidedWith();
            }
        }));
        // d2 is current closest distance
        double minDist = fromPos.distanceTo(toPos);
        Entity curEntity = null;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            if (onlyLiving && !(entity1 instanceof EntityLiving))
            	continue;
            
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
            MovingObjectPosition movingobjectposition = axisalignedbb.calculateIntercept(fromPos, toPos);

            if (axisalignedbb.isVecInside(fromPos))
            {
                if (minDist >= 0.0D)
                {
                    curEntity = entity1;
                    minDist = 0.0D;
                }
            }
            else if (movingobjectposition != null)
            {
                double d3 = fromPos.distanceTo(movingobjectposition.hitVec);

                if (d3 < minDist || minDist == 0.0D)
                {
                    curEntity = entity1;
                    minDist = d3;
                }
            }
        }

        // If we hit a block, trace is that MOP
        // If we hit an entity between that block, though, we want that
        if (curEntity != null) {
        	trace = new MovingObjectPosition(curEntity);
        }
        
        return trace;

	}
	
}
