package com.smanzana.nostrummagica.item.set;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class EquipmentSet extends ForgeRegistryEntry<EquipmentSet> {

	public abstract Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems);
	
	public abstract boolean isSetItem(ItemStack stack);
	
	public abstract boolean isSetItemValid(ItemStack stack, IInventorySlotKey<? extends LivingEntity> slot, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> existingItems);
	
	public abstract void setTick(LivingEntity entity, Map<IInventorySlotKey<? extends LivingEntity>, ItemStack> setItems);
	
	public ITextComponent getName() {
		return new TranslationTextComponent("set." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".name");
	}
	
	public ITextComponent getDescription() {
		return new TranslationTextComponent("set." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".desc");
	}
	
	public abstract Multimap<Attribute, AttributeModifier> getFullSetBonuses();
	
	public abstract int getFullSetCount();

	public List<ITextComponent> getExtraBonuses(int setCount) {
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
