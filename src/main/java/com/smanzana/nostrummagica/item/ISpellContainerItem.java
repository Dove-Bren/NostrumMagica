package com.smanzana.nostrummagica.item;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.world.item.ItemStack;

public interface ISpellContainerItem {

	public @Nullable Spell getSpell(ItemStack stack);
	
}
