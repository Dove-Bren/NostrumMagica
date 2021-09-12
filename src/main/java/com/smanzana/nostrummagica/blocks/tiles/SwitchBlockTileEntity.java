package com.smanzana.nostrummagica.blocks.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.blocks.ITriggeredBlock;
import com.smanzana.nostrummagica.entity.EntitySwitchTrigger;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ITickable;
import net.minecraft.util.math.BlockPos;

public class SwitchBlockTileEntity extends TileEntity implements ITickable {
	
	public static enum SwitchType {
		ANY,
		MAGIC,
	}
	
	private SwitchBlockTileEntity.SwitchType type;
	private BlockPos triggerOffset;
	private boolean triggered;
	private EntityLivingBase triggerEntity;
	
	public SwitchBlockTileEntity() {
		super();
		
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
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.setInteger(NBT_TYPE, this.type.ordinal());
		nbt.setLong(NBT_OFFSET, this.triggerOffset.toLong());
		nbt.setBoolean(NBT_TRIGGERED, this.triggered);
		
		return nbt;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		
		int ord = nbt.getInteger(NBT_TYPE);
		for (SwitchBlockTileEntity.SwitchType type : SwitchType.values()) {
			if (type.ordinal() == ord) {
				this.type = type;
				break;
			}
		}
		this.triggerOffset = BlockPos.fromLong(nbt.getLong(NBT_OFFSET));
		this.triggered = nbt.getBoolean(NBT_TRIGGERED);
	}
	
	@Override
	public SPacketUpdateTileEntity getUpdatePacket() {
		return new SPacketUpdateTileEntity(this.pos, 3, this.getUpdateTag());
	}

	@Override
	public NBTTagCompound getUpdateTag() {
		return this.writeToNBT(new NBTTagCompound());
	}
	
	@Override
	public void onDataPacket(NetworkManager net, SPacketUpdateTileEntity pkt) {
		super.onDataPacket(net, pkt);
		handleUpdateTag(pkt.getNbtCompound());
	}
	
	private void dirty() {
		world.markBlockRangeForRenderUpdate(pos, pos);
		world.notifyBlockUpdate(pos, this.world.getBlockState(pos), this.world.getBlockState(pos), 3);
		world.scheduleBlockUpdate(pos, this.getBlockType(),0,0);
		markDirty();
	}
	
	public void setType(SwitchBlockTileEntity.SwitchType type) {
		this.type = type;
		dirty();
	}
	
	public void setOffset(BlockPos newOffset) {
		this.triggerOffset = newOffset.toImmutable();
		dirty();
	}
	
	public void offsetTo(BlockPos targ) {
		this.setOffset(targ.subtract(this.getPos()));
	}
	
	public SwitchBlockTileEntity.SwitchType getType() {
		return this.type;
	}
	
	public BlockPos getOffset() {
		return this.triggerOffset;
	}
	
	@Nullable
	public EntityLivingBase getTriggerEntity() {
		return this.triggerEntity;
	}
	
	public boolean isTriggered() {
		return this.triggered;
	}
	
	@Override
	public void update() {
		if (world.isRemote) {
			return;
		}
		
		// Create entity here if it doesn't exist
		if (triggerEntity == null || triggerEntity.isDead || triggerEntity.world != this.world
				|| triggerEntity.getDistanceSq(this.pos.up()) > 1.5) {
			// Entity is dead OR is too far away
			if (triggerEntity != null && !triggerEntity.isDead) {
				triggerEntity.setDead();
			}
			
			triggerEntity = new EntitySwitchTrigger(this.world);
			triggerEntity.setPosition(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
			world.spawnEntity(triggerEntity);
		}
	}
	
	public void trigger(boolean isMagic) {
		if (!this.triggered) {
			if (type == SwitchType.ANY || isMagic) {
				this.triggered = true;
				NostrumMagicaSounds.DAMAGE_ICE.play(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				this.dirty();
				
				BlockPos triggerPos = this.getPos().add(this.getOffset());
				IBlockState state = world.getBlockState(triggerPos);
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
}