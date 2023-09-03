package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumSingleSpawner;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;

public class SingleSpawnerTileEntity extends TileEntity implements ITickableTileEntity {
	
	protected int ticksExisted;
	
	public SingleSpawnerTileEntity() {
		ticksExisted = 0;
	}
	
	protected void majorTick(BlockState state) {
		NostrumSingleSpawner.instance().updateTick(world, pos, state, NostrumMagica.rand);
	}
	
	@Override
	public void tick() {
		if (!world.isRemote && ++ticksExisted % 32 == 0) {
			BlockState state = this.world.getBlockState(this.pos);
			if (state == null || !(state.getBlock() instanceof NostrumSingleSpawner)) {
				world.removeTileEntity(pos);
				return;
			}
			
			majorTick(state);
		}
	}
}