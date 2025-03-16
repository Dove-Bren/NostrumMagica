package com.smanzana.nostrummagica.capabilities;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.Capability.IStorage;
import net.minecraft.nbt.Tag;

public class SpellCraftingCapability implements ISpellCrafting {
	
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
	
	public static class Serializer implements IStorage<ISpellCrafting> {
		
		public static final Serializer INSTANCE = new Serializer();
		
		private static final String NBT_PATTERNS = "patterns";
		
		protected Serializer() {
			
		}
	
		@Override
		public Tag writeNBT(Capability<ISpellCrafting> capability, ISpellCrafting instanceIn, Direction side) {
			SpellCraftingCapability instance = (SpellCraftingCapability) instanceIn;
			CompoundTag nbt = new CompoundTag();
			
			ListTag list = new ListTag();
			for (SpellCraftPattern pattern : instance.getKnownPatterns()) {
				list.add(StringTag.valueOf(pattern.getRegistryName().toString()));
			}
			nbt.put(NBT_PATTERNS, list);
			
			return nbt;
		}

		@Override
		public void readNBT(Capability<ISpellCrafting> capability, ISpellCrafting instanceIn, Direction side, Tag nbtIn) {
			SpellCraftingCapability instance = (SpellCraftingCapability) instanceIn;
			instance.clearAll();
			
			if (nbtIn.getId() == Tag.TAG_COMPOUND) {
				CompoundTag nbt = (CompoundTag) nbtIn;
				
				ListTag patternList = nbt.getList(NBT_PATTERNS, Tag.TAG_STRING);
				for (int i = 0; i < patternList.size(); i++) {
					ResourceLocation key = new ResourceLocation(patternList.getString(i));
					@Nullable SpellCraftPattern pattern = SpellCraftPattern.Get(key);
					if (pattern != null) {
						instance.patterns.add(pattern);
					} else {
						NostrumMagica.logger.error("Encountered pattern in SpellCraftingCapability that is unknown: " + key);
					}
				}
			}
		}
	}

}
