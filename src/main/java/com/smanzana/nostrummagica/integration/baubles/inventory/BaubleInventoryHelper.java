package com.smanzana.nostrummagica.integration.baubles.inventory;

import baubles.api.BaublesApi;
import baubles.api.inv.BaublesInventoryWrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.common.Optional;

public class BaubleInventoryHelper {

	@Optional.Method(modid="Baubles")
	public static IInventory getBaubleInventory(EntityPlayer player) {
		return new BaublesInventoryWrapper(BaublesApi.getBaublesHandler(player));
	}
	
}
