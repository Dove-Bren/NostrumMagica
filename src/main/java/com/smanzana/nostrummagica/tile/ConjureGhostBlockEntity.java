package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.block.dungeon.ConjureGhostBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class ConjureGhostBlockEntity extends BlockEntity {
	
	protected BlockState mimicState;
	
	protected ConjureGhostBlockEntity(BlockEntityType<? extends ConjureGhostBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		mimicState = Blocks.STONE.defaultBlockState();
	}
	
	public ConjureGhostBlockEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.ConjureGhostBlock, pos, state);
	}
	
	public BlockState getGhostState() {
		return mimicState;
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		if (tag.isEmpty()) {
			tag = saveWithId(); // ID here to prevent empty which gets sent as null and crashes otehr side
		}
		
		return tag;
	}
	
	@Override
	public void saveAdditional(CompoundTag nbt) {
		super.saveAdditional(nbt);
		
		if (this.mimicState != null) {
			nbt.put("wrapped_state", NbtUtils.writeBlockState(mimicState));
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt != null && nbt.contains("wrapped_state")) {
			mimicState = NbtUtils.readBlockState(nbt.getCompound("wrapped_state"));
		}
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
	}
	
	@Override
	public void handleUpdateTag(CompoundTag tag) {
		super.handleUpdateTag(tag);
	}

	public void spawnBlock() {
		// Replace us with our stored block
		BlockState stateToSet = getGhostState();
		if (stateToSet == null) {
			stateToSet = Blocks.STONE.defaultBlockState();
		}
		
		this.getLevel().setBlock(worldPosition, stateToSet, Block.UPDATE_ALL);
	}

	public void setContainedState(BlockState state) {
		this.mimicState = state;
	}

	public boolean shouldShowHint() {
		// Show hint if no (conjure) blocks are below this one and a player is close enough
		if (this.hasLevel() && !(this.getLevel().getBlockState(worldPosition.below()).getBlock() instanceof ConjureGhostBlock)) {
			@Nullable Player nearest = getLevel().getNearestPlayer(worldPosition.getX() + .5, worldPosition.getY(), worldPosition.getZ() + .5, 3, false);
			return nearest != null;
		}
		return false;
	}
	
}
