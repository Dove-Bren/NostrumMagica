package com.smanzana.nostrummagica.attribute;

import javax.annotation.Nullable;

import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public interface IPrintableAttribute {

	@Nullable
	public ITextComponent formatModifier(AttributeModifier modifier);
	
	@Nullable
	public static ITextComponent formatAttributeValueVanilla(Attribute attribute, AttributeModifier modifier) {
		double val = modifier.getAmount();
		if (val == 0) {
			return null;
		}

		// Formatting here copied from Vanilla
		if (val > 0) {
			return (new TranslationTextComponent("attribute.modifier.plus." + modifier.getOperation().toValue(),
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(val),
					new TranslationTextComponent(attribute.getDescriptionId())))
							.withStyle(TextFormatting.BLUE);
		} else {
			val = -val;
			return (new TranslationTextComponent("attribute.modifier.take." + modifier.getOperation().toValue(),
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(val),
					new TranslationTextComponent(attribute.getDescriptionId())))
							.withStyle(TextFormatting.RED);
		}
	}

	@Nullable
	public static ITextComponent formatAttributeValuePercentage(Attribute attribute, AttributeModifier modifier) {
		double val = modifier.getAmount();
		if (val == 0) {
			return null;
		}
		

		final String op;
		final TextFormatting color;
		if (val > 0) {
			 op = "plus";
			 color = TextFormatting.BLUE;
		} else {
			 op = "take";
			 color = TextFormatting.RED;
		}
		
		final String transKey;
		switch (modifier.getOperation()) {
		case ADDITION:
		default:
			transKey = "attribute.perc.modifier." + op + ".0";
			break;
		case MULTIPLY_BASE:
			transKey = "attribute.perc.modifier." + op + ".1";
			val += 100.0;
			break;
		case MULTIPLY_TOTAL:
			transKey = "attribute.perc.modifier." + op + ".2";
			val += 100.0;
			break;
		}
		
		return new TranslationTextComponent(transKey,
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(val > 0 ? val : -val),
					new TranslationTextComponent(attribute.getDescriptionId())
				).withStyle(color);
	}
	
	public static interface IPercentageAttribute extends IPrintableAttribute {
		@Nullable
		public default ITextComponent formatModifier(AttributeModifier modifier) {
			return IPrintableAttribute.formatAttributeValuePercentage((Attribute) this, modifier);
		}
	}
}
