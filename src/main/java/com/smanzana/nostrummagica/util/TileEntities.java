package com.smanzana.nostrummagica.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntities {

	public static final void RefreshToClients(TileEntity te) {
		final BlockPos pos = te.getBlockPos();
		final World world = te.getLevel();
		
		if (!world.isClientSide()) {
			world.sendBlockUpdated(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			te.setChanged();
		}
	}
	
}
