package com.smanzana.nostrummagica.attributes;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicPotency extends RangedAttribute {

	public static final String ID = "magic_potency";
	public static final String unlocalized_name = NostrumMagica.MODID + "." + ID;
	
	private static AttributeMagicPotency instance = null;
	
	public static AttributeMagicPotency instance() {
		if (instance == null) {
			instance = new AttributeMagicPotency();
		}
		
		return instance;
	}
	
	private AttributeMagicPotency() {
		super(unlocalized_name, 0, -100.0D, 100.0D);
		this.setShouldWatch(true);
	}

}
