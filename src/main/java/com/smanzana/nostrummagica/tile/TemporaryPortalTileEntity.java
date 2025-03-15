package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.util.Location;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class TemporaryPortalTileEntity extends TeleportationPortalTileEntity implements ITickableTileEntity {

	private long endticks;
	
	public TemporaryPortalTileEntity() {
		super(NostrumTileEntities.TemporaryPortalTileEntityType);
	}
	
	public TemporaryPortalTileEntity(Location target, long endticks) {
		super(NostrumTileEntities.TemporaryPortalTileEntityType, target);
		this.endticks = endticks;
		this.setChanged();
	}
	
	@Override
	public void tick() {
		if (level == null || level.isClientSide) {
			return;
		}
		
		if (level.getGameTime() >= this.endticks) {
			level.removeBlock(worldPosition, false);
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getRemainingCharge(player) > 0) {
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
		
		if (level != null) {
			final long now =  level.getGameTime();
			final long FadeTicks = 20 * 5;
			final long left = Math.max(0, endticks - now);
			if (left < FadeTicks) {
				opacity *= ((double) left / (double) FadeTicks);
			}
		}
		
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getCooldownTime(player) > 0) {
			opacity *= 0.5f;
		}
		
		return opacity;
	}
	
	@Override
	public void load(BlockState state, CompoundNBT compound) {
		super.load(state, compound);
		
		endticks = compound.getLong("EXPIRE");
	}
	
	@Override
	public CompoundNBT save(CompoundNBT nbt) {
		nbt = super.save(nbt);
		
		nbt.putLong("EXPIRE", endticks);
		
		return nbt;
	}
}