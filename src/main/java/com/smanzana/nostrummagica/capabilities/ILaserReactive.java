package com.smanzana.nostrummagica.capabilities;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;

public interface ILaserReactive {
	
	public static final record LaserHitResult(boolean stopLaser, @Nullable EMagicElement newElement) {
		public static LaserHitResult PASSTHROUGH = new LaserHitResult(false, null);
		public static LaserHitResult BLOCK = new LaserHitResult(true, null);
	}

	/**
	 * Called every tick when a laser is attempting to pass through this block.
	 * Returns whether the laser should be allowed through.
	 * @param laserLevel
	 * @param laserPos
	 * @param element
	 * @return
	 */
	public LaserHitResult laserPassthroughTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element);
	
	public void laserNearbyTick(LevelAccessor level, BlockPos pos, BlockState state, BlockPos laserPos, EMagicElement element, int beamDistance);
}
