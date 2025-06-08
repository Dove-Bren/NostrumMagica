package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.api.block.entity.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class TriggerRepeaterTileEntity extends BlockEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET_LIST = "offsets";
	private static final String NBT_TRIGGER_COUNT = "trigger_count";
	private static final String NBT_TRIGGER_REQUIREMENT = "trigger_requirement";
	
	private List<BlockPos> offsets;
	private int triggerRequirement;
	private int triggerCount;
	
	protected TriggerRepeaterTileEntity(BlockEntityType<? extends TriggerRepeaterTileEntity> type, BlockPos pos, BlockState state) {
		super(type, pos, state);
		offsets = new ArrayList<>();
		triggerRequirement = 1;
		triggerCount = 0;
	}
	
	public TriggerRepeaterTileEntity(BlockPos pos, BlockState state) {
		this(NostrumBlockEntities.TriggerRepeater, pos, state);
	}
	
	// Calculates the offset to the given pos and saves it
	public void addTriggerPoint(int triggerX, int triggerY, int triggerZ, boolean isWorldGen) {
		addTriggerPoint(new BlockPos(triggerX, triggerY, triggerZ), isWorldGen);
	}
	
	// Calculates the offset to the given pos and saves it
	public void addTriggerPoint(BlockPos triggerPoint, boolean isWorldGen) {
		this.addOffset(triggerPoint.subtract(this.getBlockPos()), isWorldGen);
	}
	
	public void addOffset(BlockPos offset, boolean isWorldGen) {
		offsets.add(offset);
		flush(isWorldGen);
	}
	
	public List<BlockPos> getOffsets() {
		return offsets;
	}
	
	public void setTriggerRequirement(int count, boolean isWorldGen) {
		this.triggerRequirement = Math.max(1, count);
		flush(isWorldGen);
	}
	
	public int getTriggerRequirement() {
		return this.triggerRequirement;
	}
	
	public int getCurrentTriggerCount() {
		return this.triggerCount;
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
		
		ListTag list = new ListTag();
		for (BlockPos offset : offsets) {
			list.add(NbtUtils.writeBlockPos(offset));
		}
		compound.put(NBT_OFFSET_LIST, list);
		compound.putInt(NBT_TRIGGER_COUNT, triggerCount);
		compound.putInt(NBT_TRIGGER_REQUIREMENT, triggerRequirement);
	}
	
	@Override
	public void load(CompoundTag compound) {
		super.load(compound);
		
		ListTag list = compound.getList(NBT_OFFSET_LIST, Tag.TAG_COMPOUND);
		offsets.clear();
		for (int i = 0; i < list.size(); i++) {
			offsets.add(NbtUtils.readBlockPos(list.getCompound(i)));
		}
		this.triggerCount = compound.getInt(NBT_TRIGGER_COUNT);
		this.triggerRequirement = Math.max(1, compound.getInt(NBT_TRIGGER_REQUIREMENT));
	}
	
	protected void flush(boolean isWorldGen) {
		if (!isWorldGen && level != null && !level.isClientSide) {
			BlockState state = level.getBlockState(worldPosition);
			level.sendBlockUpdated(worldPosition, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		if (!this.getOffsets().isEmpty()) {
			List<BlockPos> originalOffsets = new ArrayList<>(this.getOffsets());
			this.offsets.clear();
			
			for (BlockPos offset : originalOffsets) {
				BlockPos out = IBlueprint.ApplyRotation(offset, rotation);
				this.addOffset(out, isWorldGen);
			}
		}
	}
	
	public void trigger(BlockPos triggerSource) {
		this.triggerCount++;
		if (this.triggerCount >= this.triggerRequirement) {
			this.triggerCount = 0;
			for (BlockPos offset : this.offsets) {
				final BlockPos target = this.worldPosition.offset(offset);
				if (target.equals(triggerSource)) {
					continue;
				}
				
				BlockState state = level.getBlockState(target);
				if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
					NostrumMagica.logger.debug("Non-triggerable block pointed to at " + target);
					continue;
				}
				
				((ITriggeredBlock) state.getBlock()).trigger(level, target, state, this.getBlockPos());
			}
		}
	}
	
	public int cleanOffests(@Nullable Player feedbackPlayer) {
		Iterator<BlockPos> it = this.offsets.iterator();
		int count = 0;
		while (it.hasNext()) {
			BlockPos offset = it.next();
			final BlockPos target = this.worldPosition.offset(offset);
			final BlockState state = level.getBlockState(target);
			if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
				if (feedbackPlayer != null) {
					feedbackPlayer.sendMessage(new TextComponent("Cleaning out offset " + offset), Util.NIL_UUID);
				}
				it.remove();
				count++;
			}
		}
		return count;
	}
}