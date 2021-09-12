package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.tiles.AltarTileEntity;
import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;
import com.smanzana.nostrummagica.blocks.TemporaryTeleportationPortal;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeTeleportObelisk implements IRitualOutcome {

	public OutcomeTeleportObelisk() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Teleport the player to the obelisk pointed at by the center item
		// Must have magic unlocked, maybe?
		
		// Put the geogem back on the altar
		( (AltarTileEntity) world.getTileEntity(center)).setItem(centerItem);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (ModConfig.config.obeliskReqMagic() && (attr == null || !attr.isUnlocked()))
			return;
		
		if (centerItem == null || !(centerItem.getItem() instanceof PositionCrystal))
			return;
		
		BlockPos pos = PositionCrystal.getBlockPosition(centerItem);
		if (pos == null) {
			player.sendMessage(new TextComponentTranslation("info.teleport_obelisk.fail", new Object[0]));
			return;
		}
		
		if (!world.isRemote) {
			if (attr.hasEnhancedTeleport()) {
				TileEntity te = world.getTileEntity(pos);
				if (te == null || !(te instanceof NostrumObeliskEntity)) {
					NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
					player.sendMessage(new TextComponentTranslation("info.teleport_obelisk.fail"));
					return;
				}
				
				NostrumObeliskEntity obelisk = (NostrumObeliskEntity) te;
				BlockPos portal = TemporaryTeleportationPortal.spawnNearby(world, center.up(), 4, true, pos.up(), 20 * 30);
				if (portal != null) {
					obelisk.setOverride(portal, 20 * 30);
				}
			} else {
				ObeliskTeleportationRequestMessage.serverDoRequest(world, player, null, pos);
			}
			
			if (NostrumMagica.rand.nextInt(10) == 0) {
				float dist = 2 + NostrumMagica.rand.nextFloat() * 2;
				float dir = NostrumMagica.rand.nextFloat();
				double dirD = dir * 2 * Math.PI;
				double dx = Math.cos(dirD) * dist;
				double dz = Math.sin(dirD) * dist;
				EntityItem drop = new EntityItem(world, pos.getX() + .5 + dx, pos.getY() + 2, pos.getZ() + .5 + dz,
						NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1));
				world.spawnEntity(drop);
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
			}
		}
		
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.teleport_obelisk.desc",
				new Object[0])
				.split("\\|"));
	}
}
