package com.smanzana.nostrummagica.item.set;

import java.util.Map;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.inventory.IInventorySlotKey;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistryEntry;

public abstract class EquipmentSet extends ForgeRegistryEntry<EquipmentSet> {

	public abstract Multimap<Attribute, AttributeModifier> getSetBonuses(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems);
	
	public abstract boolean isSetItem(ItemStack stack, IInventorySlotKey<LivingEntity> slot);
	
	public abstract void setTick(LivingEntity entity, Map<IInventorySlotKey<LivingEntity>, ItemStack> setItems);
	
	public ITextComponent getName() {
		return new TranslationTextComponent("set." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".name");
	}
	
	public ITextComponent getDescription() {
		return new TranslationTextComponent("set." + this.getRegistryName().getNamespace() + "." + this.getRegistryName().getPath() + ".desc");
	}
	
}
