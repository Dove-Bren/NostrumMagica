package com.smanzana.nostrummagica.tiles;

import com.smanzana.nostrummagica.blocks.MimicBlock;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;

public class MimicBlockTileEntity extends TileEntity {
	
	protected final MimicBlock.MimicBlockData data;
	
	public MimicBlockTileEntity() {
		super(NostrumTileEntities.MimicBlockTileEntityType);
		this.data = new MimicBlock.MimicBlockData();
	}

	public MimicBlock.MimicBlockData getData() {
		return data;
	}
	
	protected void setDataBlock(BlockState state) {
		this.data.mimicState = state;
	}
	
	@Override
	public void handleUpdateTag(CompoundNBT tag) {
		super.handleUpdateTag(tag);
		
		signalBlockUpdate(); // Is this okay?
	}
	
	public void updateBlock() {
		// Something let us know the block we're mimicking has probably changed. Refresh.
		if (!this.hasWorld() || this.world.isRemote()) {
			return;
		}
		
		BlockState newState = MimicBlock.GetMimickedState(getWorld(), getPos());
		if (!newState.equals(getData().getBlockState())) {
			setDataBlock(newState);
			signalBlockUpdate();
		}
	}
	
	protected void signalBlockUpdate() {
		// On client, request a render update
		// On server, send TE updates
		if (this.hasWorld() &&  !this.isRemoved()) {
			final MimicBlock.MimicBlockData blockData = getData();
			world.notifyBlockUpdate(getPos(), blockData.getBlockState(), blockData.getBlockState(), 11); // On client, rerenders. Server flushes data.
			
			if (this.world.isRemote()) {
				this.requestModelDataUpdate();
			}
			
			// Update light values on server and client
			world.getChunkProvider().getLightManager().checkBlock(getPos());
		}
	}

}
