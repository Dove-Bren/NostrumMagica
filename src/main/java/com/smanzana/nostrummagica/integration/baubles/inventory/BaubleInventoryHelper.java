package com.smanzana.nostrummagica.integration.baubles.inventory;

import baubles.api.BaublesApi;
import baubles.api.inv.BaublesInventoryWrapper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraftforge.fml.common.Optional;

public class BaubleInventoryHelper {

	@Optional.Method(modid="baubles")
	public static IInventory getBaubleInventory(PlayerEntity player) {
		return new BaublesInventoryWrapper(BaublesApi.getBaublesHandler(player));
	}

}
