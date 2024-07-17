package com.smanzana.nostrummagica.spell.log;

import java.util.List;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellLocation;

import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.Effect;
import net.minecraft.util.text.ITextComponent;

public interface ISpellLogBuilder {
	
	public default void flush() { }
	
	public default ISpellLogBuilder stage(ITextComponent label, int ticksElapsed, List<LivingEntity> affectedEnts, List<SpellLocation> affectedLocs) { return this; }
	
	public default ISpellLogBuilder effect(LivingEntity entity) { return this; }
	
	public default ISpellLogBuilder effect(SpellLocation location) { return this; }
	
	public default ISpellLogBuilder endEffect() { return this; }
	
	public default ISpellLogBuilder damageStart(float baseDamage, @Nullable EMagicElement element) { return this; }
	
	public default ISpellLogBuilder damageFinish(float finalDamage) { return this; }
	
	public default ISpellLogBuilder healStart(float baseHeal, @Nullable EMagicElement element) { return this; }
	
	public default ISpellLogBuilder healFinish(float finalHeal) { return this; }
	
	public default ISpellLogBuilder statusStart(Effect effect, int baseDuration) { return this; }
	
	public default ISpellLogBuilder statusFinish(int finalDuration) { return this; }
	
	public default ISpellLogBuilder generalEffectStart(ITextComponent description, boolean harmful) { return this; }
	
	public default ISpellLogBuilder generalEffectFinish(float finalDmg, float finalHeal) { return this; }
	
	public default ISpellLogBuilder effectMod(ITextComponent label, float amt, boolean flat) { return this; }
	
	public default ISpellLogBuilder pushModifierStack() { return this; }
	
	public default ISpellLogBuilder popModifierStack() { return this; }
	
	public default ISpellLogBuilder addGlobalModifier(ITextComponent label, float amt, boolean flat) { return this; }

	public default ISpellLogBuilder addGlobalModifier(Skill wind_Novice, float amt, boolean flat) { return this; }
	
	public static class DummyImpl implements ISpellLogBuilder {
		
	}
	
	public static final DummyImpl Dummy = new DummyImpl();
}
