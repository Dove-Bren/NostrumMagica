package com.smanzana.nostrummagica.tile;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SorceryPortalTileEntity extends PortalBlock.NostrumPortalTileEntityBase  {
	
	public SorceryPortalTileEntity(BlockPos pos, BlockState state) {
		super(NostrumTileEntities.SorceryPortalTileEntityType, pos, state);
	}

	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		Player player = NostrumMagica.instance.proxy.getPlayer();
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
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (PortalBlock.getCooldownTime(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
}