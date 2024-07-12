package com.smanzana.nostrummagica.spell;

import net.minecraft.nbt.CompoundNBT;

/**
 * Wrapper for the different characteristics of a spell.
 * For exaple, is the spell harmful?
 * @author Skyler
 *
 */
public class SpellCharacteristics {
	
	private static final String NBT_HARMFUL = "harmful";
	private static final String NBT_ELEMENT = "element";

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
	
	public CompoundNBT toNBT() {
		CompoundNBT tag = new CompoundNBT();
		
		tag.putBoolean(NBT_HARMFUL, harmful);
		tag.put(NBT_ELEMENT, element.toNBT());
		
		return tag;
	}
	
	public static final SpellCharacteristics FromNBT(CompoundNBT tag) {
		return new SpellCharacteristics(
				tag.getBoolean(NBT_HARMFUL),
				EMagicElement.FromNBT(tag.get(NBT_ELEMENT))
				);
	}
	
}
