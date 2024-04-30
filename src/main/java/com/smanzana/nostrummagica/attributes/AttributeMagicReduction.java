package com.smanzana.nostrummagica.attributes;

import java.util.EnumMap;
import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.ai.attributes.RangedAttribute;

public class AttributeMagicReduction extends RangedAttribute {
	
	public static final String ID_PREFIX = "magic_reduct_";
	private static final String unlocalized_name_prefix = NostrumMagica.MODID + "." + ID_PREFIX;
	
	private static Map<EMagicElement, AttributeMagicReduction> instances = new EnumMap<>(EMagicElement.class);
	
	public static AttributeMagicReduction instance(EMagicElement elem) {
		AttributeMagicReduction instance = instances.get(elem); 
		if (instance == null) {
			instance = new AttributeMagicReduction(elem);
			instances.put(elem, instance);
		}
		
		return instance;
	}
	
	private final EMagicElement element;
	
	private AttributeMagicReduction(EMagicElement elem) {
		super(GetUnlocName(elem), 0, -20.0D, 20.0D);
		this.element = elem;
		this.setShouldWatch(true);
	}
	
	public EMagicElement getElement() {
		return element;
	}
	
	protected static String GetUnlocName(EMagicElement elem) {
		return unlocalized_name_prefix + elem.name().toLowerCase();
	}
	
	public String getUnlocName() {
		return GetUnlocName(element);
	}
}
