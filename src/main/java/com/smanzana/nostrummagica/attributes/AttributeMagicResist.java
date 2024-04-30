package com.smanzana.nostrummagica.attributes;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicResist extends RangedAttribute {
	
	public static final String ID = "magic_resist";
	public static final String unlocalized_name = NostrumMagica.MODID + "." + ID;
	
	private static AttributeMagicResist instance = null;
	
	public static AttributeMagicResist instance() {
		if (instance == null) {
			instance = new AttributeMagicResist();
		}
		
		return instance;
	}
	
	private AttributeMagicResist() {
		super(unlocalized_name, 0, -100.0D, 100.0D);
		this.setShouldWatch(true);
	}

}
