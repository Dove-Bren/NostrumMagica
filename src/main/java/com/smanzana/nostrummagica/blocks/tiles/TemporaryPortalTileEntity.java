package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

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
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		PlayerEntity player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getRemainingCharge(player) > 0) {
			return 0x00400000;
		}
		return 0x003030FF;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 2;
	}

	@OnlyIn(Dist.CLIENT)
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
		
		PlayerEntity player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getCooldownTime(player) > 0) {
			opacity *= 0.5f;
		}
		
		return opacity;
	}
	
	@Override
	public void readFromNBT(CompoundNBT compound) {
		super.readFromNBT(compound);
		
		endticks = compound.getLong("EXPIRE");
	}
	
	@Override
	public CompoundNBT writeToNBT(CompoundNBT nbt) {
		nbt = super.writeToNBT(nbt);
		
		nbt.putLong("EXPIRE", endticks);
		
		return nbt;
	}
}