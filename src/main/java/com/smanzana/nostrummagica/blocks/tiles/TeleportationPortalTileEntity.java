package com.smanzana.nostrummagica.blocks.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SPacketUpdateTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TeleportationPortalTileEntity extends NostrumPortal.NostrumPortalTileEntityBase  {

	private static final String NBT_TARGET = "target_pos";
	
	private BlockPos target;
	
	public TeleportationPortalTileEntity() {
		super();
	}
	
	public TeleportationPortalTileEntity(BlockPos target) {
		this();
		this.setTarget(target);
	}
	
	public @Nullable BlockPos getTarget() {
		return target;
	}
	
	public void setTarget(@Nullable BlockPos target) {
		this.target = target;
		this.markDirty();
		
		if (world != null) {
			IBlockState state = world.getBlockState(pos);
			world.notifyBlockUpdate(pos, state, state, 2);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getColor() {
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getRemainingCharge(player) > 0) {
			return 0x00A00050;
		}
		return 0x00500050;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 3;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getOpacity() {
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getRemainingCharge(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		if (compound.hasKey(NBT_TARGET, NBT.TAG_LONG)) {
			target = BlockPos.fromLong(compound.getLong(NBT_TARGET));
		} else {
			target = null;
		}
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		if (target != null) {
			nbt.setLong(NBT_TARGET, target.toLong());
		}
		
		return nbt;
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
	
}