package com.smanzana.nostrummagica.attributes;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeManaRegen extends RangedAttribute {
	
	public static final String ID = "mana_regen";
	public static final String unlocalized_name = NostrumMagica.MODID + "." + ID;
	
	private static AttributeManaRegen instance = null;
	
	public static AttributeManaRegen instance() {
		if (instance == null) {
			instance = new AttributeManaRegen();
		}
		
		return instance;
	}
	
	private AttributeManaRegen() {
		super(unlocalized_name, 0, -100.0D, 500.0D);
		this.setShouldWatch(true);
	}

}
