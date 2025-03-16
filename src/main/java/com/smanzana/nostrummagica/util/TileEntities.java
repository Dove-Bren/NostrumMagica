package com.smanzana.nostrummagica.util;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

public class TileEntities {

	public static final void RefreshToClients(BlockEntity te) {
		final BlockPos pos = te.getBlockPos();
		final Level world = te.getLevel();
		
		if (!world.isClientSide()) {
			world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			te.setChanged();
		}
	}
	
}
