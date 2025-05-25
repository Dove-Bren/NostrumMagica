package com.smanzana.nostrummagica.capabilities;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public interface ILaserReactive {
	
	/**
	 * Called every tick when a laser is attempting to pass through this block.
	 * Returns whether the laser should be allowed through.
	 * @param laserLevel
	 * @param laserPos
	 * @param element
	 * @return
	 */
	public boolean laserPassthroughTick(Level laserLevel, BlockPos laserPos, @Nullable EMagicElement element);
	
	public void laserNearbyTick(Level laserLevel, BlockPos laserPos, @Nullable EMagicElement element);
}
