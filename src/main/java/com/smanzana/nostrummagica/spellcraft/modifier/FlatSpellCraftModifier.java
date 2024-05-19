package com.smanzana.nostrummagica.spellcraft.modifier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellPartBuilder;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

public class FlatSpellCraftModifier implements ISpellCraftModifier {
	
	protected final int weightModifier;
	protected final float manaRateModifier;

	protected final int elementCountModifier;
	protected final @Nullable EMagicElement elementOverride;
	protected final @Nullable EAlteration alterationOverride;
	protected final @Nullable SpellShape shapeOverride;
	protected final @Nullable SpellTrigger triggerOverride;
	
	protected FlatSpellCraftModifier(int weightModifier, float manaRateModifier, int elementCountModifier,
			EMagicElement elementOverride, EAlteration alterationOverride, SpellShape shapeOverride,
			SpellTrigger triggerOverride) {
		super();
		this.weightModifier = weightModifier;
		this.manaRateModifier = manaRateModifier;
		this.elementCountModifier = elementCountModifier;
		this.elementOverride = elementOverride;
		this.alterationOverride = alterationOverride;
		this.shapeOverride = shapeOverride;
		this.triggerOverride = triggerOverride;
	}

	@Override
	public boolean canModify(SpellCraftContext context, SpellPart originalPart) {
		return true;
	}

	@Override
	public void modify(SpellCraftContext context, SpellPart originalPart, SpellPartBuilder builder) {
		builder.addWeightModifier(weightModifier)
			.addManaRate(manaRateModifier)
			.addElementCountModifier(elementCountModifier);
		
		if (elementOverride != null) {
			builder.setElementOverride(elementOverride);
		}
		
		if (alterationOverride != null) {
			builder.setAlterationOverride(alterationOverride);
		}
		
		if (shapeOverride != null) {
			builder.setShapeOverride(shapeOverride);
		}
		
		if (triggerOverride != null) {
			builder.setTriggerOverride(triggerOverride);
		}
	}
	
	public static final class Builder {
		
		protected int weightModifier = 0;
		protected float manaRateModifier = 0;

		protected int elementCountModifier = 0;
		protected @Nullable EMagicElement elementOverride;
		protected @Nullable EAlteration alterationOverride;
		protected @Nullable SpellShape shapeOverride;
		protected @Nullable SpellTrigger triggerOverride;
		
		public Builder() {
			
		}

		public Builder weight(int weightModifier) {
			this.weightModifier = weightModifier;
			return this;
		}

		public Builder manaRate(float manaRateModifier) {
			this.manaRateModifier = manaRateModifier;
			return this;
		}

		public Builder elementCount(int elementCountModifier) {
			this.elementCountModifier = elementCountModifier;
			return this;
		}

		public Builder overrideElement(EMagicElement elementOverride) {
			this.elementOverride = elementOverride;
			return this;
		}

		public Builder overrideAlteration(EAlteration alterationOverride) {
			this.alterationOverride = alterationOverride;
			return this;
		}

		public Builder overrideShape(SpellShape shapeOverride) {
			this.shapeOverride = shapeOverride;
			return this;
		}

		public Builder overrideTrigger(SpellTrigger triggerOverride) {
			this.triggerOverride = triggerOverride;
			return this;
		}
		
		public FlatSpellCraftModifier build() {
			return new FlatSpellCraftModifier(weightModifier, manaRateModifier, elementCountModifier, elementOverride, alterationOverride, shapeOverride, triggerOverride);
		}
		
	}

}
