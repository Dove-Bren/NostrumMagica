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
		NostrumBlocks.singleSpawner.tick(state, (ServerWorld) level, worldPosition, NostrumMagica.rand);
	}
	
	@Override
	public void tick() {
		if (!level.isClientSide && ++ticksExisted % 32 == 0) {
			BlockState state = this.level.getBlockState(this.worldPosition);
			if (state == null || !(state.getBlock() instanceof SingleSpawnerBlock)) {
				level.removeBlockEntity(worldPosition);
				return;
			}
			
			majorTick(state);
		}
	}
}