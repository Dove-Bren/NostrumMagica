package com.smanzana.nostrummagica.item.equipment;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ParadoxMirrorBlock;
import com.smanzana.nostrummagica.client.gui.container.SilverMirrorGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.Inventories;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

import net.minecraft.world.item.Item.Properties;

public class SilverMirrorItem extends HandheldMirrorItem implements ILoreTagged {

	public static final String ID = "silver_mirror";
	
	public SilverMirrorItem(Properties props) {
		super(props);
	}

	@Override
	public String getLoreKey() {
		return ID;
	}

	@Override
	public String getLoreDisplayName() {
		return "Silver Mirror";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore()
				.add("This magical mirror can be pointed at a paradox mirror and any items put into it will show up there!");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore()
				.add("This magical mirror can be pointed at a paradox mirror and any items put into it will show up there!");
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_ITEMS;
	}
	
	@Override
	protected boolean canStore(Level world, BlockPos pos) {
		// Must be a paradox mirror
		BlockState state = world.getBlockState(pos);
		return state.getBlock() instanceof ParadoxMirrorBlock;
	}

	@Override
	protected void handleOpen(Player player, InteractionHand hand, ItemStack stack) {
		int pos = Inventories.getPlayerHandSlotIndex(player.inventory, hand);
		NostrumMagica.instance.proxy.openContainer(player, SilverMirrorGui.MirrorContainer.Make(pos));
	}

}
