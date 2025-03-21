package com.smanzana.nostrummagica.capabilities;

import java.util.List;

import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraftforge.common.util.INBTSerializable;

public interface ISpellCrafting extends INBTSerializable<CompoundTag> {
	
	public List<SpellCraftPattern> getKnownPatterns();
	
	public void addPattern(SpellCraftPattern pattern);
	
	
	
	
	
	
	
	
	
	
	public void copy(ISpellCrafting source);

}
