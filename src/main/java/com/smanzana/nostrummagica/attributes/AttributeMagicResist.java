package com.smanzana.nostrummagica.attributes;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicResist extends RangedAttribute {
	
	public static final String unlocalized_name = NostrumMagica.MODID + ".magic_resist";
	
	private static AttributeMagicResist instance = null;
	
	public static AttributeMagicResist instance() {
		if (instance == null) {
			instance = new AttributeMagicResist();
		}
		
		return instance;
	}
	
	private AttributeMagicResist() {
		super(null, unlocalized_name, 0, -100.0D, 100.0D);
	}

}
