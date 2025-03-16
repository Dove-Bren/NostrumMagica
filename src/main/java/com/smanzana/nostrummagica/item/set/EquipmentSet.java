package com.smanzana.nostrummagica.item.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class EquipmentSet extends ForgeRegistryEntry<EquipmentSet> {

	public abstract Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems);
	
	public abstract boolean isSetItem(ItemStack stack);
	
	public abstract boolean isSetItemValid(ItemStack stack, IInventorySlotKey<? extends LivingEntity> slot, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> existingItems);
	
	public abstract void setTick(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems);
	
	public Component getName() {
		return new TranslatableComponent("set." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".name");
	}
	
	public Component getDescription() {
		return new TranslatableComponent("set." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".desc");
	}
	
	public abstract Multimap<Attribute, AttributeModifier> getFullSetBonuses();
	
	public abstract int getFullSetCount();

	public List<Component> getExtraBonuses(int setCount) {
		if (setCount == this.getFullSetCount()) {
			final String extraKey = "set." + this.getRegistryName().getNamespace() + "."
					+ this.getRegistryName().getPath() + ".extra";
			if (I18n.exists(extraKey)) {
				return TextUtils.GetTranslatedList(extraKey);
			}
		}
		return new ArrayList<>();
	}
}
