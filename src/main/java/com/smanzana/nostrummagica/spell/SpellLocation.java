package com.smanzana.nostrummagica.spell;

import java.util.Objects;

import com.smanzana.nostrummagica.util.RayTrace;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
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
	public final Level world;
	
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
	public final Vec3 hitPosition;
	public final BlockPos hitBlockPos;
	
	/**
	 * A position adjusted for shooting from.
	 */
	public final Vec3 shooterPosition;
	
	public SpellLocation(Level world, Vec3 hitPosition, BlockPos hitBlockPos, BlockPos selectedBlockPos, Vec3 shooterPosition) {
		this.world = world;
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
	public SpellLocation(Level world, Vec3 hitPosition, BlockPos hitBlockPos, Vec3 shooterPosition) {
		this(world, hitPosition, hitBlockPos, hitBlockPos, shooterPosition);
	}
	
	/**
	 * Convenience constructor for when there is no inside or outside and the hit and prev selections are the same.
	 * @param selectedPosition
	 * @param selectedBlockPos
	 */
	public SpellLocation(Level world, Vec3 selectedPosition, BlockPos selectedBlockPos) {
		this(world, selectedPosition, selectedBlockPos, selectedPosition);
	}
	
	public SpellLocation(Level world, BlockPos isolatedBlockPos) {
		this(world, Vec3.atCenterOf(isolatedBlockPos), isolatedBlockPos);
	}
	
	public SpellLocation(Level world, Vec3 isolatedPos) {
		this(world, isolatedPos, new BlockPos(isolatedPos));
	}
	
	@OnlyIn(Dist.CLIENT)
	public SpellLocation(LivingEntity entity, float partialTicks) {
		this(entity.getCommandSenderWorld(), entity.getPosition(partialTicks), new BlockPos(entity.getPosition(partialTicks)), entity.getEyePosition(partialTicks));
	}
	
	public SpellLocation(LivingEntity entity) {
		this(entity.getCommandSenderWorld(), entity.position(), new BlockPos(entity.position()), entity.position().add(0, entity.getEyeHeight(), 0));
	}
	
	
	public SpellLocation(Level world, BlockHitResult rayTrace) {
		this(world,
			rayTrace.getLocation(),
			RayTrace.outsideBlockPosFromResult(rayTrace),
			rayTrace.getBlockPos(),
			Vec3.atCenterOf(rayTrace.getBlockPos())
		);
	}
	
	public SpellLocation(Level world, HitResult rayTrace) {
		this(world, (BlockHitResult) rayTrace); // Kinda lame
	}
	
	@Override
	public boolean equals(Object o) {
		if (o instanceof SpellLocation) {
			final SpellLocation other = (SpellLocation) o;
			return world.equals(other.world)
					&& selectedBlockPos.equals(other.selectedBlockPos)
					&& hitPosition.equals(other.hitPosition)
					&& hitBlockPos.equals(other.hitBlockPos)
					&& shooterPosition.equals(other.shooterPosition)
					;
		}
		
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(world, selectedBlockPos, hitPosition, hitBlockPos, shooterPosition);
	}
	
	public static final Direction InferFace(SpellLocation location) {
		final Vec3 selectedCenter = Vec3.atCenterOf(location.selectedBlockPos);
		final Vec3 dirVec = selectedCenter.subtract(location.hitPosition).normalize();
		return Direction.getNearest(dirVec.x, dirVec.y, dirVec.z);
	}
	
}
