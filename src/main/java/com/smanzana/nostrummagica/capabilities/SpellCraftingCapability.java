package com.smanzana.nostrummagica.capabilities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

public class SpellCraftingCapability implements ISpellCrafting {
	
	private static final String NBT_PATTERNS = "patterns";
	
	protected List<SpellCraftPattern> patterns;
	
	public SpellCraftingCapability() {
		patterns = new ArrayList<>();
	}
	
	@Override
	public List<SpellCraftPattern> getKnownPatterns() {
		return patterns;
	}
	
	@Override
	public void addPattern(SpellCraftPattern pattern) {
		// There won't be THAT many patterns, right?
		if (!patterns.contains(pattern)) {
			patterns.add(pattern);
		}
	}
	
	@Override
	public void copy(ISpellCrafting other) {
		this.clearAll();
		
		this.patterns.addAll(other.getKnownPatterns());
	}
	
	protected void clearAll() {
		this.patterns.clear();
	}
	
	@Override
	public CompoundTag serializeNBT() {
		CompoundTag nbt = new CompoundTag();
		
		ListTag list = new ListTag();
		for (SpellCraftPattern pattern : getKnownPatterns()) {
			list.add(StringTag.valueOf(pattern.getRegistryName().toString()));
		}
		nbt.put(NBT_PATTERNS, list);
		
		return nbt;
	}

	@Override
	public void deserializeNBT(CompoundTag nbt) {
		clearAll();
		
		ListTag patternList = nbt.getList(NBT_PATTERNS, Tag.TAG_STRING);
		for (int i = 0; i < patternList.size(); i++) {
			ResourceLocation key = new ResourceLocation(patternList.getString(i));
			@Nullable SpellCraftPattern pattern = SpellCraftPattern.Get(key);
			if (pattern != null) {
				patterns.add(pattern);
			} else {
				NostrumMagica.logger.error("Encountered pattern in SpellCraftingCapability that is unknown: " + key);
			}
		}
	}
}
