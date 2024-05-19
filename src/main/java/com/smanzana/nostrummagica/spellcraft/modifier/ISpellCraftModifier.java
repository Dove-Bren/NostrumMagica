package com.smanzana.nostrummagica.spellcraft.modifier;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellPartBuilder;
import com.smanzana.nostrummagica.spells.SpellPart;

public interface ISpellCraftModifier {

	public boolean canModify(SpellCraftContext context, SpellPart originalPart);
	
	public void modify(SpellCraftContext context, SpellPart originalPart, SpellPartBuilder builder);
	
}
