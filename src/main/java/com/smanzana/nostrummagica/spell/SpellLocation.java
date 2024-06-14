package com.smanzana.nostrummagica.spell;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Spells can optionally affect a location. That location sometimes is intended to be
 * the actual block hit by, say, a projectile.
 * Other times, the location is intended to be just outside the block hit.
 * Still other times we want to know where to launch something like a projectile from.
 * This wraps up all the information for both situations and lets effects
 * pick what they'd like to do.
 * @author Skyler
 *
 */
public class SpellLocation {
	/**
	 * These represent the block selected by the shape. For example, the block collided with
	 * by a projectile or contained in an AoE.
	 */
	public final BlockPos selectedBlockPos;
	
	/**
	 * These represent the position of the actual hit.
	 * For example, raytraces set this to be where the actual collision occurred, which is outside the block
	 * collided with.
	 * Some shapes don't have a meaningful difference between this and the selected position.
	 */
	public final Vector3d hitPosition;
	public final BlockPos hitBlockPos;
	
	/**
	 * A position adjusted for shooting from.
	 */
	public final Vector3d shooterPosition;
	
	public SpellLocation(Vector3d hitPosition, BlockPos hitBlockPos, BlockPos selectedBlockPos, Vector3d shooterPosition) {
		this.hitPosition = hitPosition;
		this.hitBlockPos = hitBlockPos;
		this.selectedBlockPos = selectedBlockPos;
		this.shooterPosition = shooterPosition;
	}
	
	/**
	 * Convenience constructor for when there is difference between the hit pos and the selected position.
	 * @param selectedPosition
	 * @param selectedBlockPos
	 */
	public SpellLocation(Vector3d hitPosition, BlockPos hitBlockPos, Vector3d shooterPosition) {
		this(hitPosition, hitBlockPos, hitBlockPos, shooterPosition);
	}
	
	/**
	 * Convenience constructor for when there is no inside or outside and the hit and prev selections are the same.
	 * @param selectedPosition
	 * @param selectedBlockPos
	 */
	public SpellLocation(Vector3d selectedPosition, BlockPos selectedBlockPos) {
		this(selectedPosition, selectedBlockPos, selectedPosition);
	}
	
	public SpellLocation(BlockPos isolatedBlockPos) {
		this(Vector3d.copyCentered(isolatedBlockPos), isolatedBlockPos);
	}
	
	public SpellLocation(Vector3d isolatedPos) {
		this(isolatedPos, new BlockPos(isolatedPos));
	}
	
	@OnlyIn(Dist.CLIENT)
	public SpellLocation(LivingEntity entity, float partialTicks) {
		this(entity.func_242282_l(partialTicks), new BlockPos(entity.func_242282_l(partialTicks)), entity.getEyePosition(partialTicks));
	}
	
	public SpellLocation(LivingEntity entity) {
		this(entity.getPositionVec(), new BlockPos(entity.getPositionVec()), entity.getPositionVec().add(0, entity.getEyeHeight(), 0));
	}
	
	
	/**
	 * Raytrace hit vecs are right on the edge of the block. That means we end up 'rounding up' when
	 * hitting certain faces. Calculate the right outside blockpos.
	 * @param hitVec
	 * @param selectedPos
	 * @return
	 */
	protected static final BlockPos GetHitPos(Vector3d hitVec, BlockPos selectedPos) {
		final Vector3d diff = hitVec.subtract(Vector3d.copyCentered(selectedPos));
		return new BlockPos(
				hitVec.add(diff.normalize().scale(.05))
				);
	}
	
	public SpellLocation(BlockRayTraceResult rayTrace) {
		this(
			rayTrace.getHitVec(),
			GetHitPos(rayTrace.getHitVec(), rayTrace.getPos()),
			rayTrace.getPos(),
			Vector3d.copyCentered(rayTrace.getPos())
		);
	}
	
	public SpellLocation(RayTraceResult rayTrace) {
		this((BlockRayTraceResult) rayTrace); // Kinda lame
	}
	
}
