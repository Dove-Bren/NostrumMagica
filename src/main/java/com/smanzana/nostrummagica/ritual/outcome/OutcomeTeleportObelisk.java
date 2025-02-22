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

import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class OutcomeTeleportObelisk implements IRitualOutcome {

	public OutcomeTeleportObelisk() {
		;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Teleport the player to the obelisk pointed at by the center item
		// Must have magic unlocked, maybe?
		
		final ItemStack centerItem = layout.getCenterItem(world, center);
		
		// Put the geogem back on the altar
		( (AltarTileEntity) world.getTileEntity(center)).setItem(centerItem);
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (ModConfig.config.obeliskReqMagic() && (attr == null || !attr.isUnlocked()))
			return;
		
		if (centerItem.isEmpty() || !(centerItem.getItem() instanceof PositionCrystal))
			return;
		
		BlockPos pos = PositionCrystal.getBlockPosition(centerItem);
		if (pos == null) {
			player.sendMessage(new TranslationTextComponent("info.teleport_obelisk.fail", new Object[0]), Util.DUMMY_UUID);
			return;
		}
		
		if (!world.isRemote) {
			final BlockPos to = pos.up();
			final Location dest = new Location(world, to);
			if (attr.hasEnhancedTeleport()) {
				TileEntity te = world.getTileEntity(pos);
				if (te == null || !(te instanceof ObeliskTileEntity)) {
					NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
					player.sendMessage(new TranslationTextComponent("info.teleport_obelisk.fail"), Util.DUMMY_UUID);
					return;
				}
				
				ObeliskTileEntity obelisk = (ObeliskTileEntity) te;
				BlockPos portal = TemporaryTeleportationPortalBlock.spawnNearby(world, center.up(), 4, true, dest, 20 * 30);
				if (portal != null) {
					obelisk.setOverride(new Location(world, portal), 20 * 30);
				}
			} else {
				// Validate obelisks
				if (ObeliskTileEntity.IsObeliskPos(dest)) {
					player.sendMessage(new TranslationTextComponent("info.obelisk.dne"), Util.DUMMY_UUID);
					return;
				}
				
				BlockPos targ = null;
				for (BlockPos attempt : new BlockPos[]{to, to.up(), to.north(), to.north().east(), to.north().west(), to.east(), to.west(), to.south(), to.south().east(), to.south().west()}) {
					if (player.attemptTeleport(attempt.getX() + .5, attempt.getY() + 1, attempt.getZ() + .5, false)) {
						targ = attempt;
						break;
					}
				}
				if (targ != null) {
					//doEffects(world, to);
				} else {
					player.sendMessage(new TranslationTextComponent("info.obelisk.noroom"), Util.DUMMY_UUID);
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
				world.addEntity(drop);
				NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
			}
		}
		
	}
	
	@Override
	public String getName() {
		return "teleport_obelisk";
	}

	@Override
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.teleport_obelisk.desc");
	}
}
