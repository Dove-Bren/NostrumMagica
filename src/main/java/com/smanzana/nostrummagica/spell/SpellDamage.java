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
import com.smanzana.nostrummagica.util.AttributeUtil;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Endermite;
import net.minecraft.world.item.ItemStack;

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

		public String paramStr() {
			// BaseAdJ = (base + baseFlat)
			// SemiFinal = BaseAdj * bonusScale * resistScale
			// Final = SemiFinal + finalFlat;
			return String.format("(%.1f + %.1f) x %.1f x %.1f + %.1f", base, baseFlat, bonusScale, resistScale, finalFlat);
		}
		
		public final float calc() {
			final float baseAdj = Math.max(0, base + baseFlat);
			final float preFin = baseAdj * Math.max(0f, bonusScale) * Math.max(0f, resistScale);
			final float fin = Math.max(0, preFin + finalFlat);
			//LogDamage(base, fin, "Result");
			return fin;
		}
		
		private final void baseFlatInternal(String cause, float scale) {
			final float pre = calc();
			this.baseFlat += scale;
			final float post = calc();
			LogDamage(pre, post, this, cause);
		}
		
		private final void bonusScaleInternal(String cause, float scale) {
			final float pre = calc();
			this.bonusScale += scale;
			final float post = calc();
			LogDamage(pre, post, this, cause);
		}
		
		private final void resistScaleInternal(String cause, float scale) {
			final float pre = calc();
			this.resistScale += scale;
			final float post = calc();
			LogDamage(pre, post, this, cause);
		}
		
		private final void finalFlatInternal(String cause, float scale) {
			final float pre = calc();
			this.finalFlat += scale;
			final float post = calc();
			LogDamage(pre, post, this, cause);
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
		
		@SuppressWarnings("unused")
		private final void bonusScale(Skill skill, float scale) {
			bonusScaleInternal("Skill: " + skill.getName().getString(), scale);
			log.effectMod(skill, scale, ESpellLogModifierType.BONUS_SCALE);
		}
		
		private final void resistScale(String cause, float scale) {
			resistScaleInternal(cause, scale);
			log.effectMod(MakeLabel(cause), scale, ESpellLogModifierType.RESIST_SCALE);
		}

		@SuppressWarnings("unused")
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
		if (caster == null || !caster.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
			return 0f;
		}
		
		double amt = caster.getAttributeValue(Attributes.ATTACK_DAMAGE);
		amt -= 1; // Players always have +1 attack
		
		// Reduce any from main-hand weapon, since that's given assuming it's used to attack
		ItemStack held = caster.getMainHandItem();
		if (!held.isEmpty()) {
			final Multimap<Attribute, AttributeModifier> heldAttribs = held.getAttributeModifiers(EquipmentSlot.MAINHAND);
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
		if (caster == null || !caster.getAttributes().hasAttribute(Attributes.ATTACK_DAMAGE)) {
			return 0f;
		}
		
		double amt = 0;
		
		// Reduce any from main-hand weapon, since that's given assuming it's used to attack
		ItemStack held = caster.getMainHandItem();
		if (!held.isEmpty()) {
			final Multimap<Attribute, AttributeModifier> heldAttribs = held.getAttributeModifiers(EquipmentSlot.MAINHAND);
			if (heldAttribs != null && heldAttribs.containsKey(Attributes.ATTACK_DAMAGE)) {
				for (AttributeModifier mod : heldAttribs.get(Attributes.ATTACK_DAMAGE)) {
					amt += mod.getAmount();
				}
			}
		}
		
		return (float) amt;
	}
	
	protected static final float GetArmorModifier(LivingEntity target, final Damage damage) {
		int i = 25 - target.getArmorValue();
		return (float)i / 25f;
	}
	
	private static boolean DamageConsoleLog = false;
	private static final void LogDamage(float before, float after, Damage damage, String msg) {
		if (DamageConsoleLog) {
			NostrumMagica.logger.info(String.format("%s {%s}[%.2f -> %.2f][%+.2f]", msg, damage.paramStr(), before, after, after-before));
		}
	}
	
	private static final Component MakeLabel(String cause) {
		return new TranslatableComponent("spelllogmod.nostrummagica." + cause.toLowerCase());
	}
	
	public static final float CalculateDamage(@Nullable LivingEntity caster, LivingEntity target, float baseDamage, float efficiency, EMagicElement element, ISpellLogBuilder log) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		final @Nullable INostrumMagic magic = caster == null ? null : NostrumMagica.getMagicWrapper(caster);
		
		final Damage damage = new Damage(baseDamage, efficiency, log);
		LogDamage(0, baseDamage, damage, "Start");
		
		if (element == EMagicElement.NEUTRAL) {
			// Neutral is reduced by real armor but not affected by magic resist effects and attributes.
			// It still gains power from magic boost/magic damage AND the strength status effect/attack attribute AND is still reduces with magic reduction (below).
			final float baseAttribute = GetPhysicalAttributeBonusNoMainhand(caster);
			final float mainhand = GetPhysicalAttributeBonusMainhand(caster);
			damage.baseFlat("PhysicalAttributeBase", baseAttribute);
			
			// Note that the neutral master skill includes using some of this
			final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Neutral_Master)) {
				damage.baseFlat(NostrumSkills.Neutral_Master, (int) (mainhand * .2f));
			}
		} else {
		
			final int armor = target.getArmorValue();
			final boolean undead = target.isInvertedHealAndHarm();
			final boolean ender;
			final boolean light;
			final boolean flamy;
			
			if (target instanceof EnderMan || target instanceof Endermite
					|| target instanceof DragonEntity) {
				// Ender status and immunity can be turned off with the disrupt status effect
				MobEffectInstance effect = target.getEffect(NostrumEffects.disruption);
				if (effect != null && effect.getDuration() > 0) {
					ender = false;
				} else {
					ender = true;
				}
			} else {
				ender = false;
			}
			
			if (target.getBbHeight() < 1.5f || target instanceof EnderMan || target instanceof ShadowRedDragonEntity) {
				light = true;
			} else if (magic != null && magic.hasSkill(NostrumSkills.Wind_Inflict) && target.getEffect(MobEffects.POISON) != null) {
				// Poisoned enemies count as light with wind inflict skill
				light = true;
			} else {
				light = false;
			}
			
			if (target.fireImmune()) {
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
				MobEffectInstance strength = caster == null ? null : caster.getEffect(MobEffects.DAMAGE_BOOST);
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
				if (target.isOnFire()) {
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
		AttributeInstance attr = caster == null ? null : caster.getAttribute(NostrumAttributes.magicDamage);
		if (attr != null && attr.getValue() != 0.0D) {
			damage.bonusScale("MagicDamageAttribute", (Math.max(0, Math.min(100, 1f + (float)(attr.getValue() / 100.0)))) - 1f);
		}
		
		// TODO: make into attribute?
		final @Nullable MobEffectInstance magicWeakness = caster == null ? null : caster.getEffect(NostrumEffects.magicWeakness);
		if (magicWeakness != null && magicWeakness.getDuration() > 0) {
			damage.finalFlat(NostrumSkills.Neutral_Inflict, -2 * (magicWeakness.getAmplifier() + 1));
		}
		
		// No magic resist for neutral; it uses armor value
		if (element != EMagicElement.NEUTRAL) {
			attr = target.getAttribute(NostrumAttributes.magicResist);
			if (attr != null && attr.getValue() != 0.0D) {
				damage.resistScale("MagicResistAttribute", (float) (Math.max(0.0D, Math.min(2.0D, 1.0D - (attr.getValue() / 100.0D)))) - 1f);
			}
		} else {
			final float mod = GetArmorModifier(target, damage) - 1f;
			if (mod != 0f) {
				damage.resistScale("PhysicalArmor", mod);
			}
		}
		
		// Apply armor reductions
		final double reducAttrib = AttributeUtil.GetAttributeValueSafe(target, NostrumAttributes.GetReduceAttribute(element))
				+ AttributeUtil.GetAttributeValueSafe(target, NostrumAttributes.reduceAll);
		if (attr != null && reducAttrib != 0.0D) {
			damage.finalFlat("FlatReducAttribute_" + element.name().toLowerCase(), (float) -reducAttrib);
		}
		
		return damage.calc();
	}
	
	public static final float DamageEntity(LivingEntity target, EMagicElement element, float base, float efficiency, @Nullable LivingEntity source, ISpellLogBuilder log) {
		final float damage = CalculateDamage(source, target, base, efficiency, element, log);
		target.hurt(new MagicDamageSource(source, element), damage);
		return damage;
	}
	
	public static final float DamageEntity(LivingEntity target, EMagicElement element, float base, @Nullable LivingEntity source) {
		return DamageEntity(target, element, base, 1f, source, ISpellLogBuilder.Dummy);
	}
	
}
