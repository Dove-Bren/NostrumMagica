package com.smanzana.nostrummagica.spellcraft.modifier;

import java.util.List;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellPartBuilder;
import com.smanzana.nostrummagica.spells.LegacySpellPart;

import net.minecraft.util.text.ITextComponent;

public interface ISpellCraftModifier {

	public boolean canModify(SpellCraftContext context, LegacySpellPart originalPart);
	
	public void modify(SpellCraftContext context, LegacySpellPart originalPart, SpellPartBuilder builder);

	public List<ITextComponent> getDetails(List<ITextComponent> lines);
	
}
