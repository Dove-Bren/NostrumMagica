package com.smanzana.nostrummagica.attributes;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicPotency extends RangedAttribute {
	
	public static final String unlocalized_name = NostrumMagica.MODID + ".magic_potency";
	
	private static AttributeMagicPotency instance = null;
	
	public static AttributeMagicPotency instance() {
		if (instance == null) {
			instance = new AttributeMagicPotency();
		}
		
		return instance;
	}
	
	private AttributeMagicPotency() {
		super(unlocalized_name, 0, -100.0D, 100.0D);
	}

}
