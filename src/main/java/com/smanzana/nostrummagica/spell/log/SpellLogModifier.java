package com.smanzana.nostrummagica.spell.log;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public abstract class SpellLogModifier {

	protected final ITextComponent label;
	private final float modifier;
	
	public SpellLogModifier(ITextComponent label, float modifier) {
		this.label = label;
		this.modifier = modifier;
	}
	
	public abstract ITextComponent getDescription();
	
	public float getModifier() {
		return modifier;
	}
	
	public static class Percentage extends SpellLogModifier {
		
		private final ITextComponent description;

		public Percentage(ITextComponent label, float modifier) {
			super(label, modifier);
			
			description = label.deepCopy().append(new StringTextComponent(": ")).append(
					new StringTextComponent(String.format("%+.0f%%", modifier * 100f)).mergeStyle(modifier <= 0 ? TextFormatting.RED : TextFormatting.GREEN)
				);
		}

		@Override
		public ITextComponent getDescription() {
			return description;
		}
	}
	
	public static class Flat extends SpellLogModifier {
		
		private final ITextComponent description;

		public Flat(ITextComponent label, float modifier) {
			super(label, modifier);
			
			description = label.deepCopy().append(new StringTextComponent(": ")).append(
					new StringTextComponent(String.format("%+.1f", modifier)).mergeStyle(modifier <= 0 ? TextFormatting.RED : TextFormatting.GREEN)
				);
		}

		@Override
		public ITextComponent getDescription() {
			return description;
		}
	}
	
}
