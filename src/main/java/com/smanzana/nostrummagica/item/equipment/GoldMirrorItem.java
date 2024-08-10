package com.smanzana.nostrummagica.item.equipment;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.item.IPositionHolderItem;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.RemoteInteractMessage;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

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
	protected void handleOpen(PlayerEntity player, Hand hand, ItemStack stack) {
		if (!player.world.isRemote()) {
			return; // Only run client side to kick off handshake
		}
		
		final RegistryKey<World> dimension = IPositionHolderItem.getDimension(stack);
		final BlockPos pos = IPositionHolderItem.getBlockPosition(stack);
		
		if (pos != null && dimension != null && DimensionUtils.InDimension(player, dimension)) {
			// Since in the dimension, can use player world
			if (NostrumMagica.isBlockLoaded(player.world, pos)) {
				NetworkHandler.sendToServer(new RemoteInteractMessage(dimension, pos, hand));
			} else {
				player.sendMessage(new TranslationTextComponent("info.gold_mirror.not_loaded"), Util.DUMMY_UUID);
			}
		}
	}

}
