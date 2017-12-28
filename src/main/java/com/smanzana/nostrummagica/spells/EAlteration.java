package com.smanzana.nostrummagica.spells;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.util.ResourceLocation;

public enum EAlteration {

	INFLICT("Inflict", 10),
	RESIST("Resist", 30),
	SUPPORT("Support", 15),
	GROWTH("Growth", 25),
	ENCHANT("Enchant", 40),
	CONJURE("Conjure", 30),
	SUMMON("Summon", 50),
	ALTER("Alter", 40);
	
	private ResourceLocation glyph;
	private String name;
	private int cost;
	
	private EAlteration(String base, int cost) {
		this.name = base;
		this.glyph = new ResourceLocation(NostrumMagica.MODID, base.toLowerCase());
	}

	public ResourceLocation getGlyph() {
		return glyph;
	}

	public String getName() {
		return name;
	}
	
	public int getCost() {
		return cost;
	}
	
}
