package com.smanzana.nostrummagica.spellcraft.modifier;

import java.util.List;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellPartBuilder;
import com.smanzana.nostrummagica.spells.SpellPart;

import net.minecraft.util.text.ITextComponent;

public interface ISpellCraftModifier {

	public boolean canModify(SpellCraftContext context, SpellPart originalPart);
	
	public void modify(SpellCraftContext context, SpellPart originalPart, SpellPartBuilder builder);

	public List<ITextComponent> getDetails(List<ITextComponent> lines);
	
}
