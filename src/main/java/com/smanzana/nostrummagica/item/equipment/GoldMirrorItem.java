package com.smanzana.nostrummagica.item.equipment;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.IPositionHolderItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.RemoteInteractMessage;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GoldMirrorItem extends HandheldMirrorItem implements ILoreTagged {

	public static final String ID = "gold_mirror";
	
	public GoldMirrorItem(Properties props) {
		super(props);
	}

	@Override
	public String getLoreKey() {
		return ID;
	}

	@Override
	public String getLoreDisplayName() {
		return "Gold Mirror";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore()
				.add("This magical mirror can attach to a position in the world and then let you interact with that position later!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore()
				.add("This magical mirror can attach to a position in the world and then let you interact with that position later!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}

	@Override
	protected void handleOpen(Player player, InteractionHand hand, ItemStack stack) {
		if (!player.level.isClientSide()) {
			return; // Only run client side to kick off handshake
		}
		
		final ResourceKey<Level> dimension = IPositionHolderItem.getDimension(stack);
		final BlockPos pos = IPositionHolderItem.getBlockPosition(stack);
		
		if (pos != null && dimension != null && DimensionUtils.InDimension(player, dimension)) {
			// Since in the dimension, can use player world
			if (NostrumMagica.isBlockLoaded(player.level, pos)) {
				NetworkHandler.sendToServer(new RemoteInteractMessage(dimension, pos, hand));
			} else {
				player.sendMessage(new TranslatableComponent("info.gold_mirror.not_loaded"), Util.NIL_UUID);
			}
		}
	}

}
