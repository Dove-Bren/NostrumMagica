package com.smanzana.nostrummagica.spells.components;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;

/**
 * Individual effect part of a spell
 * @author Skyler
 *
 */
public class SpellEffectPart {

	private final @Nullable EAlteration alteration;
	private final EMagicElement element;
	private final int elementCount;
	private final float potency;
	
	public SpellEffectPart(@Nullable EAlteration alteration, EMagicElement element, int elementCount, float potency) {
		super();
		this.alteration = alteration;
		this.element = element;
		this.elementCount = elementCount;
		this.potency = potency;
	}
	
	public SpellEffectPart(EMagicElement element, int elementCount, @Nullable EAlteration alteration, float potency) {
		this(alteration, element, elementCount, potency);
	}
	
	public SpellEffectPart(EMagicElement element, int elementCount, @Nullable EAlteration alteration) {
		this(alteration, element, elementCount, 1f);
	}

	public @Nullable EAlteration getAlteration() {
		return alteration;
	}

	public EMagicElement getElement() {
		return element;
	}

	public int getElementCount() {
		return elementCount;
	}
	
	public float getPotency() {
		return this.potency;
	}
	
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_ELEMENT_COUNT = "element_count";
	private static final String NBT_ALTERATION = "alteration";
	private static final String NBT_POTENCY = "potency";
	
	public CompoundNBT toNBT(@Nullable CompoundNBT tag) {
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		tag.putString(NBT_ELEMENT, getElement().name().toLowerCase());
		tag.putInt(NBT_ELEMENT_COUNT, this.getElementCount());
		if (getAlteration() != null) {
			tag.putString(NBT_ALTERATION, this.getAlteration().name().toLowerCase());
		}
		tag.putFloat(NBT_POTENCY, getPotency());
		
		return tag;
	}
	
	public static SpellEffectPart FromNBT(CompoundNBT tag) {
		@Nullable EAlteration alteration = null;
		EMagicElement element = EMagicElement.PHYSICAL;
		int elementCount = 1;
		float potency = 1f;
		
		if (tag.contains(NBT_ELEMENT, NBT.TAG_STRING)) {
			try {
				element = EMagicElement.valueOf(tag.getString(NBT_ELEMENT).toUpperCase());
			} catch (Exception e) {
				
			}
		}
		
		if (tag.contains(NBT_ELEMENT_COUNT, NBT.TAG_INT)) {
			elementCount = tag.getInt(NBT_ELEMENT_COUNT);
		}
		
		if (tag.contains(NBT_ALTERATION, NBT.TAG_STRING)) {
			try {
				alteration = EAlteration.valueOf(tag.getString(NBT_ALTERATION).toUpperCase());
			} catch (Exception e) {
				
			}
		}
		if (tag.contains(NBT_POTENCY, NBT.TAG_FLOAT)) {
			potency = tag.getFloat(NBT_POTENCY);
		}
		
		return new SpellEffectPart(alteration, element, elementCount, potency);
	}
}
