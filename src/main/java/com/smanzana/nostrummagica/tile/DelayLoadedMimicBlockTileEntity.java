package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.block.dungeon.MimicBlock.MimicBlockData;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Hacky specialization that notices when it's not mimicking anything (air) and ticks to try and find something
 * @author Skyler
 *
 */
public class DelayLoadedMimicBlockTileEntity extends MimicBlockTileEntity implements ITickableTileEntity {
	
	protected boolean loading;
	protected int loadingTicks;
	
	protected DelayLoadedMimicBlockTileEntity(TileEntityType<? extends DelayLoadedMimicBlockTileEntity> type) {
		super(type);
		this.loading = true;
	}
	
	public DelayLoadedMimicBlockTileEntity() {
		this(NostrumTileEntities.DelayedMimicBlockTileEntityType);
		this.loading = true;
	}
	
	@SuppressWarnings("deprecation")
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
