package com.smanzana.nostrummagica.attribute;

import javax.annotation.Nullable;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public interface IPrintableAttribute {

	@Nullable
	public Component formatModifier(AttributeModifier modifier);
	
	@Nullable
	public static Component formatAttributeValueVanilla(Attribute attribute, AttributeModifier modifier) {
		double val = modifier.getAmount();
		if (val == 0) {
			return null;
		}

		// Formatting here copied from Vanilla
		if (val > 0) {
			return (new TranslatableComponent("attribute.modifier.plus." + modifier.getOperation().toValue(),
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(val),
					new TranslatableComponent(attribute.getDescriptionId())))
							.withStyle(ChatFormatting.BLUE);
		} else {
			val = -val;
			return (new TranslatableComponent("attribute.modifier.take." + modifier.getOperation().toValue(),
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(val),
					new TranslatableComponent(attribute.getDescriptionId())))
							.withStyle(ChatFormatting.RED);
		}
	}

	@Nullable
	public static Component formatAttributeValuePercentage(Attribute attribute, AttributeModifier modifier) {
		double val = modifier.getAmount();
		if (val == 0) {
			return null;
		}
		

		final String op;
		final ChatFormatting color;
		if (val > 0) {
			 op = "plus";
			 color = ChatFormatting.BLUE;
		} else {
			 op = "take";
			 color = ChatFormatting.RED;
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
		
		return new TranslatableComponent(transKey,
					ItemStack.ATTRIBUTE_MODIFIER_FORMAT.format(val > 0 ? val : -val),
					new TranslatableComponent(attribute.getDescriptionId())
				).withStyle(color);
	}
	
	public static interface IPercentageAttribute extends IPrintableAttribute {
		@Nullable
		public default Component formatModifier(AttributeModifier modifier) {
			return IPrintableAttribute.formatAttributeValuePercentage((Attribute) this, modifier);
		}
	}
}
