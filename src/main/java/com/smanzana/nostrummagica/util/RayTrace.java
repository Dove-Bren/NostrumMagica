package com.smanzana.nostrummagica.util;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.EntityPredicates;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class RayTrace {
	
	public static class NotEntity implements Predicate<LivingEntity> {
		
		private LivingEntity self;
		
		public NotEntity(LivingEntity self) {
			this.self = self;
		}
		
		@Override
		public boolean test(LivingEntity input) {
			return (!input.isEntityEqual(self) && !input.isEntityEqual(self.getRidingEntity()));
		}
	}
	
	public static class LivingOnly implements Predicate<Entity> {
		@Override
		public boolean test(Entity input) {
			LivingEntity living = NostrumMagica.resolveLivingEntity(input);
			return living != null;
		}
	}
	
	public static class OtherLiving implements Predicate<Entity> {
		
		private NotEntity filterMe;
		private LivingOnly filterLiving;
		
		public OtherLiving(LivingEntity self) {
			this.filterMe = new NotEntity(self);
			this.filterLiving = new LivingOnly();
		}
		
		@Override
		public boolean test(Entity input) {
			if (filterLiving.test(input)) {
				// is LivingEntity
				return filterMe.test(NostrumMagica.resolveLivingEntity(input));
			}
			
			return false;
		}
	}
	
	public static Vector3d directionFromAngles(float pitch, float yaw) {
		float f = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -MathHelper.cos(-pitch * 0.017453292F);
        float f3 = MathHelper.sin(-pitch * 0.017453292F);
        
        return new Vector3d((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static RayTraceResult miss(Vector3d fromPos, Vector3d toPos) {
		Vector3d rayVec = toPos.subtract(fromPos);
    	return BlockRayTraceResult.createMiss(fromPos, Direction.getFacingFromVector(rayVec.x, rayVec.y, rayVec.z), new BlockPos(fromPos));
	}
	
	public static @Nullable Entity entFromRaytrace(RayTraceResult result) {
		if (result == null
				|| result.getType() != RayTraceResult.Type.ENTITY) {
			return null;
		}
		
		return ((EntityRayTraceResult) result).getEntity();
	}
	
	public static @Nullable LivingEntity livingFromRaytrace(RayTraceResult result) {
		@Nullable Entity ent = entFromRaytrace(result);
		if (ent != null && ent instanceof LivingEntity) {
			return (LivingEntity) ent;
		}
		return null;
	}

	public static @Nullable BlockPos blockPosFromResult(RayTraceResult result) {
		if (result == null || result.getType() != RayTraceResult.Type.BLOCK) {
			return null;
		}
		
		return ((BlockRayTraceResult) result).getPos();
	}
	
	public static RayTraceResult raytrace(World world, @Nonnull Entity tracingEntity, Vector3d fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return raytrace(world, tracingEntity, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static RayTraceResult raytraceApprox(World world, @Nonnull Entity tracingEntity, Vector3d fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector, double nearbyRadius) {
		if (world == null || fromPos == null)
			return null;
		
		RayTraceResult result = raytrace(world, tracingEntity, fromPos, pitch, yaw, maxDistance, selector);
		if (nearbyRadius > 0) {
			result = nearbyRayTrace(world, result, nearbyRadius, selector);
		}
		return result;
	}
	
	public static RayTraceResult raytrace(World world, @Nonnull Entity tracingEntity, Vector3d fromPos,
			Vector3d direction, float maxDistance, Predicate<? super Entity> selector) {
		Vector3d toPos;
		
		if (world == null || fromPos == null || direction == null)
			return null;
		
		double x = direction.x * maxDistance;
		double y = direction.y * maxDistance;
		double z = direction.z * maxDistance;
		toPos = new Vector3d(fromPos.x + x, fromPos.y + y, fromPos.z + z);
		
		
		return raytrace(world, tracingEntity, fromPos, toPos, selector);
	}
	

	
	public static RayTraceResult raytraceApprox(World world, @Nonnull Entity tracingEntity, Vector3d fromPos,
			Vector3d direction, float maxDistance, Predicate<? super Entity> selector, double nearbyRadius) {
		if (world == null || fromPos == null)
			return null;
		
		RayTraceResult result = raytrace(world, tracingEntity, fromPos, direction, maxDistance, selector);
		if (nearbyRadius > 0) {
			result = nearbyRayTrace(world, result, nearbyRadius, selector);
		}
		return result;
	}

	public static RayTraceResult raytrace(World world, Entity tracingEntity, Vector3d fromPos, Vector3d toPos,
			Predicate<? super Entity> selector) {
		
        if (world == null) {
        	return miss(fromPos, toPos);
        }
        
        RayTraceResult trace;
        
        // First, raytrace against blocks.
        // First we hit also will help us lower the range of our raytrace
        trace = world.rayTraceBlocks(new RayTraceContext(fromPos, toPos, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, tracingEntity));
        
        if (trace != null && trace.getType() != RayTraceResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.getHitVec();
        }
        
        List<Entity> list = world.getEntitiesInAABBexcluding(null,
        		new AxisAlignedBB(fromPos.x, fromPos.y, fromPos.z, toPos.x, toPos.y, toPos.z),
        		EntityPredicates.NOT_SPECTATING.and(new Predicate<Entity>()
        {
            public boolean test(Entity p_apply_1_)
            {
                return !p_apply_1_.noClip && (selector == null || selector.test(p_apply_1_));
            }
        }));
        // d2 is current closest distance
        double minDist = fromPos.distanceTo(toPos);
        Entity curEntity = null;
        Vector3d curEntityVec = null;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)f1, (double)f1, (double)f1);
            Optional<Vector3d> entHit = axisalignedbb.rayTrace(fromPos, toPos);

            if (axisalignedbb.contains(fromPos))
            {
                if (minDist >= 0.0D)
                {
                    curEntity = entity1;
                    minDist = 0.0D;
                }
            }
            else if (entHit.isPresent())
            {
                double d3 = fromPos.distanceTo(entHit.get());

                if (d3 < minDist || minDist == 0.0D)
                {
                    curEntity = entity1;
                    curEntityVec = entHit.get();
                    minDist = d3;
                }
            }
        }

        // If we hit a block, trace is that MOP
        // If we hit an entity between that block, though, we want that
        if (curEntity != null) {
        	trace = new EntityRayTraceResult(curEntity, curEntityVec);
        }
        
        return trace;

	}
	
	
	public static Collection<RayTraceResult> allInPath(World world, @Nonnull Entity tracingEntity,  Vector3d fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return allInPath(world, tracingEntity, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static Collection<RayTraceResult> allInPath(World world, @Nonnull Entity tracingEntity,  Vector3d fromPos,
			Vector3d direction, float maxDistance, Predicate<? super Entity> selector) {
		Vector3d toPos;
		
		if (world == null || fromPos == null || direction == null)
			return new LinkedList<>();
		
		double x = direction.x * maxDistance;
		double y = direction.y * maxDistance;
		double z = direction.z * maxDistance;
		toPos = new Vector3d(fromPos.x + x, fromPos.y + y, fromPos.z + z);
		
		
		return allInPath(world, tracingEntity, fromPos, toPos, selector);
	}
	
	/**
	 * Like a raytrace but returns multiple.
	 * @param world
	 * @param fromPos
	 * @param toPos
	 * @param onlyLiving
	 * @return
	 */
	public static Collection<RayTraceResult> allInPath(World world, @Nonnull Entity tracingEntity, Vector3d fromPos, Vector3d toPos,
			Predicate<? super Entity> selector) {
		
		List<RayTraceResult> ret = new LinkedList<>();
			
        if (world == null || fromPos == null || toPos == null) {
        	return ret;
        }
        
        RayTraceResult trace;
        
        trace = world.rayTraceBlocks(new RayTraceContext(fromPos, toPos, RayTraceContext.BlockMode.OUTLINE, RayTraceContext.FluidMode.NONE, tracingEntity));
        
        if (trace != null && trace.getType() != RayTraceResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.getHitVec();
        	ret.add(cloneTrace(trace));
        }
        
        List<Entity> list = world.getEntitiesInAABBexcluding(null,
        		new AxisAlignedBB(fromPos.x, fromPos.y, fromPos.z, toPos.x, toPos.y, toPos.z),
        		EntityPredicates.NOT_SPECTATING.and(new Predicate<Entity>()
        {
            public boolean test(Entity p_apply_1_)
            {
                return p_apply_1_.canBeCollidedWith() && selector.test(p_apply_1_);
            }
        }));
        
        double maxDist = fromPos.distanceTo(toPos);

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getCollisionBorderSize();
            AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow((double)f1, (double)f1, (double)f1);
            Optional<Vector3d> entHit = axisalignedbb.rayTrace(fromPos, toPos);

            if (axisalignedbb.contains(fromPos))
            {
                ret.add(new EntityRayTraceResult(entity1, fromPos));
            }
            else if (entHit.isPresent())
            {
                double d3 = fromPos.distanceTo(entHit.get());

                if (d3 < maxDist)
                {
                    ret.add(new EntityRayTraceResult(entity1, entHit.get()));
                }
            }
        }

        return ret;
	}
	
	private static RayTraceResult cloneTrace(RayTraceResult in) {
		if (in.getType() == RayTraceResult.Type.ENTITY) {
			EntityRayTraceResult orig = (EntityRayTraceResult) in;
			return new EntityRayTraceResult(orig.getEntity(), orig.getHitVec());
		}
		
		BlockRayTraceResult blockResult = (BlockRayTraceResult) in;
		
		if (in.getType() == RayTraceResult.Type.MISS)
			return BlockRayTraceResult.createMiss(in.getHitVec(), blockResult.getFace(), blockResult.getPos());
		
		
		return new BlockRayTraceResult(blockResult.getHitVec(), blockResult.getFace(), blockResult.getPos(), blockResult.isInside());
	}
	
	public static RayTraceResult forwardsRaycast(Entity projectile, boolean includeEntities, boolean ignoreCollideFlag, boolean shouldExclude, Entity maybeExcludedEntity) {
		return forwardsRaycast(projectile, RayTraceContext.BlockMode.COLLIDER, includeEntities, ignoreCollideFlag, shouldExclude, maybeExcludedEntity);
	}
	
	// Copy of ProjectileUtil method but with ability to collide with other misc entities
	public static RayTraceResult forwardsRaycast(Entity projectile, RayTraceContext.BlockMode blockMode, boolean includeEntities, boolean ignoreCollideFlag, boolean shouldExclude, Entity maybeExcludedEntity){
		double d0 = projectile.getPosX();
		double d1 = projectile.getPosY();
		double d2 = projectile.getPosZ();
		double d3 = projectile.getMotion().x;
		double d4 = projectile.getMotion().y;
		double d5 = projectile.getMotion().z;
		World world = projectile.world;
		Vector3d Vector3d = new Vector3d(d0, d1, d2);
		Vector3d Vector3d1 = new Vector3d(d0 + d3, d1 + d4, d2 + d5);
		RayTraceResult raytraceresult = world.rayTraceBlocks(new RayTraceContext(Vector3d, Vector3d1, blockMode, RayTraceContext.FluidMode.NONE, projectile));

		if (includeEntities)
		{
			if (raytraceresult.getType() != RayTraceResult.Type.MISS)
			{
				Vector3d1 = raytraceresult.getHitVec();
			}

			Entity entity = null;
			Vector3d entityHitVec = null;
			List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(projectile, projectile.getBoundingBox().offset(d3, d4, d5).grow(1.0D));
			double d6 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity)list.get(i);

				if ((ignoreCollideFlag || entity1.canBeCollidedWith()) && (shouldExclude || !entity1.isEntityEqual(maybeExcludedEntity)) && !entity1.noClip)
				{
					AxisAlignedBB axisalignedbb = entity1.getBoundingBox().grow(0.30000001192092896D);
					Optional<Vector3d> innerHit = axisalignedbb.rayTrace(Vector3d, Vector3d1);

					if (innerHit.isPresent())
					{
						double d7 = Vector3d.squareDistanceTo(innerHit.get());

						if (d7 < d6 || d6 == 0.0D)
						{
							entity = entity1;
							entityHitVec = innerHit.get();
							d6 = d7;
						}
					}
				}
			}

			if (entity != null)
			{
				raytraceresult = new EntityRayTraceResult(entity, entityHitVec);
			}
		}

		return raytraceresult;
	}
	
	public static RayTraceResult nearbyRayTrace(World world, RayTraceResult result, Predicate<? super Entity> selector) {
		return nearbyRayTrace(world, result, .5, selector);
	}
	
	public static RayTraceResult nearbyRayTrace(World world, RayTraceResult result, double maxDist, Predicate<? super Entity> selector) {
		if (result == null || (result.getType() == RayTraceResult.Type.ENTITY)) {
			return result;
		}
		
		// Get entities near the result
		Vector3d hitPos = result.getHitVec();
		List<Entity> entities = world.getEntitiesInAABBexcluding(null, new AxisAlignedBB(
				hitPos.x, hitPos.y, hitPos.z, hitPos.x, hitPos.y, hitPos.z
				).grow(maxDist), selector);
		double minDist = 0;
		Entity minEnt = null;
		for (Entity ent : entities) {
			if (selector != null && !selector.test(ent)) {
				continue;
			}
			
			double distSq = hitPos.squareDistanceTo(ent.getPositionVec());
			if (minEnt == null || distSq < minDist) {
				minEnt = ent;
				minDist = distSq;
			}
		}
		
		if (minEnt != null) { 
			result = new EntityRayTraceResult(minEnt);
		}
		
		return result;
	
	}
}
