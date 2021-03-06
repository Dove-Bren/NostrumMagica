package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.TemporaryTeleportationPortal;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeRecall implements IRitualOutcome {

	public OutcomeRecall() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Return the player to their marked location, if they have one
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		BlockPos pos = attr.getMarkLocation();
		if (pos == null) {
			if (world.isRemote)
				player.addChatMessage(new TextComponentTranslation("info.recall.fail", new Object[0]));
			return;
		}
		
		if (player.dimension == attr.getMarkDimension()) {
			if (!world.isRemote) {
				
				if (attr.hasEnhancedTeleport()) {
					BlockPos portal = TemporaryTeleportationPortal.spawnNearby(world, center.up(), 4, true, pos, 20 * 30);
					if (portal != null) {
						TemporaryTeleportationPortal.spawnNearby(world, pos, 4, true, portal, 20 * 30);
					}
				} else {
					player.setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
				}
				
				if (NostrumMagica.rand.nextInt(10) == 0) {
					float dist = 2 + NostrumMagica.rand.nextFloat() * 2;
					float dir = NostrumMagica.rand.nextFloat();
					double dirD = dir * 2 * Math.PI;
					double dx = Math.cos(dirD) * dist;
					double dz = Math.sin(dirD) * dist;
					EntityItem drop = new EntityItem(world, pos.getX() + .5 + dx, pos.getY() + 2, pos.getZ() + .5 + dz,
							NostrumResourceItem.getItem(ResourceType.ENDER_BRISTLE, 1));
					world.spawnEntityInWorld(drop);
					NostrumMagicaSounds.CAST_FAIL.play(world, pos.getX() + .5, pos.getY() + 2, pos.getZ() + .5);
				}
			}
		} else {
			player.addChatMessage(new TextComponentTranslation("info.recall.baddimension", new Object[0]));
		}
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.recall.desc",
				new Object[0])
				.split("\\|"));
	}
}
