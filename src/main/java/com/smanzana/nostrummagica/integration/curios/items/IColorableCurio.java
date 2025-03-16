package com.smanzana.nostrummagica.integration.curios.items;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.item.ItemStack;

public interface IColorableCurio {

	public @Nullable EMagicElement getEmbeddedElement(ItemStack stack);
	
	public void setEmbeddedElement(ItemStack stack, EMagicElement element);
	
}
