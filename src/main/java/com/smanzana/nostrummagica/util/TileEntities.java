package com.smanzana.nostrummagica.util;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class TileEntities {

	public static final void RefreshToClients(TileEntity te) {
		final BlockPos pos = te.getPos();
		final World world = te.getWorld();
		
		if (!world.isRemote()) {
			world.notifyBlockUpdate(pos, world.getBlockState(pos), world.getBlockState(pos), 3);
			te.markDirty();
		}
	}
	
}
