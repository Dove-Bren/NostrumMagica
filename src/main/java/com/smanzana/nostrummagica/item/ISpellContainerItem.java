package com.smanzana.nostrummagica.item;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.item.ItemStack;

public interface ISpellContainerItem {

	public @Nullable Spell getSpell(ItemStack stack);
	
}
