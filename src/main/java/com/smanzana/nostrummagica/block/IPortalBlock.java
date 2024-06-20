package com.smanzana.nostrummagica.block;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface IPortalBlock {

	public boolean attemptTeleport(World world, BlockPos portalPos, BlockState worldBlock, Entity entity);

}
