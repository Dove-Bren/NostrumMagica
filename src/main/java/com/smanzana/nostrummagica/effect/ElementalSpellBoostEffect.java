package com.smanzana.nostrummagica.effect;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.DisplayEffectsScreen;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Effect consumed by a spell effect of the right type.
 * @author Skyler
 *
 */
public class ElementalSpellBoostEffect extends Effect {

	public static final String ID_PHYSICAL = "spellboost_physical";
	public static final String ID_EARTH = "spellboost_earth";
	public static final String ID_FIRE = "spellboost_fire";
	public static final String ID_ENDER = "spellboost_ender";
	public static final String ID_ICE = "spellboost_ice";
	public static final String ID_LIGHTNING = "spellboost_lightning";
	public static final String ID_WIND = "spellboost_wind";
	
	protected final @Nonnull EMagicElement element;
	
	public ElementalSpellBoostEffect(EMagicElement element) {
		super(EffectType.HARMFUL, element.getColor());
		this.element = element;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderInventoryEffect(EffectInstance effect, DisplayEffectsScreen<?> gui, MatrixStack matrixStackIn, int x, int y, float z) {
		;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
    public void renderHUDEffect(EffectInstance effect, AbstractGui gui, MatrixStack matrixStackIn, int x, int y, float z, float alpha) {
		;
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
