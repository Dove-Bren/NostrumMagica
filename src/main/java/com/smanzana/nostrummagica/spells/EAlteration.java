package com.smanzana.nostrummagica.spells;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.util.ResourceLocation;

public enum EAlteration {

	INFLICT("Inflict"),
	RESIST("Resist"),
	SUPPORT("Support"),
	GROWTH("Growth"),
	ENCHANT("Enchant"),
	CONJURE("Conjure"),
	SUMMON("Summon"),
	ALTER("Alter");
	
	private ResourceLocation glyph;
	private String name;
	
	private EAlteration(String base) {
		this.name = base;
		this.glyph = new ResourceLocation(NostrumMagica.MODID, base.toLowerCase());
	}

	public ResourceLocation getGlyph() {
		return glyph;
	}

	public String getName() {
		return name;
	}
	
}
