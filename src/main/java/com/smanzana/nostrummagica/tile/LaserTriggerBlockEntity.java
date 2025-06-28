package com.smanzana.nostrummagica.tile;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class LaserTriggerBlockEntity extends BlockEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET = "offset";
	
	private BlockPos offset;
	
	protected LaserTriggerBlockEntity(BlockEntityType<? extends LaserTriggerBlockEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		offset = new BlockPos(0, 2, 0);
	}
	
	public LaserTriggerBlockEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.LaserTrigger, pos, state);
	}
	
	// Calculates the offset to the given pos and saves it
	public void setTriggerPoint(int triggerX, int triggerY, int triggerZ, boolean isWorldGen) {
		addTriggerPoint(new BlockPos(triggerX, triggerY, triggerZ), isWorldGen);
	}
	
	// Calculates the offset to the given pos and saves it
	public void addTriggerPoint(BlockPos triggerPoint, boolean isWorldGen) {
		this.setOffset(triggerPoint.subtract(this.getBlockPos()), isWorldGen);
	}
	
	public void setOffset(BlockPos offset, boolean isWorldGen) {
		this.offset = offset.immutable();
		flush(isWorldGen);
	}
	
	public BlockPos getOffset() {
		return offset;
	}
	
	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		return this.saveWithoutMetadata();
	}
	
	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		super.onDataPacket(net, pkt);
	}
	
	@Override
	public void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		
		compound.put(NBT_OFFSET, NbtUtils.writeBlockPos(offset));
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		this.offset = NbtUtils.readBlockPos(compound.getCompound(NBT_OFFSET));
	}
	
	protected void flush(boolean isWorldGen) {
		if (!isWorldGen && level != null && !level.isClientSide) {
			this.setChanged();
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		this.setOffset(IBlueprint.ApplyRotation(offset, rotation), isWorldGen);
	}
	
	public void trigger(BlockPos triggerSource) {
		final BlockPos target = this.worldPosition.offset(offset);
		if (target.equals(triggerSource)) {
			return;
		}
		
		BlockState state = level.getBlockState(target);
		if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
			NostrumMagica.logger.debug("Non-triggerable block pointed to at " + target);
			return;
		}
		
		((ITriggeredBlock) state.getBlock()).trigger(level, target, state, this.getBlockPos());
	}
}