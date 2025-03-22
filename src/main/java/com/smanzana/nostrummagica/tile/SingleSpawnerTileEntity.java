package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.block.dungeon.SingleSpawnerBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SingleSpawnerTileEntity extends BlockEntity implements TickableBlockEntity {
	
	protected int ticksExisted;
	
	public SingleSpawnerTileEntity(BlockPos pos, BlockState state) {
		this(NostrumTileEntities.SingleSpawnerTileEntityType, pos, state);
	}
	
	protected SingleSpawnerTileEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		ticksExisted = 0;
	}
	
	// Only call on server
	protected void majorTick(BlockState state) {
		NostrumBlocks.singleSpawner.tick(state, (ServerLevel) level, worldPosition, NostrumMagica.rand);
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