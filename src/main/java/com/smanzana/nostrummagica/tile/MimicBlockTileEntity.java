package com.smanzana.nostrummagica.tile;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.block.dungeon.MimicBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

public class MimicBlockTileEntity extends BlockEntity {
	
	protected final MimicBlock.MimicBlockData data;
	
	protected MimicBlockTileEntity(BlockEntityType<? extends MimicBlockTileEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		this.data = new MimicBlock.MimicBlockData();
	}
	
	public MimicBlockTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.MimicBlock, pos, state);
	}

	public MimicBlock.MimicBlockData getData() {
		return data;
	}
	
	protected void setDataBlock(BlockState state) {
		this.data.mimicState = state;
	}
	
	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		if (tag.isEmpty()) {
			tag = saveWithId(); // ID here to prevent empty which gets sent as null and crashes otehr side
		}
		
		if (this.getData().mimicState != null) {
			tag.put("nested_state", NbtUtils.writeBlockState(this.getData().mimicState));
		}
		
		return tag;
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag compoundtag = pkt.getTag();
		if (compoundtag != null) {
			this.handleUpdateTag(compoundtag);
		}
	}
	
	protected @Nonnull BlockState refreshState() {
		return ((MimicBlock) getBlockState().getBlock()).getMimickedState(getBlockState(), getLevel(), getBlockPos());
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
		
		final BlockState newState;
		
		if (tag.contains("nested_state")) {
			newState = NbtUtils.readBlockState(tag.getCompound("nested_state"));
		} else {
			// Server told us something's changed (or we just loaded)
			newState = refreshState();
		}
		
		if (!Objects.equals(newState, getData().getBlockState())) {
			setDataBlock(newState);
			signalBlockUpdate(); // Is this okay?
		}
	}
	
	public void updateBlock() {
		// Something let us know the block we're mimicking has probably changed. Refresh.
		if (!this.hasLevel() || this.level.isClientSide()) {
			return;
		}
		
		BlockState newState = refreshState();
		if (!Objects.equals(newState, getData().getBlockState())) {
			setDataBlock(newState);
			signalBlockUpdate();
		}
	}
	
	@Override
	public void onLoad() {
		super.onLoad();
		// Trigger a refresh since we don't send update packets when tile entities are placed anymore
		this.updateBlock();
	}
	
	protected void signalBlockUpdate() {
		// On client, request a render update
		// On server, send TE updates
		if (this.hasLevel() &&  !this.isRemoved()) {
			
			if (this.level.isClientSide()) {
				this.requestModelDataUpdate();
				level.sendBlockUpdated(getBlockPos(), this.getBlockState(), this.getBlockState(), Block.UPDATE_ALL_IMMEDIATE); // On client, rerenders. Server flushes data.
			} else {
				// I want to just do this, but I think sometimes it makes packets not fire since it doesn't look
				// like anything's changed.
				//world.notifyBlockUpdate(getPos(), this.getBlockState(), this.getBlockState(), 11); // On client, rerenders. Server flushes data.
				
				ClientboundBlockEntityDataPacket supdatetileentitypacket = this.getUpdatePacket();
				for (ServerPlayer player : ((ServerLevel) level).players()) {
					if (supdatetileentitypacket != null) {
						player.connection.send(supdatetileentitypacket);
					}
				}
			}
			
			// Update light values on server and client
			level.getChunkSource().getLightEngine().checkBlock(getBlockPos());
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public IModelData getModelData() {
		// Adapted from SecretBases' code
		if(this.remove) {
			return super.getModelData();
		}
		ModelDataMap.Builder builder = new ModelDataMap.Builder()
				.withInitial(MimicBlock.MIMIC_MODEL_PROPERTY, this.data);
		return builder.build();
	}

}
