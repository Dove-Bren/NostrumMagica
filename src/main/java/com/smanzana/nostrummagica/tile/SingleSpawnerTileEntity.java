package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;

import net.minecraft.block.BlockState;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.server.ServerWorld;

public class SingleSpawnerTileEntity extends TileEntity implements ITickableTileEntity {
	
	protected int ticksExisted;
	
	public SingleSpawnerTileEntity() {
		this(NostrumTileEntities.SingleSpawnerTileEntityType);
	}
	
	protected SingleSpawnerTileEntity(TileEntityType<?> type) {
		super(type);
		ticksExisted = 0;
	}
	
	// Only call on server
	protected void majorTick(BlockState state) {
		NostrumBlocks.singleSpawner.tick(state, (ServerWorld) world, pos, NostrumMagica.rand);
	}
	
	@Override
	public void tick() {
		if (!world.isRemote && ++ticksExisted % 32 == 0) {
			BlockState state = this.world.getBlockState(this.pos);
			if (state == null || !(state.getBlock() instanceof SingleSpawnerBlock)) {
				world.removeTileEntity(pos);
				return;
			}
			
			majorTick(state);
		}
	}
}