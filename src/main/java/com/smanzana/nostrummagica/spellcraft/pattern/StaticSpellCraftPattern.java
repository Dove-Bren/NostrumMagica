package com.smanzana.nostrummagica.spellcraft.pattern;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;

/**
 * Spell craft pattern who's modifiers are static and don't change order
 * @author Skyler
 *
 */
public class StaticSpellCraftPattern extends SpellCraftPattern {
	
	protected final ISpellCraftModifier[] modifiers;
	
	public StaticSpellCraftPattern(ISpellCraftModifier ... modifiers) {
		super();
		this.modifiers = modifiers;
	}

	@Override
	public boolean hasModifier(SpellCraftContext context, int slot) {
		return slot >= 0
				&& this.modifiers.length > slot
				&& this.modifiers[slot] != null;
	}

	@Override
	public ISpellCraftModifier getModifier(SpellCraftContext context, int slot) {
		if (slot < 0 || slot >= this.modifiers.length) {
			return null;
		}
		
		return modifiers[slot];
	}
}
