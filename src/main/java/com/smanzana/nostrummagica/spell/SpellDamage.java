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
		private float base;
		private final ISpellLogBuilder log;
		
		public Damage(float base, ISpellLogBuilder log) {
			this.base = base;
			this.log = log;
		}
		
		private final void mulInternal(String cause, float scale) {
			final float pre = base;
			base *= scale;
			base = Math.max(0, base);
			final float post = base;
			LogDamage(pre, post, cause);
		}
		
		public final void mul(String cause, float scale) {
			mulInternal(cause, scale);
			log.effectMod(MakeLabel(cause), scale-1f, false);
		}
		
		public final void mul(Skill skill, float scale) {
			mulInternal("Skill: " + skill.getName().getString(), scale);
			log.effectMod(skill, scale-1f, false);
		}
		
		private final void addInternal(String cause, float diff) {
			final float pre = base;
			base += diff;
			base = Math.max(0, base);
			final float post = base;
			LogDamage(pre, post, cause);
		}
		
		public final void add(String cause, float diff) {
			addInternal(cause, diff);
			log.effectMod(MakeLabel(cause), diff, true);
		}
		
		public final void add(Skill skill, float diff) {
			addInternal("Skill: " + skill.getName().getString(), diff);
			log.effectMod(skill, diff, true);
		}
		
		public final float calc() {
			LogDamage(base, base, "Result");
			return this.base;
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
	
	public static final float CalculateDamage(@Nullable LivingEntity caster, LivingEntity target, float baseDamage, EMagicElement element, ISpellLogBuilder log) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		final @Nullable INostrumMagic magic = caster == null ? null : NostrumMagica.getMagicWrapper(caster);
		
		LogDamage(0, baseDamage, "Start");
		final Damage damage = new Damage(baseDamage, log);
		
		if (element == EMagicElement.PHYSICAL) {
			// Physical is reduced by real armor but not affected by magic resist effects and attributes.
			// It still gains power from magic boost/magic damage AND the strength status effect/attack attribute AND is still reduces with magic reduction (below).
			final float baseAttribute = GetPhysicalAttributeBonusNoMainhand(caster);
			final float mainhand = GetPhysicalAttributeBonusMainhand(caster);
			damage.add("PhysicalAttributeBase", baseAttribute);
			
			// Note that the physical master skill includes using some of this
			final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
			if (attr != null && attr.hasSkill(NostrumSkills.Physical_Master)) {
				damage.add(NostrumSkills.Physical_Master, (int) (mainhand * .2f));
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
				damage.add(NostrumSkills.Fire_Adept, 2);
			}
			
			if (element == EMagicElement.LIGHTNING && magic != null && magic.hasSkill(NostrumSkills.Lightning_Adept)) {
				damage.add(NostrumSkills.Lightning_Adept, 1);
			}
			
			if (element == EMagicElement.EARTH && magic != null && magic.hasSkill(NostrumSkills.Earth_Adept)) {
				EffectInstance strength = caster == null ? null : caster.getActivePotionEffect(Effects.STRENGTH);
				if (strength != null) {
					// Matches strength attribute boost
					damage.add(NostrumSkills.Earth_Adept, 3 * (strength.getAmplifier() + 1));
				}
			}
				
			switch (element) {
			case ENDER:
				if (ender) {
					damage.mul("EnderXEnder", 0f);
					return 0.0f; // does not affect ender
				}
				damage.mul("EnderDamage", 1.2f); // return raw damage (+20%) not affected by armor
				break;
			case LIGHTNING:
				damage.mul("LightningDamage", .75f + ((float) armor / 20f)); // double in power for every 20 armor
				break;
			case FIRE:
				// 1.5x damage against undead. Half on flame-resistant. Regular otherwise
				if (undead) {
					damage.mul("FireXUndead", 1.5f); 
				} else if (flamy) {
					damage.mul("FireXFire", .5f); // 1.5x damage against undead. Regular otherwise
				}
				break;
			case EARTH:
				// No change for earth damage
				//damage.mul("EarthDamage", 1f); not useful for logging 
				break;
			case ICE:
				// More affective against everything except undead
				if (undead) {
					damage.mul("IceXUndead", .6f);
				} else {
					damage.mul("IceDamage", 1.3f);
				}
				if (target.isBurning()) {
					damage.mul("IceDamageOnBurning", 2);
				}
				break;
			case WIND:
				// 180% against light (endermen included) enemies
				if (light) {
					damage.mul("WindLightDamage", 1.8f);
				} else {
					damage.mul("WindHeavyDamage", .8f);
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
			damage.mul("MagicDamageAttribute", Math.max(0, Math.min(100, 1f + (float)(attr.getValue() / 100.0))));
		}
		
		// No magic resist for physical; it uses armor value
		if (element != EMagicElement.PHYSICAL) {
			attr = target.getAttribute(NostrumAttributes.magicResist);
			if (attr != null && attr.getValue() != 0.0D) {
				damage.mul("MagicResistAttribute", (float) Math.max(0.0D, Math.min(2.0D, 1.0D - (attr.getValue() / 100.0D))));
			}
		} else {
			damage.mul("PhysicalArmor", GetArmorModifier(target, damage));
		}
		
		// Apply armor reductions
		attr = target.getAttribute(NostrumAttributes.GetReduceAttribute(element));
		if (attr != null && attr.getValue() != 0.0D) {
			damage.add("FlatReducAttribute", (float) -attr.getValue());
		}
		
		return damage.calc();
	}
	
	public static final float DamageEntity(LivingEntity target, EMagicElement element, float base, @Nullable LivingEntity source, ISpellLogBuilder log) {
		final float damage = CalculateDamage(source, target, base, element, log);
		target.attackEntityFrom(new MagicDamageSource(source, element), damage);
		return damage;
	}
	
	public static final float DamageEntity(LivingEntity target, EMagicElement element, float base, @Nullable LivingEntity source) {
		return DamageEntity(target, element, base, source, ISpellLogBuilder.Dummy);
	}
	
}
