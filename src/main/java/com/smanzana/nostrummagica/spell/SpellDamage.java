package com.smanzana.nostrummagica.spell;

import javax.annotation.Nullable;

import com.google.common.collect.Multimap;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.dragon.DragonEntity;
import com.smanzana.nostrummagica.entity.dragon.ShadowRedDragonEntity;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.progression.skill.Skill;
import com.smanzana.nostrummagica.spell.log.ESpellLogModifierType;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attribute;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.monster.EndermiteEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SpellDamage {
	
	private static final class Damage {
		private final float base;
		private float baseFlat;
		private float bonusScale;
		private float resistScale;
		private float finalFlat;
		private final ISpellLogBuilder log;
		
		public Damage(float base, float startingBonus, ISpellLogBuilder log) {
			this.base = base;
			this.log = log;
			baseFlat = 0f;
			bonusScale = startingBonus;
			resistScale = 1f;
			finalFlat = 0f;
		}
		
		public final float calc() {
			final float baseAdj = Math.max(0, base + baseFlat);
			final float preFin = baseAdj * Math.max(0f, bonusScale) * Math.max(0f, resistScale);
			final float fin = preFin + finalFlat;
			//LogDamage(base, fin, "Result");
			return fin;
		}
		
		private final void baseFlatInternal(String cause, float scale) {
			final float pre = calc();
			this.baseFlat += scale;
			final float post = calc();
			LogDamage(pre, post, cause);
		}
		
		private final void bonusScaleInternal(String cause, float scale) {
			final float pre = calc();
			this.bonusScale += scale;
			final float post = calc();
			LogDamage(pre, post, cause);
		}
		
		private final void resistScaleInternal(String cause, float scale) {
			final float pre = calc();
			this.resistScale += scale;
			final float post = calc();
			LogDamage(pre, post, cause);
		}
		
		private final void finalFlatInternal(String cause, float scale) {
			final float pre = calc();
			this.finalFlat += scale;
			final float post = calc();
			LogDamage(pre, post, cause);
		}
		
		private final void baseFlat(String cause, float flat) {
			baseFlatInternal(cause, flat);
			log.effectMod(MakeLabel(cause), flat, ESpellLogModifierType.BASE_FLAT);
		}
		
		private final void baseFlat(Skill skill, float flat) {
			baseFlatInternal("Skill: " + skill.getName().getString(), flat);
			log.effectMod(skill, flat, ESpellLogModifierType.BASE_FLAT);
		}
		
		private final void bonusScale(String cause, float scale) {
			bonusScaleInternal(cause, scale);
			log.effectMod(MakeLabel(cause), scale, ESpellLogModifierType.BONUS_SCALE);
		}
		
		private final void bonusScale(Skill skill, float scale) {
			bonusScaleInternal("Skill: " + skill.getName().getString(), scale);
			log.effectMod(skill, scale, ESpellLogModifierType.BONUS_SCALE);
		}
		
		private final void resistScale(String cause, float scale) {
			resistScaleInternal(cause, scale);
			log.effectMod(MakeLabel(cause), scale, ESpellLogModifierType.RESIST_SCALE);
		}
		
		private final void resistScale(Skill skill, float scale) {
			resistScaleInternal("Skill: " + skill.getName().getString(), scale);
			log.effectMod(skill, scale, ESpellLogModifierType.RESIST_SCALE);
		}
		
		private final void finalFlat(String cause, float flat) {
			finalFlatInternal(cause, flat);
			log.effectMod(MakeLabel(cause), flat, ESpellLogModifierType.FINAL_FLAT);
		}
		
		private final void finalFlat(Skill skill, float flat) {
			finalFlatInternal("Skill: " + skill.getName().getString(), flat);
			log.effectMod(skill, flat, ESpellLogModifierType.FINAL_FLAT);
		}
	}

	protected static final float GetPhysicalAttributeBonusNoMainhand(@Nullable LivingEntity caster) {
		// Get raw amount
		if (caster == null || !caster.getAttributeManager().hasAttributeInstance(Attributes.ATTACK_DAMAGE)) {
			return 0f;
		}
		
		double amt = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
		amt -= 1; // Players always have +1 attack
		
		// Reduce any from main-hand weapon, since that's given assuming it's used to attack
		ItemStack held = caster.getHeldItemMainhand();
		if (!held.isEmpty()) {
			final Multimap<Attribute, AttributeModifier> heldAttribs = held.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			if (heldAttribs != null && heldAttribs.containsKey(Attributes.ATTACK_DAMAGE)) {
				double extra = 0;
				for (AttributeModifier mod : heldAttribs.get(Attributes.ATTACK_DAMAGE)) {
					extra += mod.getAmount();
				}
				
				amt -= extra;
			}
		}
		
		return (float) amt;
	}

	protected static final float GetPhysicalAttributeBonusMainhand(@Nullable LivingEntity caster) {
		// Get raw amount
		if (caster == null || !caster.getAttributeManager().hasAttributeInstance(Attributes.ATTACK_DAMAGE)) {
			return 0f;
		}
		
		double amt = 0;
		
		// Reduce any from main-hand weapon, since that's given assuming it's used to attack
		ItemStack held = caster.getHeldItemMainhand();
		if (!held.isEmpty()) {
			final Multimap<Attribute, AttributeModifier> heldAttribs = held.getAttributeModifiers(EquipmentSlotType.MAINHAND);
			if (heldAttribs != null && heldAttribs.containsKey(Attributes.ATTACK_DAMAGE)) {
				for (AttributeModifier mod : heldAttribs.get(Attributes.ATTACK_DAMAGE)) {
					amt += mod.getAmount();
				}
			}
		}
		
		return (float) amt;
	}
	
	protected static final float GetArmorModifier(LivingEntity target, final Damage damage) {
		int i = 25 - target.getTotalArmorValue();
		return (float)i / 25f;
	}
	
	private static boolean DamageConsoleLog = false;
	private static final void LogDamage(float before, float after, String msg) {
		if (DamageConsoleLog) {
			NostrumMagica.logger.info(String.format("%s [%.2f -> %.2f][%+.2f]", msg, before, after, after-before));
		}
	}
	
	private static final ITextComponent MakeLabel(String cause) {
		return new TranslationTextComponent("spelllogmod.nostrummagica." + cause.toLowerCase());
	}
	
	public static final float CalculateDamage(@Nullable LivingEntity caster, LivingEntity target, float baseDamage, float efficiency, EMagicElement element, ISpellLogBuilder log) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		final @Nullable INostrumMagic magic = caster == null ? null : NostrumMagica.getMagicWrapper(caster);
		
		LogDamage(0, baseDamage, "Start");
		final Damage damage = new Damage(baseDamage, efficiency, log);
		
		if (element == EMagicElement.PHYSICAL) {
			// Physical is reduced by real armor but not affected by magic resist effects and attributes.
			// It still gains power from magic boost/magic damage AND the strength status effect/attack attribute AND is still reduces with magic reduction (below).
			final float baseAttribute = GetPhysicalAttributeBonusNoMainhand(caster);
			final float mainhand = GetPhysicalAttributeBonusMainhand(caster);
			damage.baseFlat("PhysicalAttributeBase", baseAttribute);
			
			// Note that the physical master skill includes using some of this
			final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Physical_Master)) {
				damage.baseFlat(NostrumSkills.Physical_Master, (int) (mainhand * .2f));
			}
		} else {
		
			final int armor = target.getTotalArmorValue();
			final boolean undead = target.isEntityUndead();
			final boolean ender;
			final boolean light;
			final boolean flamy;
			
			if (target instanceof EndermanEntity || target instanceof EndermiteEntity
					|| target instanceof DragonEntity) {
				// Ender status and immunity can be turned off with the disrupt status effect
				EffectInstance effect = target.getActivePotionEffect(NostrumEffects.disruption);
				if (effect != null && effect.getDuration() > 0) {
					ender = false;
				} else {
					ender = true;
				}
			} else {
				ender = false;
			}
			
			if (target.getHeight() < 1.5f || target instanceof EndermanEntity || target instanceof ShadowRedDragonEntity) {
				light = true;
			} else {
				light = false;
			}
			
			if (target.isImmuneToFire()) {
				flamy = true;
			} else {
				flamy = false;
			}
			
			if (element == EMagicElement.FIRE && magic != null && magic.hasSkill(NostrumSkills.Fire_Adept)) {
				damage.baseFlat(NostrumSkills.Fire_Adept, 2);
			}
			
			if (element == EMagicElement.LIGHTNING && magic != null && magic.hasSkill(NostrumSkills.Lightning_Adept)) {
				damage.baseFlat(NostrumSkills.Lightning_Adept, 1);
			}
			
			if (element == EMagicElement.EARTH && magic != null && magic.hasSkill(NostrumSkills.Earth_Adept)) {
				EffectInstance strength = caster == null ? null : caster.getActivePotionEffect(Effects.STRENGTH);
				if (strength != null) {
					// Matches strength attribute boost
					damage.baseFlat(NostrumSkills.Earth_Adept, 3 * (strength.getAmplifier() + 1));
				}
			}
				
			switch (element) {
			case ENDER:
				if (ender) {
					damage.bonusScale("EnderXEnder", -1f);
					return 0.0f; // does not affect ender
				}
				damage.bonusScale("EnderDamage", +.2f); // return raw damage (+20%) not affected by armor
				break;
			case LIGHTNING:
				damage.bonusScale("LightningDamage", (.75f + ((float) armor / 20f)) -1f); // double in power for every 20 armor
				break;
			case FIRE:
				// 1.5x damage against undead. Half on flame-resistant. Regular otherwise
				if (undead) {
					damage.bonusScale("FireXUndead", +.5f); 
				} else if (flamy) {
					damage.bonusScale("FireXFire", -.5f); // 1.5x damage against undead. Regular otherwise
				}
				break;
			case EARTH:
				// No change for earth damage
				//damage.mul("EarthDamage", 1f); not useful for logging 
				break;
			case ICE:
				// More affective against everything except undead
				if (undead) {
					damage.bonusScale("IceXUndead", -.4f);
				} else {
					damage.bonusScale("IceDamage", +.3f);
				}
				if (target.isBurning()) {
					damage.bonusScale("IceDamageOnBurning", +1f);
				}
				break;
			case WIND:
				// 180% against light (endermen included) enemies
				if (light) {
					damage.bonusScale("WindLightDamage", +.8f);
				} else {
					damage.bonusScale("WindHeavyDamage", -.2f);
				}
				break;
			default:
				//base;
				break;
			}
		}
		
		// Apply boosts and resist
		ModifiableAttributeInstance attr = caster == null ? null : caster.getAttribute(NostrumAttributes.magicDamage);
		if (attr != null && attr.getValue() != 0.0D) {
			damage.bonusScale("MagicDamageAttribute", (Math.max(0, Math.min(100, 1f + (float)(attr.getValue() / 100.0)))) - 1f);
		}
		
		// No magic resist for physical; it uses armor value
		if (element != EMagicElement.PHYSICAL) {
			attr = target.getAttribute(NostrumAttributes.magicResist);
			if (attr != null && attr.getValue() != 0.0D) {
				damage.resistScale("MagicResistAttribute", (float) (Math.max(0.0D, Math.min(2.0D, 1.0D - (attr.getValue() / 100.0D)))) - 1f);
			}
		} else {
			damage.resistScale("PhysicalArmor", GetArmorModifier(target, damage) - 1f);
		}
		
		// Apply armor reductions
		attr = target.getAttribute(NostrumAttributes.GetReduceAttribute(element));
		if (attr != null && attr.getValue() != 0.0D) {
			damage.finalFlat("FlatReducAttribute", (float) -attr.getValue());
		}
		
		return damage.calc();
	}
	
	public static final float DamageEntity(LivingEntity target, EMagicElement element, float base, float efficiency, @Nullable LivingEntity source, ISpellLogBuilder log) {
		final float damage = CalculateDamage(source, target, base, efficiency, element, log);
		target.attackEntityFrom(new MagicDamageSource(source, element), damage);
		return damage;
	}
	
	public static final float DamageEntity(LivingEntity target, EMagicElement element, float base, @Nullable LivingEntity source) {
		return DamageEntity(target, element, base, 1f, source, ISpellLogBuilder.Dummy);
	}
	
}
