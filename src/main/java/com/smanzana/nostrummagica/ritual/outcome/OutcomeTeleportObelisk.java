package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.TemporaryTeleportationPortalBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.tile.ObeliskTileEntity;
import com.smanzana.nostrummagica.util.Location;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;

public class OutcomeTeleportObelisk implements IRitualOutcome {

	public OutcomeTeleportObelisk() {
		;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Teleport the player to the obelisk pointed at by the center item
		// Must have magic unlocked, maybe?
		
		final ItemStack centerItem = layout.getCenterItem(world, center);
		
		// Put the geogem back on the altar
		( (AltarTileEntity) world.getBlockEntity(center)).setItem(centerItem);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (ModConfig.config.obeliskReqMagic() && (attr == null || !attr.isUnlocked()))
			return;
		
		if (centerItem.isEmpty() || !(centerItem.getItem() instanceof PositionCrystal))
			return;
		
		BlockPos pos = PositionCrystal.getBlockPosition(centerItem);
		if (pos == null) {
			player.sendMessage(new TranslatableComponent("info.teleport_obelisk.fail", new Object[0]), Util.NIL_UUID);
			return;
		}
		
		if (!world.isClientSide) {
			final BlockPos to = pos.above();
			final Location dest = new Location(world, to);
			if (attr.hasEnhancedTeleport()) {
				BlockEntity te = world.getBlockEntity(pos);
				if (te == null || !(te instanceof ObeliskTileEntity)) {
					NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
					player.sendMessage(new TranslatableComponent("info.teleport_obelisk.fail"), Util.NIL_UUID);
					return;
				}
				
				ObeliskTileEntity obelisk = (ObeliskTileEntity) te;
				BlockPos portal = TemporaryTeleportationPortalBlock.spawnNearby(world, center.above(), 4, true, dest, 20 * 30);
				if (portal != null) {
					obelisk.setOverride(new Location(world, portal), 20 * 30);
				}
			} else {
				// Validate obelisks
				if (ObeliskTileEntity.IsObeliskPos(dest)) {
					player.sendMessage(new TranslatableComponent("info.obelisk.dne"), Util.NIL_UUID);
					return;
				}
				
				BlockPos targ = null;
				for (BlockPos attempt : new BlockPos[]{to, to.above(), to.north(), to.north().east(), to.north().west(), to.east(), to.west(), to.south(), to.south().east(), to.south().west()}) {
					if (player.randomTeleport(attempt.getX() + .5, attempt.getY() + 1, attempt.getZ() + .5, false)) {
						targ = attempt;
						break;
					}
				}
				if (targ != null) {
					//doEffects(world, to);
				} else {
					player.sendMessage(new TranslatableComponent("info.obelisk.noroom"), Util.NIL_UUID);
				}
			}
			
			if (NostrumMagica.rand.nextInt(10) == 0) {
				float dist = 2 + NostrumMagica.rand.nextFloat() * 2;
				float dir = NostrumMagica.rand.nextFloat();
				double dirD = dir * 2 * Math.PI;
				double dx = Math.cos(dirD) * dist;
				double dz = Math.sin(dirD) * dist;
				ItemEntity drop = new ItemEntity(world, pos.getX() + .5 + dx, pos.getY() + 2, pos.getZ() + .5 + dz,
						new ItemStack(NostrumItems.resourceEnderBristle));
				world.addFreshEntity(drop);
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
			}
		}
		
	}
	
	@Override
	public String getName() {
		return "teleport_obelisk";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.teleport_obelisk.desc");
	}
}
