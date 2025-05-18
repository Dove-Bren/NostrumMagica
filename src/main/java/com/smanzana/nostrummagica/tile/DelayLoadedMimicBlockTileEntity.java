package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.block.dungeon.MimicBlock.MimicBlockData;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Hacky specialization that notices when it's not mimicking anything (air) and ticks to try and find something
 * @author Skyler
 *
 */
public class DelayLoadedMimicBlockTileEntity extends MimicBlockTileEntity implements TickableBlockEntity {
	
	protected boolean loading;
	protected int loadingTicks;
	
	protected DelayLoadedMimicBlockTileEntity(BlockEntityType<? extends DelayLoadedMimicBlockTileEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.loading = true;
	}
	
	public DelayLoadedMimicBlockTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.DelayedMimicBlock, pos, state);
		this.loading = true;
	}
	
	protected boolean successfullyLoaded() {
		MimicBlockData data = this.getData();
		return data != null && data.mimicState != null && !data.mimicState.isAir();
	}
	
	@Override
	public void tick() {
		if (!level.isClientSide() && loading && loadingTicks++ % 20 == 19) {
			// Attempt to load again
			updateBlock();
			if (successfullyLoaded()) {
				this.loading = false;
			}
		}
	}

}
