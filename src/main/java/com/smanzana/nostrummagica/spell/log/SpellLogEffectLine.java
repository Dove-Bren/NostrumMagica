package com.smanzana.nostrummagica.spell.log;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.potion.Effect;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public abstract class SpellLogEffectLine {

	protected final List<SpellLogModifier> modifiers;

	public SpellLogEffectLine(List<SpellLogModifier> modifiers) {
		this.modifiers = modifiers;
	}

	public List<SpellLogModifier> getModifiers() {
		return modifiers;
	}
	
	public abstract boolean isHarmful();
	
	public abstract ITextComponent getName();
	
	public abstract ITextComponent getDescription();
	
	public abstract float getTotalDamage();
	
	public abstract float getTotalHeal();
	
	public abstract static class NumericSpellLogEffectLine extends SpellLogEffectLine {
		protected final float base;
		protected final float total;
		
		public NumericSpellLogEffectLine(float base, float total, List<SpellLogModifier> modifiers) {
			super(modifiers);
			this.base = base;
			this.total = total;
		}

		public float getBase() {
			return base;
		}

		public float getTotal() {
			return total;
		}
	}
	
	public static class Damage extends NumericSpellLogEffectLine {

		private final TranslationTextComponent name;
		private final TranslationTextComponent desc;
		private final EMagicElement element;
		
		public Damage(float base, float total, @Nullable EMagicElement element, List<SpellLogModifier> modifiers) {
			super(base, total, modifiers);
			
			name = new TranslationTextComponent("spelllog.damage.name");
			desc = new TranslationTextComponent("spelllog.damage.desc", String.format("%.1f", base), String.format("%.1f", total), element == null ? "" : (element.getName() + " "));
			this.element = element;
		}

		@Override
		public boolean isHarmful() {
			return true;
		}

		@Override
		public ITextComponent getDescription() {
			return desc;
		}

		@Override
		public float getTotalDamage() {
			return this.total;
		}

		@Override
		public float getTotalHeal() {
			return 0;
		}
		
		public @Nullable EMagicElement getElement() {
			return this.element;
		}

		@Override
		public ITextComponent getName() {
			return name;
		}
	}
	
	public static class Heal extends NumericSpellLogEffectLine {

		private final TranslationTextComponent name;
		private final TranslationTextComponent desc;
		private final EMagicElement element;
		
		public Heal(float base, float total, @Nullable EMagicElement element, List<SpellLogModifier> modifiers) {
			super(base, total, modifiers);

			name = new TranslationTextComponent("spelllog.heal.name");
			desc = new TranslationTextComponent("spelllog.heal.desc", String.format("%.1f", base), String.format("%.1f", total), element == null ? "" : (element.getName() + " "));
			this.element = element;
		}

		@Override
		public boolean isHarmful() {
			return false;
		}

		@Override
		public ITextComponent getDescription() {
			return desc;
		}

		@Override
		public float getTotalDamage() {
			return 0;
		}

		@Override
		public float getTotalHeal() {
			return this.total;
		}
		
		public @Nullable EMagicElement getElement() {
			return this.element;
		}

		@Override
		public ITextComponent getName() {
			return name;
		}
	}
	
	public static class Status extends NumericSpellLogEffectLine {
		
		private final Effect effect;
		private final TranslationTextComponent name;
		private final TranslationTextComponent desc;
		
		public Status(Effect effect, float base, float total, List<SpellLogModifier> modifiers) {
			super(base, total, modifiers);
			this.effect = effect;

			final ITextComponent effectName = effect.getDisplayName().deepCopy().mergeStyle(this.isHarmful() ? TextFormatting.RED : TextFormatting.DARK_BLUE);
			name = new TranslationTextComponent("spelllog.status.name", effectName);
			desc = new TranslationTextComponent("spelllog.status.desc", effectName, base, total);
		}

		@Override
		public boolean isHarmful() {
			return !effect.isBeneficial();
		}

		@Override
		public ITextComponent getDescription() {
			return desc;
		}

		@Override
		public float getTotalDamage() {
			return 0;
		}

		@Override
		public float getTotalHeal() {
			return 0;
		}

		@Override
		public ITextComponent getName() {
			return name;
		}
	}
	
	public static class General extends SpellLogEffectLine {

		private final ITextComponent name;
		private final ITextComponent description;
		private final boolean harmful;
		private final float damage;
		private final float heal;

		public General(boolean harmful, float damage, float heal, ITextComponent name, ITextComponent description, List<SpellLogModifier> modifiers) {
			super(modifiers);
			this.description = description;
			this.harmful = harmful;
			this.damage = damage;
			this.heal = heal;
			this.name = name;
		}

		public General(boolean harmful, ITextComponent name, ITextComponent description, List<SpellLogModifier> modifiers) {
			this(harmful, 0f, 0f, name, description, modifiers);
		}

		@Override
		public boolean isHarmful() {
			return this.harmful;
		}

		@Override
		public ITextComponent getDescription() {
			return this.description;
		}

		@Override
		public float getTotalDamage() {
			return damage;
		}

		@Override
		public float getTotalHeal() {
			return heal;
		}

		@Override
		public ITextComponent getName() {
			return name;
		}
		
	}
}
