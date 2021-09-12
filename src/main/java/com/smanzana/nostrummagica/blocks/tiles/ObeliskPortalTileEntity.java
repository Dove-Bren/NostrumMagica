package com.smanzana.nostrummagica.blocks.tiles;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumPortal;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ObeliskPortalTileEntity extends TeleportationPortalTileEntity {
	
	@Override
	public @Nullable BlockPos getTarget() {
		// Defer to obelisk below us
		TileEntity te = world.getTileEntity(pos.down());
		if (te != null && te instanceof NostrumObeliskEntity) {
			BlockPos target = ((NostrumObeliskEntity) te).getCurrentTarget();
			if (target != null) {
				target = target.up(); // we 'target' the actual obelisk but don't want to tele them there!
			}
			return target;
		}
		
		return null;
	}
	
	@SideOnly(Side.CLIENT)
	@Override
	public int getColor() {
		TileEntity te = world.getTileEntity(pos.down());
		if (te != null && te instanceof NostrumObeliskEntity && ((NostrumObeliskEntity) te).hasOverride()) {
			return 0x0000FF50;
		}
		return 0x004000FF;
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
		if (NostrumPortal.getCooldownTime(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
}