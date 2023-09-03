package com.smanzana.nostrummagica.integration.enderio.wrappers;

import crazypants.enderio.api.teleport.TravelSource;
import crazypants.enderio.base.teleport.TravelController;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class TravelControllerWrapper {

	public TravelControllerWrapper() {
		
	}
	
	public static boolean activateTravelAccessable(ItemStack equipped, Hand hand, World world, PlayerEntity player, TravelSource source) {
		return TravelController.activateTravelAccessable(equipped, hand, world, player, source);
	}
	
}
