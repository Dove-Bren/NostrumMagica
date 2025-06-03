package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.BlueprintBlock;
import com.smanzana.nostrummagica.block.dungeon.ConjureGhostBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

public class ConjureGhostBlockEntity extends BlockEntity implements IOrientedTileEntity {
	
	protected BlockState mimicState;
	protected @Nullable CompoundTag tileEntityData;
	protected @Nullable Direction facingOffset;
	
	protected ConjureGhostBlockEntity(BlockEntityType<? extends ConjureGhostBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		mimicState = Blocks.STONE.defaultBlockState();
		tileEntityData = null;
		facingOffset = null;
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
		if (this.tileEntityData != null) {
			nbt.put("wrapped_te_data", this.tileEntityData);
		}
		if (this.facingOffset != null) {
			nbt.putString("facing_offset", this.facingOffset.getName());
		}
	}
	
	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);
		
		if (nbt == null) {
			return;
		}
		
		if (nbt.contains("wrapped_state")) {
			mimicState = NbtUtils.readBlockState(nbt.getCompound("wrapped_state"));
		}
		if (mimicState != null) {
			if (nbt.contains("wrapped_te_data")) {
				// only bother if we have a state. Otherwise clean out here
				this.tileEntityData = nbt.getCompound("wrapped_te_data");
			}
			if (nbt.contains("facing_offset")) {
				this.facingOffset = Direction.byName(nbt.getString("facing_offset"));
			}
		} else {
			this.tileEntityData = null;
			this.facingOffset = null;
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
		
		
		if (this.tileEntityData != null) {
			BlockEntity ent = BlockEntity.loadStatic(worldPosition, stateToSet, tileEntityData);
			if (ent != null) {
				level.setBlockEntity(ent);
				if (this.facingOffset != null && ent instanceof IOrientedTileEntity oriented) {
					oriented.setSpawnedFromRotation(facingOffset, false);
				}
			}
		}
	}

	public void setContainedState(BlockState state, @Nullable BlockEntity entity) {
		this.setContainedState(state, entity == null ? null : entity.saveWithId());
	}

	public void setContainedState(BlockState state, @Nullable CompoundTag entityData) {
		this.mimicState = state;
		this.tileEntityData = state == null ? null : entityData; // force null if no blockstate
	}
	
	public boolean shouldShowHint() {
		// Show hint if no (conjure) blocks are below this one and a player is close enough
		if (this.hasLevel() && !(this.getLevel().getBlockState(worldPosition.below()).getBlock() instanceof ConjureGhostBlock)) {
			@Nullable Player nearest = getLevel().getNearestPlayer(worldPosition.getX() + .5, worldPosition.getY(), worldPosition.getZ() + .5, 3, false);
			return nearest != null;
		}
		return false;
	}

	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		// Capture rotation and use it later when we spawn our te
		this.facingOffset = rotation;
		
		// Transform block state for model update
		BlueprintBlock b = BlueprintBlock.getBlueprintBlock(getGhostState(), null);
		this.mimicState = b.getSpawnState(rotation);
		
		if (!isWorldGen) {
			this.setChanged();
		}
	}
	
}
