package com.smanzana.nostrummagica.integration.curios.inventory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.util.ISlotHelper;

public class CurioInventoryWrapper implements Container {
	
	public static CurioInventoryWrapper getCuriosInventory(Player entity) {
		return new CurioInventoryWrapper(entity, false);
//		Optional<ItemStackArrayWrapper> opt = CuriosApi.getCuriosHelper().getCuriosHandler(entity).map((handler) -> {
//			ICurioStacksHandler[] stackHandlers = handler.getCurios().values().toArray(new ICurioStacksHandler[0]);
//			ArrayList<ItemStack> itemList = new ArrayList<>();
//			for (ICurioStacksHandler h : stackHandlers) {
//				for (int i = 0; i < h.getSlots(); i++) {
//					itemList.add(h.getStacks().getStackInSlot(i));
//				}
//			}
//			
//			return new ItemStackArrayWrapper(itemList.toArray(new ItemStack[0]));
//		});
//		return opt.orElse(EMPTY_INV);
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
	
	private final Map<CurioSlotReference, ItemStack> slots;
	private final Map<Integer, CurioSlotReference> flatSlotMap;
	
	protected CurioInventoryWrapper(LivingEntity entity, boolean empty) {
		slots = new HashMap<>();
		flatSlotMap = new HashMap<>();
		
		// Slot helper is only available on the server :(
		ISlotHelper slotHelper = CuriosApi.getSlotHelper();
		if (slotHelper != null) {
			for (String typeID : slotHelper.getSlotTypeIds()) {
				for (int i = 0; i < slotHelper.getSlotsForType(entity, typeID); i++) {
					CurioSlotReference key = new CurioSlotReference(typeID, i);
					final ItemStack stack = empty ? ItemStack.EMPTY : key.getHeldStack(entity);
					slots.put(key, stack);
					flatSlotMap.put(flatSlotMap.size(), key);
				}
			}
		} else {
			LazyOptional<ICuriosItemHandler> handler = CuriosApi.getCuriosHelper().getCuriosHandler(entity);
			if (handler.isPresent()) {
				handler.resolve().get().getCurios().entrySet().stream()
					.forEachOrdered((entry) -> {
						for (int i = 0; i < entry.getValue().getSlots(); i++) {
							CurioSlotReference key = new CurioSlotReference(entry.getKey(), i);
							final ItemStack stack = empty ? ItemStack.EMPTY : key.getHeldStack(entity);
							slots.put(key, stack);
							flatSlotMap.put(flatSlotMap.size(), key);
						}
					});
			}
		}
	}
	
	public Collection<CurioSlotReference> getKeySet() {
		return slots.keySet();
	}
	
	@Override
	public void clearContent() {
		slots.replaceAll((slot, existing) -> ItemStack.EMPTY);
	}

	@Override
	public int getContainerSize() {
		return slots.size();
	}

	@Override
	public boolean isEmpty() {
		return slots.values().stream().filter(stack -> !stack.isEmpty()).findAny().isPresent();
	}
	
	protected CurioSlotReference getKey(int index) {
		return flatSlotMap.get(index);
	}

	@Override
	public ItemStack getItem(int index) {
		return slots.get(getKey(index));
	}

	@Override
	public ItemStack removeItem(int index, int count) {
		return slots.get(getKey(index)).split(count);
	}

	@Override
	public ItemStack removeItemNoUpdate(int index) {
		return slots.remove(getKey(index));
	}

	@Override
	public void setItem(int index, ItemStack stack) {
		slots.put(getKey(index), stack);
	}

	@Override
	public void setChanged() {
		;
	}

	@Override
	public boolean stillValid(Player player) {
		return true;
	}
}
