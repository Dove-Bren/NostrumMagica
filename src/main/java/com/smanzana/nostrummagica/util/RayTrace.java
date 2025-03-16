package com.smanzana.nostrummagica.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class RayTrace {
	
	public static class NotEntity implements Predicate<LivingEntity> {
		
		private LivingEntity self;
		
		public NotEntity(LivingEntity self) {
			this.self = self;
		}
		
		@Override
		public boolean test(LivingEntity input) {
			return (!input.is(self) && !input.is(self.getVehicle()));
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
	
	public static Vec3 directionFromAngles(float pitch, float yaw) {
		float f = Mth.cos(-yaw * 0.017453292F - (float)Math.PI);
        float f1 = Mth.sin(-yaw * 0.017453292F - (float)Math.PI);
        float f2 = -Mth.cos(-pitch * 0.017453292F);
        float f3 = Mth.sin(-pitch * 0.017453292F);
        
        return new Vec3((double)(f1 * f2), (double)f3, (double)(f * f2));
	}
	
	public static HitResult miss(Vec3 fromPos, Vec3 toPos) {
		Vec3 rayVec = toPos.subtract(fromPos);
    	return BlockHitResult.miss(fromPos, Direction.getNearest(rayVec.x, rayVec.y, rayVec.z), new BlockPos(fromPos));
	}
	
	public static @Nullable Entity entFromRaytrace(HitResult result) {
		if (result == null
				|| result.getType() != HitResult.Type.ENTITY) {
			return null;
		}
		
		return ((EntityHitResult) result).getEntity();
	}
	
	public static @Nullable LivingEntity livingFromRaytrace(HitResult result) {
		@Nullable Entity ent = entFromRaytrace(result);
		if (ent != null && ent instanceof LivingEntity) {
			return (LivingEntity) ent;
		}
		return null;
	}

	public static @Nullable BlockPos blockPosFromResult(HitResult result) {
		if (result == null || result.getType() != HitResult.Type.BLOCK) {
			return null;
		}
		
		return ((BlockHitResult) result).getBlockPos();
	}
	
	public static HitResult raytrace(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return raytrace(world, tracingEntity, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static HitResult raytraceApprox(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector, double nearbyRadius) {
		if (world == null || fromPos == null)
			return null;
		
		HitResult result = raytrace(world, tracingEntity, fromPos, pitch, yaw, maxDistance, selector);
		if (nearbyRadius > 0) {
			result = nearbyRayTrace(world, result, nearbyRadius, selector);
		}
		return result;
	}
	
	public static HitResult raytrace(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos,
			Vec3 direction, float maxDistance, Predicate<? super Entity> selector) {
		Vec3 toPos;
		
		if (world == null || fromPos == null || direction == null)
			return null;
		
		double x = direction.x * maxDistance;
		double y = direction.y * maxDistance;
		double z = direction.z * maxDistance;
		toPos = new Vec3(fromPos.x + x, fromPos.y + y, fromPos.z + z);
		
		
		return raytrace(world, tracingEntity, fromPos, toPos, selector);
	}
	

	
	public static HitResult raytraceApprox(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos,
			Vec3 direction, float maxDistance, Predicate<? super Entity> selector, double nearbyRadius) {
		if (world == null || fromPos == null)
			return null;
		
		HitResult result = raytrace(world, tracingEntity, fromPos, direction, maxDistance, selector);
		if (nearbyRadius > 0) {
			result = nearbyRayTrace(world, result, nearbyRadius, selector);
		}
		return result;
	}

	public static HitResult raytrace(Level world, Entity tracingEntity, Vec3 fromPos, Vec3 toPos,
			Predicate<? super Entity> selector) {
		
        if (world == null) {
        	return miss(fromPos, toPos);
        }
        
        HitResult trace;
        
        // First, raytrace against blocks.
        // First we hit also will help us lower the range of our raytrace
        trace = world.clip(new ClipContext(fromPos, toPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, tracingEntity));
        
        if (trace != null && trace.getType() != HitResult.Type.MISS) {
        	// limit toPos to position of block hit
        	toPos = trace.getLocation();
        }
        
        List<Entity> list = world.getEntities((Entity) null,
        		new AABB(fromPos.x, fromPos.y, fromPos.z, toPos.x, toPos.y, toPos.z),
        		EntitySelector.NO_SPECTATORS.and(new Predicate<Entity>()
        {
            public boolean test(Entity p_apply_1_)
            {
                return !p_apply_1_.noPhysics && (selector == null || selector.test(p_apply_1_));
            }
        }));
        // d2 is current closest distance
        double minDist = fromPos.distanceTo(toPos);
        Entity curEntity = null;
        Vec3 curEntityVec = null;

        for (int j = 0; j < list.size(); ++j)
        {
            Entity entity1 = (Entity)list.get(j);
            
            float f1 = entity1.getPickRadius();
            AABB axisalignedbb = entity1.getBoundingBox().inflate((double)f1, (double)f1, (double)f1);
            Optional<Vec3> entHit = axisalignedbb.clip(fromPos, toPos);

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
        	trace = new EntityHitResult(curEntity, curEntityVec);
        }
        
        return trace;

	}
	
	
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity,  Vec3 fromPos, float pitch,
			float yaw, float maxDistance, Predicate<? super Entity> selector) {
		if (world == null || fromPos == null)
			return null;
		
		return allInPath(world, tracingEntity, fromPos, directionFromAngles(pitch, yaw), maxDistance, selector);
	}
	
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity,  Vec3 fromPos,
			Vec3 direction, float maxDistance, Predicate<? super Entity> selector) {
		Vec3 toPos;
		
		if (world == null || fromPos == null || direction == null)
			return new LinkedList<>();
		
		double x = direction.x * maxDistance;
		double y = direction.y * maxDistance;
		double z = direction.z * maxDistance;
		toPos = new Vec3(fromPos.x + x, fromPos.y + y, fromPos.z + z);
		
		
		return allInPath(world, tracingEntity, fromPos, toPos, selector);
	}
	
	protected static final boolean cursorInBounds(Vec3 cursorIn, Vec3 from, Vec3 to) {
		final int minX = (int) to.x > (int) from.x ? (int) from.x : (int) to.x;
		final int minY = (int) to.y > (int) from.y ? (int) from.y : (int) to.y;
		final int minZ = (int) to.z > (int) from.z ? (int) from.z : (int) to.z;
		
		final int maxX = (int) to.x > (int) from.x ? (int) to.x : (int) from.x;
		final int maxY = (int) to.y > (int) from.y ? (int) to.y : (int) from.y;
		final int maxZ = (int) to.z > (int) from.z ? (int) to.z : (int) from.z;
		
		// note: min and max may be equal
		
		BlockPos cursor = new BlockPos(cursorIn);
		if (cursor.getX() < minX || cursor.getX() > maxX) return false;
		if (cursor.getY() < minY || cursor.getY() > maxY) return false;
		if (cursor.getZ() < minZ || cursor.getZ() > maxZ) return false;
		return true;
	}
	
	protected static Collection<BlockHitResult> allBlocksInPath(Level world, Vec3 fromPos, Vec3 toPos,
			ClipContext.Block blockMode, ClipContext.Fluid fluidMode, CollisionContext selectMode, boolean includeAir) {
		// Can't use world.rayTraceBlocks since it stops at first hit
		
		// Going to step 'diff' units at a time and run a raytrace for every blockpos encountered.
		// Calculate diff based on actual difference in where we're tracing, but scaled down so that no dimension is greater than 1.
		// Just normalize/2 to achieve this even though this can miss diagonals.
		final Vec3 diff = toPos.subtract(fromPos).normalize().scale(.5);
		final Set<BlockPos> seen = new HashSet<>();
		final List<BlockHitResult> results = new ArrayList<>(32);
		Vec3 cursor = new Vec3(fromPos.x, fromPos.y, fromPos.z);
		
		do {
			final BlockPos blockPos = new BlockPos(cursor);
			if (!seen.contains(blockPos)) {
				seen.add(blockPos.immutable());
				
				// This is generally the contents of the raytracing func IBlockReader#rayTraceBlocks passes to #doRayTrace.
				final BlockState state = world.getBlockState(blockPos);
				final FluidState fluidstate = world.getFluidState(blockPos);
				
				// Do individual block raytrace
				final VoxelShape blockShape = world.isEmptyBlock(blockPos) && includeAir ? Shapes.block() : blockMode.get(state, world, blockPos, selectMode);
				final BlockHitResult blockResult = world.clipWithInteractionOverride(fromPos, toPos, blockPos, blockShape, state);
				final VoxelShape fluidShape = fluidMode.canPick(fluidstate) ? fluidstate.getShape(world, blockPos) : Shapes.empty();
				final BlockHitResult fluidResult = fluidShape.clip(fromPos, toPos, blockPos);
				
				// Ooops don't do this. Add either/both if non-null
//				// Different than vanilla: do some null checks before comparing distances.
//				// Not sure if the added branches are actually slower though.
//				final @Nullable BlockRayTraceResult resultToAdd;
//				if (blockResult == null && fluidResult != null) {
//					resultToAdd = fluidResult;
//				} else if (fluidResult == null && blockResult != null) {
//					resultToAdd = blockResult;
//				} else {
//					// Decide between block and fluid based on distance
//					final double blockDist = blockResult == null ? Double.MAX_VALUE : fromPos.squareDistanceTo(blockResult.getHitVec());
//					final double fluidDist = fluidResult == null ? Double.MAX_VALUE : fromPos.squareDistanceTo(fluidResult.getHitVec());
//					resultToAdd = blockDist <= fluidDist ? blockResult : fluidResult;
//				}
//				
//				if (resultToAdd != null) {
//					results.add(resultToAdd);
//				}
				if (blockResult != null) {
					results.add(blockResult);
				}
				if (fluidResult != null) {
					results.add(fluidResult);
				}
				
				
				// Actual vanilla code for reference:
				{
					/****************************
					BlockState blockstate = this.getBlockState(p_217297_2_);
					FluidState fluidstate = this.getFluidState(p_217297_2_);
					Vector3d vector3d = p_217297_1_.getStartVec();
					Vector3d vector3d1 = p_217297_1_.getEndVec();
					VoxelShape voxelshape = p_217297_1_.getBlockShape(blockstate, this, p_217297_2_);
					BlockRayTraceResult blockraytraceresult = this.rayTraceBlocks(vector3d, vector3d1, p_217297_2_, voxelshape, blockstate);
					VoxelShape voxelshape1 = p_217297_1_.getFluidShape(fluidstate, this, p_217297_2_);
					BlockRayTraceResult blockraytraceresult1 = voxelshape1.rayTrace(vector3d, vector3d1, p_217297_2_);
					double d0 = blockraytraceresult == null ? Double.MAX_VALUE : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult.getHitVec());
					double d1 = blockraytraceresult1 == null ? Double.MAX_VALUE : p_217297_1_.getStartVec().squareDistanceTo(blockraytraceresult1.getHitVec());
					return d0 <= d1 ? blockraytraceresult : blockraytraceresult1;
					 ****************************/
				}
			}
			cursor = cursor.add(diff);
		} while (cursorInBounds(cursor, fromPos, toPos));
		
		return results;
	}
	
	protected static Collection<EntityHitResult> allEntitiesInPath(Level world, Vec3 fromPos, Vec3 toPos, Predicate<? super Entity> selector) {
		final List<EntityHitResult> results = new ArrayList<>(8);
		
		List<Entity> list = world.getEntities((Entity) null,
				new AABB(fromPos.x, fromPos.y, fromPos.z, toPos.x, toPos.y, toPos.z),
				EntitySelector.NO_SPECTATORS.and((e) -> e.isPickable() && selector.test(e)));
		
		double maxDist = fromPos.distanceTo(toPos);

		for (int j = 0; j < list.size(); ++j) {
			Entity entity1 = (Entity)list.get(j);

			float f1 = entity1.getPickRadius();
			AABB axisalignedbb = entity1.getBoundingBox().inflate((double)f1, (double)f1, (double)f1);
			Optional<Vec3> entHit = axisalignedbb.clip(fromPos, toPos);

			if (axisalignedbb.contains(fromPos)) {
				results.add(new EntityHitResult(entity1, fromPos));
			} else if (entHit.isPresent()) {
				double d3 = fromPos.distanceTo(entHit.get());

				if (d3 < maxDist) {
					results.add(new EntityHitResult(entity1, entHit.get()));
				}
			}
		}
		
		return results;
	}
	
	/**
	 * Like a raytrace but returns multiple.
	 * @param world
	 * @param fromPos
	 * @param toPos
	 * @param onlyLiving
	 * @return
	 */
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, Vec3 toPos,
			Predicate<? super Entity> selector) {
		return allInPath(world, tracingEntity, fromPos, toPos, selector, false);
	}
	
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, Vec3 toPos,
			Predicate<? super Entity> selector, boolean includeAir) {
		return allInPath(world, tracingEntity, fromPos, toPos, selector,
				ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, includeAir);
	}
	
	public static Collection<HitResult> allInPath(Level world, @Nonnull Entity tracingEntity, Vec3 fromPos, Vec3 toPos,
			Predicate<? super Entity> selector, ClipContext.Block blockMode, ClipContext.Fluid fluidMode, boolean includeAir) {
		
		if (world == null || fromPos == null || toPos == null) {
			return new ArrayList<>();
		}
		
		final Collection<BlockHitResult> blocks = allBlocksInPath(world, fromPos, toPos, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, CollisionContext.of(tracingEntity), includeAir); 
		final Collection<EntityHitResult> entities = allEntitiesInPath(world, fromPos, toPos, selector);
		final List<HitResult> ret = new ArrayList<>(blocks.size() + entities.size());
		
		ret.addAll(blocks);
		ret.addAll(entities);
		
		return ret;
	}
	
	public static HitResult forwardsRaycast(Entity projectile, boolean includeEntities, boolean ignoreCollideFlag, boolean shouldExclude, Entity maybeExcludedEntity) {
		return forwardsRaycast(projectile, ClipContext.Block.COLLIDER, includeEntities, ignoreCollideFlag, shouldExclude, maybeExcludedEntity);
	}
	
	// Copy of ProjectileUtil method but with ability to collide with other misc entities
	public static HitResult forwardsRaycast(Entity projectile, ClipContext.Block blockMode, boolean includeEntities, boolean ignoreCollideFlag, boolean shouldExclude, Entity maybeExcludedEntity){
		double d0 = projectile.getX();
		double d1 = projectile.getY();
		double d2 = projectile.getZ();
		double d3 = projectile.getDeltaMovement().x;
		double d4 = projectile.getDeltaMovement().y;
		double d5 = projectile.getDeltaMovement().z;
		Level world = projectile.level;
		Vec3 Vector3d = new Vec3(d0, d1, d2);
		Vec3 Vector3d1 = new Vec3(d0 + d3, d1 + d4, d2 + d5);
		HitResult raytraceresult = world.clip(new ClipContext(Vector3d, Vector3d1, blockMode, ClipContext.Fluid.NONE, projectile));

		if (includeEntities)
		{
			if (raytraceresult.getType() != HitResult.Type.MISS)
			{
				Vector3d1 = raytraceresult.getLocation();
			}

			Entity entity = null;
			Vec3 entityHitVec = null;
			List<Entity> list = world.getEntities(projectile, projectile.getBoundingBox().move(d3, d4, d5).inflate(1.0D));
			double d6 = 0.0D;

			for (int i = 0; i < list.size(); ++i)
			{
				Entity entity1 = (Entity)list.get(i);

				if ((ignoreCollideFlag || entity1.isPickable()) && (shouldExclude || !entity1.is(maybeExcludedEntity)) && !entity1.noPhysics)
				{
					AABB axisalignedbb = entity1.getBoundingBox().inflate(0.30000001192092896D);
					Optional<Vec3> innerHit = axisalignedbb.clip(Vector3d, Vector3d1);

					if (innerHit.isPresent())
					{
						double d7 = Vector3d.distanceToSqr(innerHit.get());

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
				raytraceresult = new EntityHitResult(entity, entityHitVec);
			}
		}

		return raytraceresult;
	}
	
	public static HitResult nearbyRayTrace(Level world, HitResult result, Predicate<? super Entity> selector) {
		return nearbyRayTrace(world, result, .5, selector);
	}
	
	public static HitResult nearbyRayTrace(Level world, HitResult result, double maxDist, Predicate<? super Entity> selector) {
		if (result == null || (result.getType() == HitResult.Type.ENTITY)) {
			return result;
		}
		
		// Get entities near the result
		Vec3 hitPos = result.getLocation();
		List<Entity> entities = world.getEntities((Entity) null, new AABB(
				hitPos.x, hitPos.y, hitPos.z, hitPos.x, hitPos.y, hitPos.z
				).inflate(maxDist), selector);
		double minDist = 0;
		Entity minEnt = null;
		for (Entity ent : entities) {
			if (selector != null && !selector.test(ent)) {
				continue;
			}
			
			double distSq = hitPos.distanceToSqr(ent.position());
			if (minEnt == null || distSq < minDist) {
				minEnt = ent;
				minDist = distSq;
			}
		}
		
		if (minEnt != null) { 
			result = new EntityHitResult(minEnt);
		}
		
		return result;
	
	}
}
