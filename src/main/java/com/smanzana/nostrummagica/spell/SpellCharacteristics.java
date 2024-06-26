package com.smanzana.nostrummagica.spell;

/**
 * Wrapper for the different characteristics of a spell.
 * For exaple, is the spell harmful?
 * @author Skyler
 *
 */
public class SpellCharacteristics {

	public final boolean harmful;
	public final EMagicElement element;
	
	public SpellCharacteristics(boolean harmful, EMagicElement element) {
		super();
		this.harmful = harmful;
		this.element = element;
	}

	public boolean isHarmful() {
		return harmful;
	}

	public EMagicElement getElement() {
		return element;
	}
	
}
