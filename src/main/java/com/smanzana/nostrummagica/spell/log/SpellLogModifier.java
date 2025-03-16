package com.smanzana.nostrummagica.spell.log;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;

public abstract class SpellLogModifier {

	protected final Component label;
	protected final ESpellLogModifierType type;
	private final float modifier;
	
	public SpellLogModifier(ESpellLogModifierType type, Component label, float modifier) {
		this.type = type;
		this.label = label;
		this.modifier = modifier;
	}
	
	public abstract Component getDescription();
	
	public float getModifier() {
		return modifier;
	}

	protected ESpellLogModifierType getType() {
		return type;
	}
	
	protected static abstract class Percentage extends SpellLogModifier {
		
		private final Component description;

		public Percentage(ESpellLogModifierType type, Component label, float modifier, ChatFormatting negColor, ChatFormatting posColor) {
			super(type, label, modifier);
			
			description = label.copy().append(new TextComponent(": ")).append(
					new TextComponent(String.format("%+.0f%%", modifier * 100f)).withStyle(modifier <= 0 ? negColor : posColor)
				);
		}

		@Override
		public Component getDescription() {
			return description;
		}
	}
	
	protected static class BonusScale extends Percentage {
		public BonusScale(Component label, float modifier) {
			super(ESpellLogModifierType.BONUS_SCALE, label, modifier, ChatFormatting.RED, ChatFormatting.GREEN);
		}
	}
	
	protected static class ResistScale extends Percentage {
		public ResistScale(Component label, float modifier) {
			super(ESpellLogModifierType.RESIST_SCALE, label, modifier, ChatFormatting.DARK_RED, ChatFormatting.DARK_BLUE);
		}
	}
	
	protected static abstract class Flat extends SpellLogModifier {
		
		private final Component description;

		public Flat(ESpellLogModifierType type, Component label, float modifier, ChatFormatting negColor, ChatFormatting posColor) {
			super(type, label, modifier);
			
			description = label.copy().append(new TextComponent(": ")).append(
					new TextComponent(String.format("%+.1f", modifier)).withStyle(modifier <= 0 ? negColor : posColor)
				);
		}

		@Override
		public Component getDescription() {
			return description;
		}
	}
	
	protected static class BaseFlat extends Flat {
		public BaseFlat(Component label, float modifier) {
			super(ESpellLogModifierType.BASE_FLAT, label, modifier, ChatFormatting.YELLOW, ChatFormatting.GOLD);
		}
	}
	
	protected static class FinalFlat extends Flat {
		public FinalFlat(Component label, float modifier) {
			super(ESpellLogModifierType.FINAL_FLAT, label, modifier, ChatFormatting.RED, ChatFormatting.BLUE);
		}
	}
	
	public static final SpellLogModifier Make(Component label, float modifier, ESpellLogModifierType type) {
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
