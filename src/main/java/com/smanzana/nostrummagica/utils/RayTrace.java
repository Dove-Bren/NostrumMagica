package com.smanzana.nostrummagica.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.EntitySelectors;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

public class RayTrace {
	
	public static class NotEntity implements Predicate<EntityLivingBase> {
		
		private EntityLivingBase self;
		
		public NotEntity(EntityLivingBase self) {
			this.self = self;
		}
		
		@Override
		public boolean apply(EntityLivingBase input) {
			return (!input.isEntityEqual(self) && !input.isEntityEqual(self.getRidingEntity()));
		}
	}
	
	public static class LivingOnly implements Predicate<Entity> {
		@Override
		public boolean apply(Entity input) {
			return input != null && input instanceof EntityLivingBase;
		}
	}
	
	public static class OtherLiving implements Predicate<Entity> {
		
		private NotEntity filterMe;
		private LivingOnly filterLiving;
		
		public OtherLiving(EntityLivingBase self) {
			this.filterMe = new NotEntity(self);
			this.filterLiving = new LivingOnly();
		}
		
		@Override
		public boolean apply(Entity input) {
			if (filterLiving.apply(input)) {
				// is EntityLivingBase
				return filterMe.apply((EntityLivingBase) input);
			}
			
			return false;
		}
	}
	
	public static Vec3d directionFromAngles(float pitch, float yaw) {
		float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        
        return new Vec3d((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static RayTraceResult raytrace(World world, Vec3d fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return raytrace(world, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static RayTraceResult raytrace(World world, Vec3d fromPos,
			Vec3d direction, float maxDistance, Predicate<? super Entity> selector) {
		Vec3d toPos;
		
		if (world == null || fromPos == null || direction == null)
			return null;
		
		double x = direction.xCoord * maxDistance;
		double y = direction.yCoord * maxDistance;
		double z = direction.zCoord * maxDistance;
		toPos = new Vec3d(fromPos.xCoord + x, fromPos.yCoord + y, fromPos.zCoord + z);
		
		
		return raytrace(world, fromPos, toPos, selector);
	}

	public static RayTraceResult raytrace(World world, Vec3d fromPos, Vec3d toPos,
			Predicate<? super Entity> selector) {
		
        if (world == null || fromPos == null || toPos == null) {
        	return null;
        }
        
        RayTraceResult trace;
        
        // First, raytrace against blocks.
        // First we hit also will help us lower the range of our raytrace
        trace = world.rayTraceBlocks(fromPos, toPos, false, true, false);
        
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.hitVec;
        }
        
        List<Entity> list = world.getEntitiesInAABBexcluding(null,
        		new AxisAlignedBB(fromPos.xCoord, fromPos.yCoord, fromPos.zCoord, toPos.xCoord, toPos.yCoord, toPos.zCoord),
        		Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            public boolean apply(Entity p_apply_1_)
            {
                return p_apply_1_.canBeCollidedWith() && (selector == null || selector.apply(p_apply_1_));
            }
        }));
        // d2 is current closest distance
        double minDist = fromPos.distanceTo(toPos);
        Entity curEntity = null;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
            RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(fromPos, toPos);

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
        	trace = new RayTraceResult(curEntity);
        }
        
        return trace;

	}
	
	
	public static Collection<RayTraceResult> allInPath(World world, Vec3d fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return allInPath(world, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static Collection<RayTraceResult> allInPath(World world, Vec3d fromPos,
			Vec3d direction, float maxDistance, Predicate<? super Entity> selector) {
		Vec3d toPos;
		
		if (world == null || fromPos == null || direction == null)
			return null;
		
		double x = direction.xCoord * maxDistance;
		double y = direction.yCoord * maxDistance;
		double z = direction.zCoord * maxDistance;
		toPos = new Vec3d(fromPos.xCoord + x, fromPos.yCoord + y, fromPos.zCoord + z);
		
		
		return allInPath(world, fromPos, toPos, selector);
	}
	
	/**
	 * Like a raytrace but returns multiple.
	 * @param world
	 * @param fromPos
	 * @param toPos
	 * @param onlyLiving
	 * @return
	 */
	public static Collection<RayTraceResult> allInPath(World world, Vec3d fromPos, Vec3d toPos,
			Predicate<? super Entity> selector) {
		
		List<RayTraceResult> ret = new LinkedList<>();
			
        if (world == null || fromPos == null || toPos == null) {
        	return ret;
        }
        
        RayTraceResult trace;
        
        trace = world.rayTraceBlocks(fromPos, toPos, false, true, false);
        
        if (trace != null && trace.typeOfHit != RayTraceResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.hitVec;
        	ret.add(cloneTrace(trace));
        }
        
        List<Entity> list = world.getEntitiesInAABBexcluding(null,
        		new AxisAlignedBB(fromPos.xCoord, fromPos.yCoord, fromPos.zCoord, toPos.xCoord, toPos.yCoord, toPos.zCoord),
        		Predicates.and(EntitySelectors.NOT_SPECTATING, new Predicate<Entity>()
        {
            public boolean apply(Entity p_apply_1_)
            {
                return p_apply_1_.canBeCollidedWith() && selector.apply(p_apply_1_);
            }
        }));
        
        double maxDist = fromPos.distanceTo(toPos);

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getEntityBoundingBox().expand((double)f1, (double)f1, (double)f1);
            RayTraceResult movingobjectposition = axisalignedbb.calculateIntercept(fromPos, toPos);

            if (axisalignedbb.isVecInside(fromPos))
            {
                ret.add(new RayTraceResult(entity1));
            }
            else if (movingobjectposition != null)
            {
                double d3 = fromPos.distanceTo(movingobjectposition.hitVec);

                if (d3 < maxDist)
                {
                    ret.add(new RayTraceResult(entity1));
                }
            }
        }

        return ret;
	}
	
	private static RayTraceResult cloneTrace(RayTraceResult in) {
		if (in.typeOfHit == RayTraceResult.Type.ENTITY)
			return new RayTraceResult(in.entityHit);
		
		if (in.typeOfHit == RayTraceResult.Type.MISS)
			return new RayTraceResult(RayTraceResult.Type.MISS, in.hitVec, in.sideHit, in.getBlockPos());
		
		return new RayTraceResult(in.hitVec, in.sideHit, in.getBlockPos());
	}
	
}
