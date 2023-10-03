package com.smanzana.nostrummagica.integration.curios.inventory;

import java.util.ArrayList;
import java.util.SortedMap;
import java.util.function.Predicate;

import com.smanzana.nostrummagica.utils.Inventories.ItemStackArrayWrapper;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosAPI;
import top.theillusivec4.curios.api.inventory.CurioStackHandler;

public class CurioInventoryWrapper {
	
	public static final ItemStackArrayWrapper EMPTY_INV = new ItemStackArrayWrapper(new ItemStack[0]);
	
	public static IInventory getCuriosInventory(PlayerEntity entity) {
		LazyOptional<ItemStackArrayWrapper> opt = CuriosAPI.getCuriosHandler(entity).map((handler) -> {
			CurioStackHandler[] stackHandlers = handler.getCurioMap().values().toArray(new CurioStackHandler[0]);
			ArrayList<ItemStack> itemList = new ArrayList<>();
			for (CurioStackHandler h : stackHandlers) {
				for (int i = 0; i < h.getSlots(); i++) {
					itemList.add(h.getStackInSlot(i));
				}
			}
			
			return new ItemStackArrayWrapper(itemList.toArray(new ItemStack[0]));
		});
		return opt.orElse(EMPTY_INV);
	}

	public static void forEach(LivingEntity entity, Predicate<ItemStack> action) {
		CuriosAPI.getCuriosHandler(entity).ifPresent((handler) -> {
			SortedMap<String, CurioStackHandler> curioMap = handler.getCurioMap();
			for (CurioStackHandler h : curioMap.values()) {
				for (int i = 0; i < h.getSlots(); i++) {
					ItemStack stack = h.getStackInSlot(i);
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
