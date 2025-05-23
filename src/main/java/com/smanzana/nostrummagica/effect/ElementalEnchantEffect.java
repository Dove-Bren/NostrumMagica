package com.smanzana.nostrummagica.effect;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ElementalEnchantEffect extends MobEffect {

	public static final String ID_PHYSICAL = "enchant_physical";
	public static final String ID_EARTH = "enchant_earth";
	public static final String ID_FIRE = "enchant_fire";
	public static final String ID_ENDER = "enchant_ender";
	public static final String ID_ICE = "enchant_ice";
	public static final String ID_LIGHTNING = "enchant_lightning";
	public static final String ID_WIND = "enchant_wind";
	private static final String MOD_UUID_PHYSICAL = "29722cd0-345e-4fd2-9695-a8e794288dcc";
	private static final String MOD_UUID_EARTH = "21098b4d-eefa-4894-9cb5-f89997edc5a7";
	private static final String MOD_UUID_FIRE = "84c5972a-401f-4210-8c46-47b241ac5228";
	private static final String MOD_UUID_ENDER = "fe6a1c87-69c3-4415-b48c-248aab83f1b5";
	private static final String MOD_UUID_ICE = "d9d76ef4-63f3-4644-95aa-17147ab66406";
	private static final String MOD_UUID_LIGHTNING = "57c96a89-97fe-4720-b519-6fa11720a722";
	private static final String MOD_UUID_WIND = "5a0964a6-ebba-4239-8f61-4e2727db9616";
	
	protected static final String GetModID(EMagicElement element) {
		switch (element) {
		case EARTH:
			return MOD_UUID_EARTH;
		case ENDER:
			return MOD_UUID_ENDER;
		case FIRE:
			return MOD_UUID_FIRE;
		case ICE:
			return MOD_UUID_ICE;
		case LIGHTNING:
			return MOD_UUID_LIGHTNING;
		case PHYSICAL:
			return MOD_UUID_PHYSICAL;
		case WIND:
			return MOD_UUID_WIND;
		}
		return null;
	}
	
	protected final @Nonnull EMagicElement element;
	
	public ElementalEnchantEffect(EMagicElement element) {
		super(MobEffectCategory.NEUTRAL, element.getColor());
		
		this.element = element;
		
		if (element == EMagicElement.PHYSICAL) {
			this.addAttributeModifier(Attributes.ARMOR, GetModID(this.element), 2D, AttributeModifier.Operation.ADDITION);
			for (EMagicElement weakElem : EMagicElement.values()) {
				if (weakElem == EMagicElement.PHYSICAL) {
					continue;
				}
				this.addAttributeModifier(NostrumAttributes.GetReduceAttribute(weakElem), GetModID(this.element), -.5D, AttributeModifier.Operation.ADDITION);
			}
		} else {
			this.addAttributeModifier(NostrumAttributes.GetReduceAttribute(this.element), GetModID(this.element), 1D, AttributeModifier.Operation.ADDITION);
			this.addAttributeModifier(NostrumAttributes.GetReduceAttribute(this.element.getOpposite()), GetModID(this.element), -1D, AttributeModifier.Operation.ADDITION);
		}
	}
	
	public static ElementalEnchantEffect GetForElement(EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		switch (element) {
		case EARTH:
			return NostrumEffects.enchantEarth;
		case ENDER:
			return NostrumEffects.enchantEnder;
		case FIRE:
			return NostrumEffects.enchantFire;
		case ICE:
			return NostrumEffects.enchantIce;
		case LIGHTNING:
			return NostrumEffects.enchantLightning;
		case PHYSICAL:
			return NostrumEffects.enchantPhysical;
		case WIND:
			return NostrumEffects.enchantWind;
		}
		
		return null;
	}
}
