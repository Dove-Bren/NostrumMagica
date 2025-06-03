package com.smanzana.nostrummagica.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface IPortalBlock {

	public boolean attemptTeleport(Level world, BlockPos portalPos, BlockState worldBlock, Entity entity);

}
