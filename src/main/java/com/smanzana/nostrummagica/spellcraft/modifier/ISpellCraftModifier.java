package com.smanzana.nostrummagica.spellcraft.modifier;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellPartBuilder;
import com.smanzana.nostrummagica.spells.SpellPart;

import net.minecraft.item.ItemStack;

public interface ISpellCraftModifier {

	public boolean canModify(SpellCraftContext context, ItemStack rune, SpellPart originalPart);
	
	public void modify(SpellCraftContext context, ItemStack rune, SpellPart originalPart, SpellPartBuilder builder);
	
}
