package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TemporaryPortalTileEntity extends TeleportationPortalTileEntity implements ITickableTileEntity {

	private long endticks;
	
	public TemporaryPortalTileEntity() {
		super();
	}
	
	public TemporaryPortalTileEntity(BlockPos target, long endticks) {
		super(target);
		this.endticks = endticks;
		this.markDirty();
	}
	
	@Override
	public void update() {
		if (world == null || world.isRemote) {
			return;
		}
		
		if (world.getTotalWorldTime() >= this.endticks) {
			world.setBlockToAir(pos);
		}
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getColor() {
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getRemainingCharge(player) > 0) {
			return 0x00400000;
		}
		return 0x003030FF;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 2;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getOpacity() {
		float opacity = .9f;
		
		if (world != null) {
			final long now =  world.getTotalWorldTime();
			final long FadeTicks = 20 * 5;
			final long left = Math.max(0, endticks - now);
			if (left < FadeTicks) {
				opacity *= ((double) left / (double) FadeTicks);
			}
		}
		
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getCooldownTime(player) > 0) {
			opacity *= 0.5f;
		}
		
		return opacity;
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		super.readFromNBT(compound);
		
		endticks = compound.getLong("EXPIRE");
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.setLong("EXPIRE", endticks);
		
		return nbt;
	}
}