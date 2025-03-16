package com.smanzana.nostrummagica.spellcraft.modifier;

import java.util.List;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellIngredient;
import com.smanzana.nostrummagica.spellcraft.SpellIngredientBuilder;

import net.minecraft.network.chat.Component;

public interface ISpellCraftModifier {

	public boolean canModify(SpellCraftContext context, SpellIngredient originalPart);
	
	public void modify(SpellCraftContext context, SpellIngredient originalPart, SpellIngredientBuilder builder);

	public List<Component> getDetails(List<Component> lines);
	
}
