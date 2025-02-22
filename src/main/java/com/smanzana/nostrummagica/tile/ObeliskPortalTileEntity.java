package com.smanzana.nostrummagica.tile;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.PortalBlock;
import com.smanzana.nostrummagica.util.Location;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ObeliskPortalTileEntity extends TeleportationPortalTileEntity {
	
	public ObeliskPortalTileEntity() {
		super(NostrumTileEntities.ObeliskPortalTileEntityType);
	}
	
	@Override
	public @Nullable Location getTarget() {
		// Defer to obelisk below us
		TileEntity te = world.getTileEntity(pos.down());
		if (te != null && te instanceof ObeliskTileEntity) {
			Location target = ((ObeliskTileEntity) te).getCurrentTarget();
			if (target != null) {
				target = new Location(target.getPos().up(), target.getDimension()); // we 'target' the actual obelisk but don't want to tele them there!
			}
			return target;
		}
		
		return null;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public int getColor() {
		TileEntity te = world.getTileEntity(pos.down());
		if (te != null && te instanceof ObeliskTileEntity && ((ObeliskTileEntity) te).hasOverride()) {
			return 0x0000FF50;
		}
		return 0x004000FF;
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
		if (PortalBlock.getCooldownTime(player) > 0) {
			return 0.5f;
		}
		return .9f;
	}
}