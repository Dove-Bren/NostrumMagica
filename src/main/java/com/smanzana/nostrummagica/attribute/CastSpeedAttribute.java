package com.smanzana.nostrummagica.attribute;

import com.smanzana.nostrummagica.attribute.IPrintableAttribute.IPercentageAttribute;

import net.minecraft.world.entity.ai.attributes.RangedAttribute;

/**
 * An entity's casting speed. Base is 100.0.
 * So +15 is +15% SPEED.
 * So someone with a total of 115 (so 100 base + 15 bonus) casting speed would cast things 15% faster.
 * AKA something that took 100 ticks would take (100/1.15) ticks (~86).
 * With a 300 casting speed, 100 ticks would become (100/3) ticks (~33).
 * Note that cast times that are <1 tick are approximated to instant.
 */
public class CastSpeedAttribute extends RangedAttribute implements IPercentageAttribute {
	
	public static final String ID = "casting_speed";
	
	public CastSpeedAttribute(String name) {
		super(name, 100, 1.0D, 1000.0D);
		this.setSyncable(true);
	}

}
