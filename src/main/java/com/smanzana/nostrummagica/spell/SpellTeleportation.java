package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class SpellTeleportation {
	
	private static final boolean isPassable(World world, BlockPos pos) {
		if (world.isEmptyBlock(pos))
			return true;
		
		BlockState state = world.getBlockState(pos);
		
		if (state == null)
			return true;
		if (state.getMaterial().isLiquid())
			return true;
		if (!state.getMaterial().blocksMotion())
			return true;
		
		return false;
	}
	
	protected static final @Nullable Vector3d BlinkNoPassthrough(Entity entity, Vector3d source, Vector3d idealDestination) {
		Vector3d dest = null;
		RayTraceResult mop = entity.level.clip(new RayTraceContext(source, idealDestination, BlockMode.COLLIDER, FluidMode.NONE, entity));
		if (mop != null && mop.getLocation().distanceTo(source) > 0.5) {
			// We got one
			BlockPos pos;
			if (mop.getType() == RayTraceResult.Type.BLOCK) {
				SpellLocation hitLoc = new SpellLocation(entity.level, mop);
				pos = hitLoc.hitBlockPos;
			} else {
				pos = new BlockPos(mop.getLocation()); 
			}
			
			// Adjust down 1 if it's clear but one above is not
			if (isPassable(entity.level, pos) && !isPassable(entity.level, pos.offset(0, 1, 0)) && isPassable(entity.level, pos.offset(0, -1, 0))) {
				pos = pos.below();
			}
			
			if (isPassable(entity.level, pos) && isPassable(entity.level, pos.offset(0, 1, 0))) {
				dest = Vector3d.atCenterOf(pos);
			}
		}
		return dest;
	}
	
	protected static final @Nullable Vector3d BlinkPassthrough(Entity entity, Vector3d source, Vector3d idealDestination) {
		int i = 4; // Attempt raytrace from (20% * i * pathlength)
		Vector3d dest = null;
		Vector3d translation = idealDestination.subtract(source);
		Vector3d from;
		double curDist;
		while (i >= 0) {
			if (i == 0) {
				// optimization
				from = source;
			} else {
				curDist = (.2 * i);
				from = new Vector3d(translation.x * curDist,
						translation.y * curDist,
						translation.z * curDist);
				from = source.add(from);
			}
			
			dest = BlinkNoPassthrough(entity, from, idealDestination);
			if (dest != null) {
				break;
			}
			
			i--;
		}
		
		return dest;
	}

	public static final @Nullable Vector3d Blink(Entity entity, Vector3d source, Vector3d direction, double dist, boolean passthrough) {
		Vector3d dest;
		BlockPos bpos;
		Vector3d translation = new Vector3d(direction.x * dist,
				direction.y * dist,
				direction.z * dist);
		
		// Find ideal dest (vect addition). Can we go there? Then go there.
		// Else step backwards and raycast forward in 1/5 increments.
		// See if place we hit is same spot as raycast. If so, fail and do again
		
		dest = source.add(translation);
		bpos = new BlockPos(dest.x, dest.y, dest.z);
		// If passthrough, see if we get lucky and ideal spot has space
		if (passthrough
			&& isPassable(entity.level, bpos)
			&& isPassable(entity.level, bpos.offset(0, 1, 0))) {
				// Whoo! Looks like we can teleport there!
		} else {
			if (passthrough) {
				dest = BlinkPassthrough(entity, source, dest);
			} else {
				dest = BlinkNoPassthrough(entity, source, dest);
			}
		}
		
		return dest;
	}
	
}
