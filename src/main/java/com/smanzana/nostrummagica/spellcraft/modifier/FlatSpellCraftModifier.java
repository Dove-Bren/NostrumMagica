package com.smanzana.nostrummagica.spellcraft.modifier;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellIngredient;
import com.smanzana.nostrummagica.spellcraft.SpellIngredientBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class FlatSpellCraftModifier implements ISpellCraftModifier {
	
	protected static final ChatFormatting STYLE_GOOD = ChatFormatting.DARK_GREEN;
	protected static final ChatFormatting STYLE_BAD = ChatFormatting.RED;
	protected static final ChatFormatting STYLE_OVERRIDE = ChatFormatting.DARK_PURPLE;
	
	protected final int weightModifier;
	protected final float manaRateModifier;

	protected final int elementCountModifier;
	protected final float efficiencyModifier;
	protected final @Nullable EMagicElement elementOverride;
	protected final @Nullable EAlteration alterationOverride;
	protected final @Nullable SpellShape shapeOverride;
	
	protected FlatSpellCraftModifier(int weightModifier, float manaRateModifier, int elementCountModifier, float efficiencyModifier,
			EMagicElement elementOverride, EAlteration alterationOverride, SpellShape shapeOverride) {
		super();
		this.weightModifier = weightModifier;
		this.manaRateModifier = manaRateModifier;
		this.elementCountModifier = elementCountModifier;
		this.elementOverride = elementOverride;
		this.alterationOverride = alterationOverride;
		this.shapeOverride = shapeOverride;
		this.efficiencyModifier = efficiencyModifier;
	}

	@Override
	public boolean canModify(SpellCraftContext context, SpellIngredient originalPart) {
		// If mana or weight is involved, it's definitely applicable
		if (this.weightModifier != 0 || this.manaRateModifier != 0) {
			return true;
		}
		
		if (originalPart.shape != null) {
			return this.shapeOverride != null;
		} else {
			return this.elementOverride != null
					|| this.elementCountModifier != 0
					|| this.alterationOverride != null
					|| this.efficiencyModifier != 0f;
		}
	}

	@Override
	public void modify(SpellCraftContext context, SpellIngredient originalPart, SpellIngredientBuilder builder) {
		builder.addWeightModifier(weightModifier)
			.addManaRate(manaRateModifier);
		
		if (originalPart.shape != null) {
			if (shapeOverride != null) {
				builder.setShapeOverride(shapeOverride);
			}
		} else {
			if (elementOverride != null) {
				builder.setElementOverride(elementOverride);
			}
			
			if (alterationOverride != null) {
				builder.setAlterationOverride(alterationOverride);
			}
			
			if (elementCountModifier != 0) {
				builder.addElementCountModifier(elementCountModifier);
			}
			
			builder.addEfficiency(efficiencyModifier);
		}
	}

	@Override
	public List<Component> getDetails(List<Component> lines) {
		if (weightModifier != 0) {
			lines.add(new TranslatableComponent("spellcraftmod.weight", (weightModifier < 0 ? "" : "+"), weightModifier)
					.withStyle(weightModifier < 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (manaRateModifier != 0) {
			final int manaPerc = (int) (manaRateModifier * 100);
			lines.add(new TranslatableComponent("spellcraftmod.mana", (manaRateModifier < 0 ? "" : "+"), manaPerc)
					.withStyle(manaRateModifier < 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (efficiencyModifier != 0) {
			final int effPerc = (int) (efficiencyModifier * 100);
			lines.add(new TranslatableComponent("spellcraftmod.efficiency", (efficiencyModifier < 0 ? "" : "+"), effPerc)
					.withStyle(elementCountModifier > 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (elementCountModifier != 0) {
			lines.add(new TranslatableComponent("spellcraftmod.elementcount", (elementCountModifier < 0 ? "" : "+"), elementCountModifier)
					.withStyle(elementCountModifier > 0 ? STYLE_GOOD : STYLE_BAD));
		}
		if (elementOverride != null) {
			lines.add(new TranslatableComponent("spellcraftmod.override.element", elementOverride.getDisplayName())
					.withStyle(STYLE_OVERRIDE));
		}
		if (alterationOverride != null) {
			lines.add(new TranslatableComponent("spellcraftmod.override.alteration", alterationOverride.getDisplayName())
					.withStyle(STYLE_OVERRIDE));
		}
		if (shapeOverride != null) {
			lines.add(new TranslatableComponent("spellcraftmod.override.shape", shapeOverride.getDisplayName())
					.withStyle(STYLE_OVERRIDE));
		}
		
		return lines;
	}
	
	public static final class Builder {
		
		protected int weightModifier = 0;
		protected float manaRateModifier = 0;

		protected float efficiencyModifier = 0;
		protected int elementCountModifier = 0;
		protected @Nullable EMagicElement elementOverride;
		protected @Nullable EAlteration alterationOverride;
		protected @Nullable SpellShape shapeOverride;
		
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
		
		public Builder efficiency(float efficiencyModifier) {
			this.efficiencyModifier = efficiencyModifier;
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

		public FlatSpellCraftModifier build() {
			return new FlatSpellCraftModifier(weightModifier, manaRateModifier, elementCountModifier, efficiencyModifier, elementOverride, alterationOverride, shapeOverride);
		}
		
	}

}
