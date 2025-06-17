package com.smanzana.nostrummagica.spell.log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class SpellLogBuilder implements ISpellLogBuilder {
	
	private static abstract class EffectBuilder {
		private final List<SpellLogModifier> lineModifiers;
		
		protected EffectBuilder(List<SpellLogModifier> baseModifiers) {
			this.lineModifiers = new ArrayList<>(baseModifiers);
		}
		
		public EffectBuilder modify(SpellLogModifier modifier) {
			this.lineModifiers.add(modifier);
			return this;
		}
		
		protected List<SpellLogModifier> buildModifiers() {
			lineModifiers.sort((a, b) -> a.getType().ordinal() - b.getType().ordinal());
			return this.lineModifiers;
		}
		
		public abstract SpellLogEffectLine build();
		
		// Dmg/Heal amt + general
		public EffectBuilder baseHeal(float amt) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder finalHeal(float amt) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder baseDamage(float amt) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder finalDamage(float amt) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder element(@Nullable EMagicElement elem) { throw new IllegalStateException("Building the wrong type of effect"); }
		
		// Effect
		public EffectBuilder effect(MobEffect effect) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder baseDuration(int duration) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder finalDuration(int duration) { throw new IllegalStateException("Building the wrong type of effect"); }
		
		// General
		public EffectBuilder harmful(boolean harmful) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder name(Component name) { throw new IllegalStateException("Building the wrong type of effect"); }
		public EffectBuilder desc(Component desc) { throw new IllegalStateException("Building the wrong type of effect"); }
	}
	
	private static final class DamageEffectBuilder extends EffectBuilder {
		
		private float baseDmg;
		private float finalDmg;
		private EMagicElement element;
		
		public DamageEffectBuilder(List<SpellLogModifier> baseModifiers) {
			super(baseModifiers);
			baseDmg = -1;
			finalDmg = -1;
		}

		@Override
		public EffectBuilder baseDamage(float amt) {
			baseDmg = amt;
			return this;
		}

		@Override
		public EffectBuilder finalDamage(float amt) {
			finalDmg = amt;
			return this;
		}
		
		@Override
		public EffectBuilder element(@Nullable EMagicElement elem) {
			this.element = elem;
			return this;
		}

		@Override
		public SpellLogEffectLine build() {
			if (baseDmg == -1 || finalDmg == -1) {
				throw new IllegalStateException("Didn't specify both base and final damage amounts");
			}
			return new SpellLogEffectLine.Damage(baseDmg, finalDmg, element, buildModifiers());
		}
	}
	
	private static final class HealEffectBuilder extends EffectBuilder {
		
		private float baseHeal;
		private float finalHeal;
		private EMagicElement element;
		
		public HealEffectBuilder(List<SpellLogModifier> baseModifiers) {
			super(baseModifiers);
			baseHeal = -1;
			finalHeal = -1;
		}

		@Override
		public EffectBuilder baseHeal(float amt) {
			baseHeal = amt;
			return this;
		}

		@Override
		public EffectBuilder finalHeal(float amt) {
			finalHeal = amt;
			return this;
		}
		
		@Override
		public EffectBuilder element(@Nullable EMagicElement elem) {
			this.element = elem;
			return this;
		}

		@Override
		public SpellLogEffectLine build() {
			if (baseHeal == -1 || finalHeal == -1) {
				throw new IllegalStateException("Didn't specify both base and final heal amounts");
			}
			return new SpellLogEffectLine.Heal(baseHeal, finalHeal, element, buildModifiers());
		}
	}
	
	private static final class StatusEffectBuilder extends EffectBuilder {
		
		private MobEffect effect;
		private int baseDuration;
		private int finalDuration;
		
		public StatusEffectBuilder(List<SpellLogModifier> baseModifiers) {
			super(baseModifiers);
			baseDuration = -1;
			finalDuration = -1;
		}
		
		@Override
		public EffectBuilder effect(MobEffect effect) {
			this.effect = effect;
			return this;
		}

		@Override
		public EffectBuilder baseDuration(int duration) {
			baseDuration = duration;
			return this;
		}

		@Override
		public EffectBuilder finalDuration(int duration) {
			finalDuration = duration;
			return this;
		}

		@Override
		public SpellLogEffectLine build() {
			if (effect == null || baseDuration == -1 || finalDuration == -1) {
				throw new IllegalStateException("Must specify effect, base duration, and final duration");
			}
			return new SpellLogEffectLine.Status(effect, baseDuration, finalDuration, buildModifiers());
		}
	}
	
	private static final class GeneralEffectBuilder extends EffectBuilder {
		
		private float amtDmg;
		private float amtHeal;
		private Component name;
		private Component desc;
		private boolean harmful;
		
		public GeneralEffectBuilder(List<SpellLogModifier> baseModifiers) {
			super(baseModifiers);
			amtDmg = -1;
			amtHeal = -1;
			desc = null;
			harmful = false;
		}

		@Override
		public EffectBuilder finalDamage(float amt) {
			amtDmg = amt;
			return this;
		}

		@Override
		public EffectBuilder finalHeal(float amt) {
			amtHeal = amt;
			return this;
		}
		
		@Override
		public EffectBuilder harmful(boolean harmful) {
			this.harmful = harmful;
			return this;
		}
		
		@Override
		public EffectBuilder name(Component name) {
			this.name = name;
			return this;
		}
		
		@Override
		public EffectBuilder desc(Component desc) {
			this.desc = desc;
			return this;
		}

		@Override
		public SpellLogEffectLine build() {
			if (amtDmg == -1 || amtHeal == -1 || desc == null || name == null) {
				throw new IllegalStateException("Must specify damage and heal amounts and a description");
			}
			return new SpellLogEffectLine.General(harmful, amtDmg, amtHeal, name, desc, buildModifiers());
		}
	}

	private SpellLogEntry log;
	
	private boolean buildingStage;
	private boolean buildingEffectSummary;
	private boolean buildingEffectLine;
	
	// Stage building
	private final Map<LivingEntity, SpellLogEffectSummary> stageEnts;
	private final Map<SpellLocation, SpellLogEffectSummary> stageLocs;
	private int stageIdx;
	private int stageTicks;
	private SpellShape stageShape;
	
	// Effect summary
	private LivingEntity summaryEntity;
	private SpellLocation summaryLocation;
	private final List<SpellLogEffectLine> summaryEffects;
	
	// Effect line building
	private EffectBuilder effectBuilder;
	private final List<List<SpellLogModifier>> modifierStack;
	
	public SpellLogBuilder(SpellLogEntry log) {
		this.log = log;
		
		this.stageEnts = new HashMap<>();
		this.stageLocs = new HashMap<>();
		this.stageTicks = -1;
		this.stageShape = null;
		
		this.summaryEntity = null;
		this.summaryLocation = null;
		this.summaryEffects = new ArrayList<>();
		
		this.effectBuilder = null;
		
		this.modifierStack = new ArrayList<>();
		this.modifierStack.add(new ArrayList<>());
	}
	
	protected boolean isBuilding() {
		return buildingStage || buildingEffectSummary || buildingEffectLine;
	}
	
	@Override
	public void flush() {
		if (isBuilding()) {
			if (!buildingStage) {
				throw new IllegalStateException("Was building effects but no stage!");
			}
			
			if (buildingEffectLine) {
				flushEffectLine();
			}
			
			if (buildingEffectSummary) {
				flushEffectSummary();
			}
			
			SpellLogStage stage = new SpellLogStage(new HashMap<>(stageEnts), new HashMap<>(stageLocs), stageTicks);
			
			buildingStage = false;
			stageEnts.clear();
			stageLocs.clear();
			stageTicks = -1;
			
			this.log.addStage(stageIdx, stageShape, stage);
			stageShape = null;
			stageIdx = -1;
		}
	}
	
	protected void flushEffectSummary() {
		if (buildingEffectSummary) {
			final SpellLogEffectSummary summary = new SpellLogEffectSummary(new ArrayList<>(this.summaryEffects));
			if (this.summaryEntity != null) {
				this.stageEnts.merge(this.summaryEntity, summary, (a, b) -> {
					List<SpellLogEffectLine> combined = new ArrayList<>(a.getElements().size() + b.getElements().size());
					combined.addAll(a.getElements());
					combined.addAll(b.getElements());
					return new SpellLogEffectSummary(combined);
				});
			} else {
				this.stageLocs.merge(this.summaryLocation, summary, (a, b) -> {
					List<SpellLogEffectLine> combined = new ArrayList<>(a.getElements().size() + b.getElements().size());
					combined.addAll(a.getElements());
					combined.addAll(b.getElements());
					return new SpellLogEffectSummary(combined);
				});
			}
			
			summaryEffects.clear();
		}
		
		buildingEffectSummary = false;
	}
	
	protected void flushEffectLine() {
		if (buildingEffectLine) {
			if (!buildingEffectSummary) {
				throw new IllegalStateException("Was building effect lines but no summary!");
			}
			
			this.summaryEffects.add(this.effectBuilder.build());
		}
		
		buildingEffectLine = false;
		effectBuilder = null;
	}

	@Override
	public SpellLogBuilder stage(int spellStageIdx, SpellShape shape, int ticksElapsed, List<LivingEntity> affectedEnts, List<SpellLocation> affectedLocs) {
		flush();
		
		buildingStage = true;
		this.stageIdx = spellStageIdx;
		this.stageTicks = ticksElapsed;
		this.stageShape = shape;
		
		if (affectedEnts != null)
		for (LivingEntity ent : affectedEnts) {
			this.stageEnts.put(ent, null);
		}
		
		if (affectedLocs != null)
		for (SpellLocation loc : affectedLocs) {
			this.stageLocs.put(loc, null);
		}
		
		return this;
	}

	@Override
	public SpellLogBuilder effect(LivingEntity entity) {
		if (this.buildingEffectSummary) {
			throw new IllegalStateException("Previous effect summary was not finished");
		}
		
		this.buildingEffectSummary = true;
		this.summaryEntity = entity;
		
		return this;
	}

	@Override
	public SpellLogBuilder effect(SpellLocation location) {
		if (this.buildingEffectSummary) {
			throw new IllegalStateException("Previous effect summary was not finished");
		}
		
		this.buildingEffectSummary = true;
		this.summaryLocation = location;
		
		return this;
	}

	@Override
	public SpellLogBuilder endEffect() {
		this.flushEffectSummary();
		return this;
	}

	@Override
	public SpellLogBuilder damageStart(float baseDamage, @Nullable EMagicElement element) {
		if (this.buildingEffectLine) {
			throw new IllegalStateException("Previous effect was not finished");
		}
		
		this.buildingEffectSummary = true;
		this.buildingEffectLine = true;
		this.effectBuilder = new DamageEffectBuilder(getModifiers());
		this.effectBuilder.baseDamage(baseDamage).element(element);
		
		return this;
	}

	@Override
	public SpellLogBuilder damageFinish(float finalDamage) {
		this.effectBuilder.finalDamage(finalDamage);
		flushEffectLine();
		return this;
	}

	@Override
	public SpellLogBuilder healStart(float baseHeal, @Nullable EMagicElement element) {
		if (this.buildingEffectLine) {
			throw new IllegalStateException("Previous effect was not finished");
		}
		
		this.buildingEffectSummary = true;
		this.buildingEffectLine = true;
		this.effectBuilder = new HealEffectBuilder(getModifiers());
		this.effectBuilder.baseHeal(baseHeal).element(element);
		
		return this;
	}

	@Override
	public SpellLogBuilder healFinish(float finalHeal) {
		this.effectBuilder.finalHeal(finalHeal);
		flushEffectLine();
		return this;
	}
	
	@Override
	public ISpellLogBuilder damageManaStart(int baseDamage, @Nullable EMagicElement element) { return this; }

	@Override
	public ISpellLogBuilder damageManaFinish(int finalDamage) { return this; }

	@Override
	public ISpellLogBuilder restoreManaStart(int baseHeal, @Nullable EMagicElement element) { return this; }

	@Override
	public ISpellLogBuilder restoreManaFinish(int finalHeal) { return this; }

	@Override
	public SpellLogBuilder statusStart(MobEffect effect, int baseDuration) {
		if (this.buildingEffectLine) {
			throw new IllegalStateException("Previous effect was not finished");
		}
		
		this.buildingEffectSummary = true;
		this.buildingEffectLine = true;
		this.effectBuilder = new StatusEffectBuilder(getModifiers());
		this.effectBuilder.effect(effect).baseDuration(baseDuration);
		
		return this;
	}

	@Override
	public SpellLogBuilder statusFinish(int finalDuration) {
		this.effectBuilder.finalDuration(finalDuration);
		flushEffectLine();
		return this;
	}

	@Override
	public SpellLogBuilder generalEffectStart(Component name, Component description, boolean harmful) {
		if (this.buildingEffectLine) {
			throw new IllegalStateException("Previous effect was not finished");
		}
		
		this.buildingEffectSummary = true;
		this.buildingEffectLine = true;
		this.effectBuilder = new GeneralEffectBuilder(getModifiers());
		this.effectBuilder.name(name).desc(description).harmful(harmful);
		
		return this;
	}

	@Override
	public SpellLogBuilder generalEffectFinish(float finalDmg, float finalHeal) {
		this.effectBuilder.finalDamage(finalDmg).finalHeal(finalHeal);
		flushEffectLine();
		return this;
	}

	@Override
	public SpellLogBuilder effectMod(Component label, float amt, ESpellLogModifierType type) {
		if (!this.buildingEffectLine || this.effectBuilder == null) {
			throw new IllegalStateException("Wasn't building an effect line, so can't add a modifier");
		}
		this.effectBuilder.modify(SpellLogModifier.Make(label, amt, type));
		return this;
	}
	
	@Override
	public ISpellLogBuilder effectMod(Skill skill, float amt, ESpellLogModifierType type) {
		return effectMod(makeSkillLabel(skill), amt, type);
	}

	@Override
	public SpellLogBuilder pushModifierStack() {
		this.modifierStack.add(new ArrayList<>(this.modifierStack.get(this.modifierStack.size() - 1)));
		return this;
	}

	@Override
	public SpellLogBuilder popModifierStack() {
		this.modifierStack.remove(this.modifierStack.size() - 1);
		if (this.modifierStack.isEmpty()) {
			throw new IllegalStateException("Popped too many modifiers");
		}
		return this;
	}
	
	@Override
	public ISpellLogBuilder addGlobalModifier(Component label, float amt, ESpellLogModifierType type) {
		getModifiers().add(SpellLogModifier.Make(label, amt, type));
		return this;
	}
	
	@Override
	public ISpellLogBuilder addGlobalModifier(Skill skill, float amt, ESpellLogModifierType type) {
		return addGlobalModifier(makeSkillLabel(skill), amt, type);
	}
	
	protected List<SpellLogModifier> getModifiers() {
		return this.modifierStack.get(this.modifierStack.size() - 1);
	}
	
	protected Component makeSkillLabel(Skill skill) {
		return new TextComponent("")
			.append(new TranslatableComponent("spelllogmod.nostrummagica.skill").withStyle(ChatFormatting.GOLD))
			.append(skill.getName().copy());
	}
	
}
