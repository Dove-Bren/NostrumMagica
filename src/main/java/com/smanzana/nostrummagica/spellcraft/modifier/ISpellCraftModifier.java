package com.smanzana.nostrummagica.spellcraft.modifier;

import java.util.List;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellIngredient;
import com.smanzana.nostrummagica.spellcraft.SpellIngredientBuilder;

import net.minecraft.util.text.ITextComponent;

public interface ISpellCraftModifier {

	public boolean canModify(SpellCraftContext context, SpellIngredient originalPart);
	
	public void modify(SpellCraftContext context, SpellIngredient originalPart, SpellIngredientBuilder builder);

	public List<ITextComponent> getDetails(List<ITextComponent> lines);
	
}
