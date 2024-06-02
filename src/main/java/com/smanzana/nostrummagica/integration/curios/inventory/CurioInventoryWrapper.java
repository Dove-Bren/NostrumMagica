package com.smanzana.nostrummagica.integration.curios.inventory;

import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import com.smanzana.nostrummagica.util.Inventories.ItemStackArrayWrapper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

public class CurioInventoryWrapper {
	
	public static final ItemStackArrayWrapper EMPTY_INV = new ItemStackArrayWrapper(new ItemStack[0]);
	
	public static IInventory getCuriosInventory(PlayerEntity entity) {
		Optional<ItemStackArrayWrapper> opt = CuriosApi.getCuriosHelper().getCuriosHandler(entity).map((handler) -> {
			ICurioStacksHandler[] stackHandlers = handler.getCurios().values().toArray(new ICurioStacksHandler[0]);
			ArrayList<ItemStack> itemList = new ArrayList<>();
			for (ICurioStacksHandler h : stackHandlers) {
				for (int i = 0; i < h.getSlots(); i++) {
					itemList.add(h.getStacks().getStackInSlot(i));
				}
			}
			
			return new ItemStackArrayWrapper(itemList.toArray(new ItemStack[0]));
		});
		return opt.orElse(EMPTY_INV);
	}

	public static void forEach(LivingEntity entity, Predicate<ItemStack> action) {
		CuriosApi.getCuriosHelper().getCuriosHandler(entity).ifPresent((handler) -> {
			Map<String, ICurioStacksHandler> curioMap = handler.getCurios();
			for (ICurioStacksHandler h : curioMap.values()) {
				for (int i = 0; i < h.getSlots(); i++) {
					ItemStack stack = h.getStacks().getStackInSlot(i);
					if (!stack.isEmpty()) {
						// Perform action. Cancel iteration if action returns true
						if (action.test(stack)) {
							return;
						}
					}
				}
			}
		});
	}
}
