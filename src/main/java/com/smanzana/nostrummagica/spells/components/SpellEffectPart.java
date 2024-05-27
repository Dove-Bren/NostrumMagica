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
	
	public SpellEffectPart(@Nullable EAlteration alteration, EMagicElement element, int elementCount) {
		super();
		this.alteration = alteration;
		this.element = element;
		this.elementCount = elementCount;
	}
	
	public SpellEffectPart(EMagicElement element, int elementCount, @Nullable EAlteration alteration) {
		this(alteration, element, elementCount);
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
	
	private static final String NBT_ELEMENT = "element";
	private static final String NBT_ELEMENT_COUNT = "element_count";
	private static final String NBT_ALTERATION = "alteration";
	
	public CompoundNBT toNBT(@Nullable CompoundNBT tag) {
		if (tag == null) {
			tag = new CompoundNBT();
		}
		
		tag.putString(NBT_ELEMENT, getElement().name().toLowerCase());
		tag.putInt(NBT_ELEMENT_COUNT, this.getElementCount());
		if (getAlteration() != null) {
			tag.putString(NBT_ALTERATION, this.getAlteration().name().toLowerCase());
		}
		
		return tag;
	}
	
	public static SpellEffectPart FromNBT(CompoundNBT tag) {
		@Nullable EAlteration alteration = null;
		EMagicElement element = EMagicElement.PHYSICAL;
		int elementCount = 1;
		
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
		
		return new SpellEffectPart(alteration, element, elementCount);
	}
}
