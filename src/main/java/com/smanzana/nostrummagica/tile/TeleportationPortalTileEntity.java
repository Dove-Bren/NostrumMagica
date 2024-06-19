package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class TeleportationPortalTileEntity extends PortalBlock.NostrumPortalTileEntityBase  {

	private static final String NBT_TARGET = "target_pos";
	
	private BlockPos target;
	
	protected TeleportationPortalTileEntity(TileEntityType<?> type) {
		super(type);
	}
	
	public TeleportationPortalTileEntity() {
		this(NostrumTileEntities.TeleportationPortalTileEntityType);
	}
	
	protected TeleportationPortalTileEntity(TileEntityType<?> type, BlockPos target) {
		this(type);
		this.setTarget(target);
	}
	
	public TeleportationPortalTileEntity(BlockPos target) {
		this(NostrumTileEntities.TeleportationPortalTileEntityType, target);
	}
	
	public @Nullable BlockPos getTarget() {
		return target;
	}
	
	public void setTarget(@Nullable BlockPos target) {
		this.target = target;
		this.markDirty();
		
		if (world != null) {
			BlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getRemainingCharge(player) > 0) {
			return 0x00A00050;
		}
		return 0x00500050;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 3;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getOpacity() {
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getRemainingCharge(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
	
	@Override
	public void read(BlockState state, CompoundNBT compound) {
		super.read(state, compound);
		
		if (compound.contains(NBT_TARGET, NBT.TAG_LONG)) {
			// Legacy!
			target = WorldUtil.blockPosFromLong1_12_2(compound.getLong(NBT_TARGET));
		} else if (compound.contains(NBT_TARGET)) {
			target = NBTUtil.readBlockPos(compound.getCompound(NBT_TARGET));
		} else {
			target = null;
		}
	}
	
	@Override
	public CompoundNBT write(CompoundNBT nbt) {
		nbt = super.write(nbt);
		
		if (target != null) {
			nbt.put(NBT_TARGET, NBTUtil.writeBlockPos(target));
		}
		
		return nbt;
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
	
}