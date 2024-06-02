package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

public class SpellIngredientBuilder {

	public final SpellIngredient base;
	
	protected @Nullable SpellShape shapeOverride;
	
	protected int weightModifier;
	protected float manaRate;
	
	protected float efficiency;
	protected int elementCountModifier;
	protected @Nullable EMagicElement elementOverride;
	protected @Nullable EAlteration alterationOverride;
	
	public SpellIngredientBuilder(SpellIngredient base) {
		this.base = base;
		
		weightModifier = 0;
		manaRate = 1f;
		efficiency = 1f;
		elementCountModifier = 0;
		elementOverride = null;
		alterationOverride = null;
		shapeOverride = null;
	}
	
	public SpellIngredientBuilder addWeightModifier(int delta) {
		this.weightModifier += delta;
		return this;
	}
	
	public SpellIngredientBuilder addManaRate(float delta) {
		this.manaRate += delta;
		return this;
	}
	
	public SpellIngredientBuilder addEfficiency(float delta) {
		this.efficiency += delta;
		return this;
	}
	
	public SpellIngredientBuilder addElementCountModifier(int delta) {
		this.elementCountModifier += delta;
		return this;
	}

	public void setWeightModifier(int weightModifier) {
		this.weightModifier = weightModifier;
	}

	public void setElementOverride(EMagicElement elementOverride) {
		this.elementOverride = elementOverride;
	}

	public void setAlterationOverride(EAlteration alterationOverride) {
		this.alterationOverride = alterationOverride;
	}
	
	public void setShapeOverride(SpellShape shapeOverride) {
		this.shapeOverride = shapeOverride;
	}

	public SpellIngredient getBase() {
		return base;
	}

	public int getWeightModifier() {
		return weightModifier;
	}

	public float getManaRate() {
		return manaRate;
	}
	
	public float getEfficiency() {
		return efficiency;
	}

	public int getElementCountModifier() {
		return elementCountModifier;
	}

	public EMagicElement getElementOverride() {
		return elementOverride;
	}

	public EAlteration getAlterationOverride() {
		return alterationOverride;
	}
	
	public SpellShape getShapeOverride() {
		return shapeOverride;
	}

	public int getCurrentWeight() {
		return base.weight + this.getWeightModifier();
	}

	public float getCurrentMana() {
		return base.manaRate * this.getManaRate();
	}
	
	public float getCurrentEfficiency() {
		if (base.element == null) {
			return 1f;
		}
		
		return base.efficiency * this.getEfficiency();
	}

	public int getCurrentElementCountBonus() {
		if (base.element == null) {
			return 0;
		}
		
		return Math.max(0, Math.min(3, elementCountModifier));
	}

	public @Nullable EMagicElement getCurrentElement() {
		return getElementOverride() == null ? base.element : getElementOverride();
	}

	public @Nullable EAlteration getCurrentAlteration() {
		return getAlterationOverride() == null ? base.alteration : getAlterationOverride();
	}
	
	public @Nullable SpellShape getCurrentShape() {
		return getShapeOverride() == null ? base.shape.getShape() : getShapeOverride();
	}

	public final SpellIngredient build() {
		// Prefer element, then alteration, then shape since that goes from most-likely-to-produce-a-good-spell to least
		if (getElementOverride() != null) {
			return new SpellIngredient(getCurrentElement(), getCurrentWeight(), getCurrentMana(), getCurrentElementCountBonus(), getCurrentEfficiency());
		}
		if (getAlterationOverride() != null) {
			return new SpellIngredient(getCurrentAlteration(), getCurrentWeight(), getCurrentMana(), getCurrentEfficiency());
		}
		if (getShapeOverride() != null) {
			return new SpellIngredient(new SpellShapePart(getCurrentShape(), base.shape.getProperties()), getCurrentWeight(), getCurrentMana());
		}

		if (getCurrentElement() != null) {
			return new SpellIngredient(getCurrentElement(), getCurrentWeight(), getCurrentMana(), getCurrentElementCountBonus(), getCurrentEfficiency());
		}
		if (getCurrentAlteration() != null) {
			return new SpellIngredient(getCurrentAlteration(), getCurrentWeight(), getCurrentMana(), getCurrentEfficiency());
		}
		if (getCurrentShape() != null) {
			return new SpellIngredient(new SpellShapePart(getCurrentShape(), base.shape.getProperties()), getCurrentWeight(), getCurrentMana());
		}
		
		NostrumMagica.logger.error("Null spell ingredient");
		return new SpellIngredient(EMagicElement.PHYSICAL, getCurrentWeight(), getCurrentMana(), getCurrentElementCountBonus(), getCurrentEfficiency());
	}
}
