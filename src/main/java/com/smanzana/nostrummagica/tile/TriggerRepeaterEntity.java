package com.smanzana.nostrummagica.tile;

import java.util.ArrayList;
import java.util.List;

import com.smanzana.nostrummagica.block.ITriggeredBlock;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class TriggerRepeaterEntity extends TileEntity implements IOrientedTileEntity {
	
	private static final String NBT_OFFSET_LIST = "offsets";
	
	private List<BlockPos> offsets;
	
	public TriggerRepeaterEntity() {
		super(NostrumTileEntities.TriggerRepeaterTileEntityType);
		offsets = new ArrayList<>();
	}
	
	// Calculates the offset to the given pos and saves it
	public void addTriggerPoint(int triggerX, int triggerY, int triggerZ, boolean isWorldGen) {
		addTriggerPoint(new BlockPos(triggerX, triggerY, triggerZ), isWorldGen);
	}
	
	// Calculates the offset to the given pos and saves it
	public void addTriggerPoint(BlockPos triggerPoint, boolean isWorldGen) {
		this.addOffset(triggerPoint.subtract(this.getPos()), isWorldGen);
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
		return new SUpdateTileEntityPacket(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		return this.write(new CompoundNBT());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(this.getBlockState(), pkt.getNbtCompound());
	}
	
	public CompoundNBT write(CompoundNBT compound) {
		super.write(compound);
		
		ListNBT list = new ListNBT();
		for (BlockPos offset : offsets) {
			list.add(NBTUtil.writeBlockPos(offset));
		}
		compound.put(NBT_OFFSET_LIST, list);
		
		return compound;
	}
	
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		
		ListNBT list = compound.getList(NBT_OFFSET_LIST, NBT.TAG_COMPOUND);
		offsets.clear();
		for (int i = 0; i < list.size(); i++) {
			offsets.add(NBTUtil.readBlockPos(list.getCompound(i)));
		}
	}
	
	protected void flush(boolean isWorldGen) {
		if (!isWorldGen && world != null && !world.isRemote) {
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		if (!this.getOffsets().isEmpty()) {
			List<BlockPos> originalOffsets = new ArrayList<>(this.getOffsets());
			this.offsets.clear();
			
			for (BlockPos offset : originalOffsets) {
				BlockPos out = RoomBlueprint.applyRotation(offset, rotation);
				this.addOffset(out, isWorldGen);
			}
		}
	}
	
	public void trigger(BlockPos triggerSource) {
		for (BlockPos offset : this.offsets) {
			final BlockPos target = this.pos.add(offset);
			if (target.equals(triggerSource)) {
				continue;
			}
			
			BlockState state = world.getBlockState(target);
			if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
				return;
			}
			
			((ITriggeredBlock) state.getBlock()).trigger(world, target, state, this.getPos());
		}
	}
}