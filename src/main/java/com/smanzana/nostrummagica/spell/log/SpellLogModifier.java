package com.smanzana.nostrummagica.spell.log;

import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;

public abstract class SpellLogModifier {

	protected final ITextComponent label;
	protected final ESpellLogModifierType type;
	private final float modifier;
	
	public SpellLogModifier(ESpellLogModifierType type, ITextComponent label, float modifier) {
		this.type = type;
		this.label = label;
		this.modifier = modifier;
	}
	
	public abstract ITextComponent getDescription();
	
	public float getModifier() {
		return modifier;
	}

	protected ESpellLogModifierType getType() {
		return type;
	}
	
	protected static abstract class Percentage extends SpellLogModifier {
		
		private final ITextComponent description;

		public Percentage(ESpellLogModifierType type, ITextComponent label, float modifier, TextFormatting negColor, TextFormatting posColor) {
			super(type, label, modifier);
			
			description = label.copy().append(new StringTextComponent(": ")).append(
					new StringTextComponent(String.format("%+.0f%%", modifier * 100f)).withStyle(modifier <= 0 ? negColor : posColor)
				);
		}

		@Override
		public ITextComponent getDescription() {
			return description;
		}
	}
	
	protected static class BonusScale extends Percentage {
		public BonusScale(ITextComponent label, float modifier) {
			super(ESpellLogModifierType.BONUS_SCALE, label, modifier, TextFormatting.RED, TextFormatting.GREEN);
		}
	}
	
	protected static class ResistScale extends Percentage {
		public ResistScale(ITextComponent label, float modifier) {
			super(ESpellLogModifierType.RESIST_SCALE, label, modifier, TextFormatting.DARK_RED, TextFormatting.DARK_BLUE);
		}
	}
	
	protected static abstract class Flat extends SpellLogModifier {
		
		private final ITextComponent description;

		public Flat(ESpellLogModifierType type, ITextComponent label, float modifier, TextFormatting negColor, TextFormatting posColor) {
			super(type, label, modifier);
			
			description = label.copy().append(new StringTextComponent(": ")).append(
					new StringTextComponent(String.format("%+.1f", modifier)).withStyle(modifier <= 0 ? negColor : posColor)
				);
		}

		@Override
		public ITextComponent getDescription() {
			return description;
		}
	}
	
	protected static class BaseFlat extends Flat {
		public BaseFlat(ITextComponent label, float modifier) {
			super(ESpellLogModifierType.BASE_FLAT, label, modifier, TextFormatting.YELLOW, TextFormatting.GOLD);
		}
	}
	
	protected static class FinalFlat extends Flat {
		public FinalFlat(ITextComponent label, float modifier) {
			super(ESpellLogModifierType.FINAL_FLAT, label, modifier, TextFormatting.RED, TextFormatting.BLUE);
		}
	}
	
	public static final SpellLogModifier Make(ITextComponent label, float modifier, ESpellLogModifierType type) {
		switch (type) {
		case BASE_FLAT:
			return new BaseFlat(label, modifier);
		case BONUS_SCALE:
			return new BonusScale(label, modifier);
		case FINAL_FLAT:
			return new FinalFlat(label, modifier);
		case RESIST_SCALE:
			return new ResistScale(label, modifier);
		}
		
		return null;
	}
	
}
