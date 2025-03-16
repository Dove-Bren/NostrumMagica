package com.smanzana.nostrummagica.spell.log;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.network.chat.Component;

public interface ISpellLogBuilder {
	
	public default void flush() { }
	
	public default ISpellLogBuilder stage(int stageIdx, SpellShape shape, int ticksElapsed, List<LivingEntity> affectedEnts, List<SpellLocation> affectedLocs) { return this; }
	
	public default ISpellLogBuilder effect(LivingEntity entity) { return this; }
	
	public default ISpellLogBuilder effect(SpellLocation location) { return this; }
	
	public default ISpellLogBuilder endEffect() { return this; }
	
	public default ISpellLogBuilder damageStart(float baseDamage, @Nullable EMagicElement element) { return this; }
	
	public default ISpellLogBuilder damageFinish(float finalDamage) { return this; }
	
	public default ISpellLogBuilder healStart(float baseHeal, @Nullable EMagicElement element) { return this; }
	
	public default ISpellLogBuilder healFinish(float finalHeal) { return this; }
	
	public default ISpellLogBuilder statusStart(MobEffect effect, int baseDuration) { return this; }
	
	public default ISpellLogBuilder statusFinish(int finalDuration) { return this; }
	
	public default ISpellLogBuilder generalEffectStart(Component name, Component description, boolean harmful) { return this; }
	
	public default ISpellLogBuilder generalEffectFinish(float finalDmg, float finalHeal) { return this; }
	
	public default ISpellLogBuilder effectMod(Component label, float amt, ESpellLogModifierType type) { return this; }
	
	public default ISpellLogBuilder effectMod(Skill skill, float amt, ESpellLogModifierType type) { return this; }
	
	public default ISpellLogBuilder pushModifierStack() { return this; }
	
	public default ISpellLogBuilder popModifierStack() { return this; }
	
	public default ISpellLogBuilder addGlobalModifier(Component label, float amt, ESpellLogModifierType type) { return this; }

	public default ISpellLogBuilder addGlobalModifier(Skill skill, float amt, ESpellLogModifierType type) { return this; }
	
	public static class DummyImpl implements ISpellLogBuilder {
		
	}
	
	public static final DummyImpl Dummy = new DummyImpl();
}
