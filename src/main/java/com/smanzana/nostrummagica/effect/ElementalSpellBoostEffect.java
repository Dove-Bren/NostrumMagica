package com.smanzana.nostrummagica.effect;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Effect consumed by a spell effect of the right type.
 * @author Skyler
 *
 */
public class ElementalSpellBoostEffect extends MobEffect {

	public static final String ID_PHYSICAL = "spellboost_physical";
	public static final String ID_EARTH = "spellboost_earth";
	public static final String ID_FIRE = "spellboost_fire";
	public static final String ID_ENDER = "spellboost_ender";
	public static final String ID_ICE = "spellboost_ice";
	public static final String ID_LIGHTNING = "spellboost_lightning";
	public static final String ID_WIND = "spellboost_wind";
	
	protected final @Nonnull EMagicElement element;
	
	public ElementalSpellBoostEffect(EMagicElement element) {
		super(MobEffectCategory.HARMFUL, element.getColor());
		this.element = element;
	}
	
	public static ElementalSpellBoostEffect GetForElement(EMagicElement element) {
		if (element == null) {
			element = EMagicElement.PHYSICAL;
		}
		switch (element) {
		case EARTH:
			return NostrumEffects.spellBoostEarth;
		case ENDER:
			return NostrumEffects.spellBoostEnder;
		case FIRE:
			return NostrumEffects.spellBoostFire;
		case ICE:
			return NostrumEffects.spellBoostIce;
		case LIGHTNING:
			return NostrumEffects.spellBoostLightning;
		case PHYSICAL:
			return NostrumEffects.spellBoostPhysical;
		case WIND:
			return NostrumEffects.spellBoostWind;
		}
		
		return null;
	}
}
