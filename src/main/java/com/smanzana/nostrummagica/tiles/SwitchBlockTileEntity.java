package com.smanzana.nostrummagica.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.ITriggeredBlock;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.utils.WorldUtil;
import com.smanzana.nostrummagica.world.blueprints.IOrientedTileEntity;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;

public class SwitchBlockTileEntity extends TileEntity implements ITickableTileEntity, IOrientedTileEntity {
	
	public static enum SwitchType {
		ANY,
		MAGIC,
	}
	
	private SwitchBlockTileEntity.SwitchType type;
	private BlockPos triggerOffset;
	private boolean triggered;
	private LivingEntity triggerEntity;
	
	public SwitchBlockTileEntity() {
		super(NostrumTileEntities.SwitchBlockTileEntityType);
		
		type = SwitchType.ANY;
		triggerOffset = new BlockPos(0, -2, 0);
		triggerEntity = null;
		triggered = false;
	}
	
	public SwitchBlockTileEntity(SwitchBlockTileEntity.SwitchType type, BlockPos pos) {
		this();
		
		this.type = type;
		this.triggerOffset = pos;
	}
	
	private static final String NBT_TYPE = "switch_type";
	private static final String NBT_OFFSET = "switch_offset";
	private static final String NBT_TRIGGERED = "triggered";
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		nbt.putInt(NBT_TYPE, this.type.ordinal());
		nbt.put(NBT_OFFSET, NBTUtil.writeBlockPos(this.triggerOffset));
		nbt.putBoolean(NBT_TRIGGERED, this.triggered);
		
		return nbt;
	}
	
	@Override
	public void read(CompoundNBT nbt) {
		super.read(nbt);
		
		int ord = nbt.getInt(NBT_TYPE);
		for (SwitchBlockTileEntity.SwitchType type : SwitchType.values()) {
			if (type.ordinal() == ord) {
				this.type = type;
				break;
			}
		}
		
		if (nbt.contains(NBT_OFFSET, NBT.TAG_LONG)) {
			this.triggerOffset = WorldUtil.blockPosFromLong1_12_2(nbt.getLong(NBT_OFFSET));
		} else {
			this.triggerOffset = NBTUtil.readBlockPos(nbt.getCompound(NBT_OFFSET));
		}
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
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
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private void dirty() {
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		markDirty();
	}
	
	public void setType(SwitchBlockTileEntity.SwitchType type) {
		this.type = type;
		dirty();
	}
	
	public void setOffset(BlockPos newOffset) {
		setOffset(newOffset, false);
	}
	
	public void setOffset(BlockPos newOffset, boolean isWorldGen) {
		this.triggerOffset = newOffset.toImmutable();
		if (!isWorldGen) {
			dirty();
		}
	}
	
	public void offsetTo(BlockPos targ) {
		this.setOffset(targ.subtract(this.getPos()));
	}
	
	public SwitchBlockTileEntity.SwitchType getSwitchType() {
		return this.type;
	}
	
	public BlockPos getOffset() {
		return this.triggerOffset;
	}
	
	@Nullable
	public LivingEntity getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public boolean isTriggered() {
		return this.triggered;
	}
	
	@Override
	public void tick() {
		if (world.isRemote) {
			return;
		}
		
		// Create entity here if it doesn't exist
		BlockPos blockUp = pos.up();
		if (triggerEntity == null || !triggerEntity.isAlive() || triggerEntity.world != this.world
				|| triggerEntity.getDistanceSq(blockUp.getX() + .5, blockUp.getY(), blockUp.getZ() + .5) > 1.5) {
			// Entity is dead OR is too far away
			if (triggerEntity != null && !triggerEntity.isAlive()) {
				triggerEntity.remove();
			}
			
			triggerEntity = new EntitySwitchTrigger(NostrumEntityTypes.switchTrigger, this.world);
			triggerEntity.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
			world.addEntity(triggerEntity);
		}
	}
	
	public void trigger(boolean isMagic) {
		if (!this.triggered) {
			if (type == SwitchType.ANY || isMagic) {
				this.triggered = true;
				NostrumMagicaSounds.DAMAGE_ICE.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				this.dirty();
				
				BlockPos triggerPos = this.getPos().add(this.getOffset());
				BlockState state = world.getBlockState(triggerPos);
				if (state == null || !(state.getBlock() instanceof ITriggeredBlock)) {
					return;
				}
				
				((ITriggeredBlock) state.getBlock()).trigger(world, triggerPos, state, this.getPos());
			} else {
				// Wrong input type
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
			}
		}
	}

	@Override
	public void setSpawnedFromRotation(Direction rotation, boolean isWorldGen) {
		this.setOffset(RoomBlueprint.applyRotation(this.getOffset(), rotation), isWorldGen);
	}
}