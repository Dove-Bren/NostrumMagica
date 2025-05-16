package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.ClipContext.Block;
import net.minecraft.world.level.ClipContext.Fluid;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SpellTeleportation {
	
	private static final boolean isPassable(Level world, BlockPos pos) {
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
	
	protected static final @Nullable Vec3 BlinkNoPassthrough(Entity entity, Vec3 source, Vec3 idealDestination) {
		Vec3 dest = null;
		HitResult mop = entity.level.clip(new ClipContext(source, idealDestination, Block.COLLIDER, Fluid.NONE, entity));
		if (mop != null && mop.getLocation().distanceTo(source) > 0.5) {
			// We got one
			BlockPos pos;
			if (mop.getType() == HitResult.Type.BLOCK) {
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
				dest = Vec3.atCenterOf(pos);
			}
		}
		return dest;
	}
	
	protected static final @Nullable Vec3 BlinkPassthrough(Entity entity, Vec3 source, Vec3 idealDestination) {
		int i = 4; // Attempt raytrace from (20% * i * pathlength)
		Vec3 dest = null;
		Vec3 translation = idealDestination.subtract(source);
		Vec3 from;
		double curDist;
		while (i >= 0) {
			if (i == 0) {
				// optimization
				from = source;
			} else {
				curDist = (.2 * i);
				from = new Vec3(translation.x * curDist,
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

	public static final @Nullable Vec3 Blink(Entity entity, Vec3 source, Vec3 direction, double dist, boolean passthrough) {
		Vec3 dest;
		BlockPos bpos;
		Vec3 translation = new Vec3(direction.x * dist,
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
