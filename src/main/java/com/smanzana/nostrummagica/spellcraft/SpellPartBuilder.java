package com.smanzana.nostrummagica.spellcraft;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

public class SpellPartBuilder {

	public final SpellPart base;
	
	protected int weightModifier;
	protected float manaRate;

	protected int elementCountModifier;
	protected @Nullable EMagicElement elementOverride;
	protected @Nullable EAlteration alterationOverride;
	protected @Nullable SpellShape shapeOverride;
	protected @Nullable SpellTrigger triggerOverride;
	
	public SpellPartBuilder(SpellPart base) {
		this.base = base;
		
		weightModifier = 0;
		manaRate = 1f;
		elementCountModifier = 0;
		elementOverride = null;
		alterationOverride = null;
		shapeOverride = null;
		triggerOverride = null;
	}
	
	public SpellPartBuilder addWeightModifier(int delta) {
		this.weightModifier += delta;
		return this;
	}
	
	public SpellPartBuilder addManaRate(float delta) {
		this.manaRate += delta;
		return this;
	}
	
	public SpellPartBuilder addElementCountModifier(int delta) {
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

	public void setTriggerOverride(SpellTrigger triggerOverride) {
		this.triggerOverride = triggerOverride;
	}

	public SpellPart getBase() {
		return base;
	}

	public int getWeightModifier() {
		return weightModifier;
	}

	public float getManaRate() {
		return manaRate;
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

	public SpellTrigger getTriggerOverride() {
		return triggerOverride;
	}
	
	public int getCurrentWeight() {
		return Math.max(0, SpellCrafting.CalculateWeight(base) + this.getWeightModifier());
	}

	public int getCurrentMana() {
		return (int) Math.ceil(SpellCrafting.CalculateManaCost(base) * this.getManaRate());
	}

	public int getCurrentElementCount() {
		if (base.isTrigger()) {
			return 0;
		}
		
		return Math.max(0, Math.min(3, base.getElementCount() + elementCountModifier));
	}

	public @Nullable EMagicElement getCurrentElement() {
		if (base.isTrigger()) {
			return null;
		}
		
		return getElementOverride() == null ? base.getElement() : getElementOverride();
	}

	public @Nullable EAlteration getCurrentAlteration() {
		if (base.isTrigger()) {
			return null;
		}
		
		return getAlterationOverride() == null ? base.getAlteration() : getAlterationOverride();
	}

	public @Nullable SpellShape getCurrentShape() {
		if (base.isTrigger()) {
			return null;
		}
		
		return getShapeOverride() == null ? base.getShape() : getShapeOverride();
	}

	public @Nullable SpellTrigger getCurrentTrigger() {
		if (!base.isTrigger()) {
			return null;
		}
		
		return getTriggerOverride() == null ? base.getTrigger() : getTriggerOverride();
	}
	
	public final SpellPart build() {
		if (base.isTrigger()) {
			return new SpellPart(this.getCurrentTrigger(), base.getParam());
		} else {
			return new SpellPart(this.getCurrentShape(), this.getCurrentElement(), this.getCurrentElementCount(), this.getCurrentAlteration(), base.getParam());
		}
	}
}
