package com.smanzana.nostrummagica.integration.enderio.wrappers;

import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.base.teleport.TravelController;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.world.World;

public class TravelControllerWrapper {

	public TravelControllerWrapper() {
		
	}
	
	public static boolean activateTravelAccessable(ItemStack equipped, EnumHand hand, World world, EntityPlayer player, TravelSource source) {
		return TravelController.activateTravelAccessable(equipped, hand, world, player, source);
	}
	
}
