package com.smanzana.nostrummagica.integration.curios.inventory;

import java.util.Objects;

import com.smanzana.nostrummagica.inventory.IInventorySlotKey;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

public class CurioSlotReference implements IInventorySlotKey<LivingEntity> {
	
	private final String slotType;
	private final int slotIdx;

	public CurioSlotReference(String slotType, int slotIdx) {
		this.slotType = slotType;
		this.slotIdx = slotIdx;
	}

	@Override
	public ItemStack getHeldStack(LivingEntity entity) {
		return CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve().flatMap(h -> h.getStacksHandler(this.slotType)).map(h -> h.getStacks().getStackInSlot(this.slotIdx))
				.orElse(ItemStack.EMPTY);
	}

	@Override
	public ItemStack setStack(LivingEntity entity, ItemStack newStack) {
		ItemStack existing = getHeldStack(entity);
		CuriosApi.getCuriosHelper().getCuriosHandler(entity).resolve().flatMap(h -> h.getStacksHandler(this.slotType)).ifPresent(h -> h.getStacks().setStackInSlot(slotIdx, newStack));
		return existing;
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof CurioSlotReference) {
			return ((CurioSlotReference) o).slotType.equals(this.slotType)
					&& ((CurioSlotReference) o).slotIdx == this.slotIdx;
		}
		return false;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(slotType, slotIdx);
	}

}
