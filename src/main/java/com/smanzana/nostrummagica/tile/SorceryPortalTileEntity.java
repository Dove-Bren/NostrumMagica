package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SorceryPortalTileEntity extends PortalBlock.NostrumPortalTileEntityBase  {
	
	public SorceryPortalTileEntity() {
		super(NostrumTileEntities.SorceryPortalTileEntityType);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getRemainingCharge(player) > 0) {
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
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getCooldownTime(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
}