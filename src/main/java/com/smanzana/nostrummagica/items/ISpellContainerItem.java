package com.smanzana.nostrummagica.items;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.item.ItemStack;

public interface ISpellContainerItem {

	public @Nullable Spell getSpell(ItemStack stack);
	
}
