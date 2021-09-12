package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SorceryPortalTileEntity extends NostrumPortal.NostrumPortalTileEntityBase  {

	@SideOnly(Side.CLIENT)
	@Override
	public int getColor() {
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getRemainingCharge(player) > 0) {
			return 0x00FF0050;
		}
		return 0x00C00050;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 6;
	}

	@SideOnly(Side.CLIENT)
	@Override
	public float getOpacity() {
		EntityPlayer player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getCooldownTime(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
}