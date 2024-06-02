package com.smanzana.nostrummagica.tile;

import java.util.Objects;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.block.MimicBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.data.IModelData;
import net.minecraftforge.client.model.data.ModelDataMap;

public class MimicBlockTileEntity extends TileEntity {
	
	protected final MimicBlock.MimicBlockData data;
	
	protected MimicBlockTileEntity(TileEntityType<? extends MimicBlockTileEntity> type) {
		super(type);
		this.data = new MimicBlock.MimicBlockData();
	}
	
	public MimicBlockTileEntity() {
		this(NostrumTileEntities.MimicBlockTileEntityType);
	}

	public MimicBlock.MimicBlockData getData() {
		return data;
	}
	
	protected void setDataBlock(BlockState state) {
		this.data.mimicState = state;
	}
	
	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		
		if (this.getData().mimicState != null) {
			tag.put("nested_state", NBTUtil.writeBlockState(this.getData().mimicState));
		}
		
		return tag;
	}
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.pos, -1, this.getUpdateTag());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		this.handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	protected @Nonnull BlockState refreshState() {
		return ((MimicBlock) getBlockState().getBlock()).getMimickedState(getBlockState(), getWorld(), getPos());
	}
	
	@Override
	public void handleUpdateTag(BlockState state, CompoundNBT tag) {
		super.handleUpdateTag(state, tag);
		
		final BlockState newState;
		
		if (tag.contains("nested_state")) {
			newState = NBTUtil.readBlockState(tag.getCompound("nested_state"));
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
		if (!this.hasWorld() || this.world.isRemote()) {
			return;
		}
		
		BlockState newState = refreshState();
		if (!Objects.equals(newState, getData().getBlockState())) {
			setDataBlock(newState);
			signalBlockUpdate();
		}
	}
	
	protected void signalBlockUpdate() {
		// On client, request a render update
		// On server, send TE updates
		if (this.hasWorld() &&  !this.isRemoved()) {
			
			if (this.world.isRemote()) {
				this.requestModelDataUpdate();
				world.notifyBlockUpdate(getPos(), this.getBlockState(), this.getBlockState(), 11); // On client, rerenders. Server flushes data.
			} else {
				// I want to just do this, but I think sometimes it makes packets not fire since it doesn't look
				// like anything's changed.
				//world.notifyBlockUpdate(getPos(), this.getBlockState(), this.getBlockState(), 11); // On client, rerenders. Server flushes data.
				
				SUpdateTileEntityPacket supdatetileentitypacket = this.getUpdatePacket();
				for (ServerPlayerEntity player : ((ServerWorld) world).getPlayers()) {
					if (supdatetileentitypacket != null) {
						player.connection.sendPacket(supdatetileentitypacket);
					}
				}
			}
			
			// Update light values on server and client
			world.getChunkProvider().getLightManager().checkBlock(getPos());
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public IModelData getModelData() {
		// Adapted from SecretBases' code
		if(this.removed) {
			return super.getModelData();
		}
		ModelDataMap.Builder builder = new ModelDataMap.Builder()
				.withInitial(MimicBlock.MIMIC_MODEL_PROPERTY, this.data);
		return builder.build();
	}

}
