package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.tile.IOrientedTileEntity;
import com.smanzana.autodungeons.world.blueprints.IBlueprint;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ITriggeredBlock;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.util.Constants.NBT;

public class TriggerRepeaterTileEntity extends TileEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET_LIST = "offsets";
	
	private List<BlockPos> offsets;
	
	protected TriggerRepeaterTileEntity(TileEntityType<? extends TriggerRepeaterTileEntity> type) {
		super(type);
		offsets = new ArrayList<>();
	}
	
	public TriggerRepeaterTileEntity() {
		this(NostrumTileEntities.TriggerRepeaterTileEntityType);
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
	
	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.save(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getTag());
	}
	
	public CompoundNBT save(CompoundNBT compound) {
		super.save(compound);
		
		ListNBT list = new ListNBT();
		for (BlockPos offset : offsets) {
			list.add(NBTUtil.writeBlockPos(offset));
		}
		compound.put(NBT_OFFSET_LIST, list);
		
		return compound;
	}
	
	public void load(BlockState state, CompoundNBT compound) {
		super.load(state, compound);
		
		ListNBT list = compound.getList(NBT_OFFSET_LIST, NBT.TAG_COMPOUND);
		offsets.clear();
		for (int i = 0; i < list.size(); i++) {
			offsets.add(NBTUtil.readBlockPos(list.getCompound(i)));
		}
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
	
	public int cleanOffests(@Nullable PlayerEntity feedbackPlayer) {
		Iterator<BlockPos> it = this.offsets.iterator();
		int count = 0;
		while (it.hasNext()) {
			BlockPos offset = it.next();
			final BlockPos target = this.worldPosition.offset(offset);
			final BlockState state = level.getBlockState(target);
			if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
				if (feedbackPlayer != null) {
					feedbackPlayer.sendMessage(new StringTextComponent("Cleaning out offset " + offset), Util.NIL_UUID);
				}
				it.remove();
				count++;
			}
		}
		return count;
	}
}