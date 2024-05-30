package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;

/**
 * An ingredient to a spell. A more symbolic version of a rune in a rune slot.
 * @author Skyler
 *
 */
public class SpellIngredient {
	
	public final @Nullable SpellShapePart shape;
	public final @Nullable EMagicElement element;
	public final @Nullable EAlteration alteration;
	
	public final int weight;
	public final float manaRate;
	public final int elementCountBonus;
	public final float efficiency;
	
	private SpellIngredient(SpellShapePart shape, EMagicElement element, EAlteration alteration,
			int weight, float manaRate, int elementCountBonus, float efficiency) {
		this.shape = shape;
		this.element = element;
		this.alteration = alteration;
		this.weight = weight;
		this.manaRate = manaRate;
		this.elementCountBonus = elementCountBonus;
		this.efficiency = efficiency;
	}
	
	public SpellIngredient(SpellShapePart shape, int weight, float manaRate) {
		this(shape, null, null, weight, manaRate, 0, 1f);
	}
	
	public SpellIngredient(EMagicElement element, int weight, float manaRate, int elementCountBonus, float efficiency) {
		this(null, element, null, weight, manaRate, elementCountBonus, efficiency);
	}
	
	public SpellIngredient(EAlteration alteration, int weight, float manaRate, float efficiency) {
		this(null, null, alteration, weight, manaRate, 0, efficiency);
	}
	
}
