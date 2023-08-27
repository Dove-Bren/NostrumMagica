package com.smanzana.nostrummagica.blocks.tiles;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SorceryPortalTileEntity extends NostrumPortal.NostrumPortalTileEntityBase  {

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		PlayerEntity player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getRemainingCharge(player) > 0) {
			return 0x00FF0050;
		}
		return 0x00C00050;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getRotationPeriod() {
		return 6;
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public float getOpacity() {
		PlayerEntity player = NostrumMagica.proxy.getPlayer();
		if (NostrumPortal.getCooldownTime(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
}