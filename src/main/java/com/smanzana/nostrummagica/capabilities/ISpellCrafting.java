package com.smanzana.nostrummagica.capabilities;

import java.util.List;

import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

public interface ISpellCrafting {
	
	public List<SpellCraftPattern> getKnownPatterns();
	
	public void addPattern(SpellCraftPattern pattern);
	
	
	
	
	
	
	
	
	
	
	public void copy(ISpellCrafting source);

}
