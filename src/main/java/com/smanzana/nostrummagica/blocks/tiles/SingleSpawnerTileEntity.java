package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;

import net.minecraft.block.state.IBlockState;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;

public class SingleSpawnerTileEntity extends TileEntity implements ITickable {
	
	protected int ticksExisted;
	
	public SingleSpawnerTileEntity() {
		ticksExisted = 0;
	}
	
	protected void majorTick(IBlockState state) {
		NostrumSingleSpawner.instance().updateTick(world, pos, state, NostrumMagica.rand);
	}
	
	@Override
	public void update() {
		if (!world.isRemote && ++ticksExisted % 32 == 0) {
			IBlockState state = this.world.getBlockState(this.pos);
			if (state == null || !(state.getBlock() instanceof NostrumSingleSpawner)) {
				world.removeTileEntity(pos);
				return;
			}
			
			majorTick(state);
		}
	}
}