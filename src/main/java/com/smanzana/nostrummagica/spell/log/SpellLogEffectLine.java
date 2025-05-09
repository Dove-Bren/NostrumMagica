package com.smanzana.nostrummagica.spell.log;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public abstract class SpellLogEffectLine {

	protected final List<SpellLogModifier> modifiers;

	public SpellLogEffectLine(List<SpellLogModifier> modifiers) {
		this.modifiers = modifiers;
	}

	public List<SpellLogModifier> getModifiers() {
		return modifiers;
	}
	
	public abstract boolean isHarmful();
	
	public abstract Component getName();
	
	public abstract Component getDescription();
	
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

		private final TranslatableComponent name;
		private final TranslatableComponent desc;
		private final EMagicElement element;
		
		public Damage(float base, float total, @Nullable EMagicElement element, List<SpellLogModifier> modifiers) {
			super(base, total, modifiers);
			
			name = new TranslatableComponent("spelllog.damage.name");
			desc = new TranslatableComponent("spelllog.damage.desc", String.format("%.1f", base), String.format("%.1f", total), element == null ? TextComponent.EMPTY : element.getDisplayName());
			this.element = element;
		}

		@Override
		public boolean isHarmful() {
			return true;
		}

		@Override
		public Component getDescription() {
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
		public Component getName() {
			return name;
		}
	}
	
	public static class Heal extends NumericSpellLogEffectLine {

		private final TranslatableComponent name;
		private final TranslatableComponent desc;
		private final EMagicElement element;
		
		public Heal(float base, float total, @Nullable EMagicElement element, List<SpellLogModifier> modifiers) {
			super(base, total, modifiers);

			name = new TranslatableComponent("spelllog.heal.name");
			desc = new TranslatableComponent("spelllog.heal.desc", String.format("%.1f", base), String.format("%.1f", total), element == null ? TextComponent.EMPTY : element.getDisplayName());
			this.element = element;
		}

		@Override
		public boolean isHarmful() {
			return false;
		}

		@Override
		public Component getDescription() {
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
		public Component getName() {
			return name;
		}
	}
	
	public static class Status extends NumericSpellLogEffectLine {
		
		private final MobEffect effect;
		private final TranslatableComponent name;
		private final TranslatableComponent desc;
		
		public Status(MobEffect effect, float base, float total, List<SpellLogModifier> modifiers) {
			super(base, total, modifiers);
			this.effect = effect;

			final Component effectName = effect.getDisplayName().copy().withStyle(this.isHarmful() ? ChatFormatting.RED : ChatFormatting.DARK_BLUE);
			name = new TranslatableComponent("spelllog.status.name", effectName);
			desc = new TranslatableComponent("spelllog.status.desc", effectName, base, total);
		}

		@Override
		public boolean isHarmful() {
			return !effect.isBeneficial();
		}

		@Override
		public Component getDescription() {
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
		public Component getName() {
			return name;
		}
	}
	
	public static class General extends SpellLogEffectLine {

		private final Component name;
		private final Component description;
		private final boolean harmful;
		private final float damage;
		private final float heal;

		public General(boolean harmful, float damage, float heal, Component name, Component description, List<SpellLogModifier> modifiers) {
			super(modifiers);
			this.description = description;
			this.harmful = harmful;
			this.damage = damage;
			this.heal = heal;
			this.name = name;
		}

		public General(boolean harmful, Component name, Component description, List<SpellLogModifier> modifiers) {
			this(harmful, 0f, 0f, name, description, modifiers);
		}

		@Override
		public boolean isHarmful() {
			return this.harmful;
		}

		@Override
		public Component getDescription() {
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
		public Component getName() {
			return name;
		}
		
	}
}
