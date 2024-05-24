package com.smanzana.nostrummagica.spellcraft.modifier;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellPartBuilder;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class FlatSpellCraftModifier implements ISpellCraftModifier {
	
	protected static final TextFormatting STYLE_GOOD = TextFormatting.DARK_GREEN;
	protected static final TextFormatting STYLE_BAD = TextFormatting.RED;
	protected static final TextFormatting STYLE_OVERRIDE = TextFormatting.DARK_PURPLE;
	
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
		// If mana or weight is involved, it's definitely applicable
		if (this.weightModifier != 0 || this.manaRateModifier != 0) {
			return true;
		}
		if (originalPart.isTrigger()) {
			return this.triggerOverride != null;
		} else {
			return this.elementCountModifier != 0
					|| this.shapeOverride != null
					|| this.alterationOverride != null
					|| this.elementOverride != null;
		}
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

	@Override
	public List<ITextComponent> getDetails(List<ITextComponent> lines) {
		if (weightModifier != 0) {
			lines.add(new TranslationTextComponent("spellcraftmod.weight", (weightModifier < 0 ? "" : "+"), weightModifier)
					.mergeStyle(weightModifier < 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (manaRateModifier != 0) {
			final int manaPerc = (int) (manaRateModifier * 100);
			lines.add(new TranslationTextComponent("spellcraftmod.mana", (manaRateModifier < 0 ? "" : "+"), manaPerc)
					.mergeStyle(manaRateModifier < 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (elementCountModifier != 0) {
			lines.add(new TranslationTextComponent("spellcraftmod.elementcount", (elementCountModifier < 0 ? "" : "+"), elementCountModifier)
					.mergeStyle(elementCountModifier > 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (elementOverride != null) {
			lines.add(new TranslationTextComponent("spellcraftmod.override.element", elementOverride.getName())
					.mergeStyle(STYLE_OVERRIDE));
		}
		if (alterationOverride != null) {
			lines.add(new TranslationTextComponent("spellcraftmod.override.alteration", alterationOverride.getName())
					.mergeStyle(STYLE_OVERRIDE));
		}
		if (shapeOverride != null) {
			lines.add(new TranslationTextComponent("spellcraftmod.override.shape", shapeOverride.getDisplayName())
					.mergeStyle(STYLE_OVERRIDE));
		}
		if (triggerOverride != null) {
			lines.add(new TranslationTextComponent("spellcraftmod.override.trigger", triggerOverride.getDisplayName())
					.mergeStyle(STYLE_OVERRIDE));
		}
		
		return lines;
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
