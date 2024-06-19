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

public class SpellDamage {
	
	private static final class Damage {
		public float base;
		
		public Damage(float base) {
			this.base = base;
		}
	}

	protected static final float GetPhysicalAttributeBonus(LivingEntity caster) {
		// Get raw amount
		if (!caster.getAttributeManager().hasAttributeInstance(Attributes.ATTACK_DAMAGE)) {
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
				
				// Note that the physical master skill includes using some of this
				final @Nullable INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Physical_Master)) {
					amt -= (int) ((float) extra * .8f);
				} else {
					amt -= extra;
				}
			}
		}
		
		return (float) amt;
	}
	
	protected static final float GetArmorModifier(LivingEntity target, final Damage damage) {
		int i = 25 - target.getTotalArmorValue();
		return (float)i / 25f;
	}
	
	private static boolean DamageLog = true;
	private static final void LogDamage(float before, float after, String msg) {
		if (DamageLog) {
			NostrumMagica.logger.info(String.format("%s [%.2f -> %.2f][%+.2f]", msg, before, after, after-before));
		}
	}
	
	private static final void MulDamage(Damage damage, String cause, float scale) {
		final float pre = damage.base;
		damage.base *= scale;
		damage.base = Math.max(0, damage.base);
		final float post = damage.base;
		LogDamage(pre, post, cause);
	}
	
	private static final void AddDamage(Damage damage, String cause, float diff) {
		final float pre = damage.base;
		damage.base += diff;
		damage.base = Math.max(0, damage.base);
		final float post = damage.base;
		LogDamage(pre, post, cause);
	}
	
	public static final float CalculateDamage(@Nullable LivingEntity caster, LivingEntity target, float baseDamage, EMagicElement element) {
		float amt = 0f;
		
		if (target == null)
			return amt;
		
		final INostrumMagic magic = NostrumMagica.getMagicWrapper(caster);
		
		LogDamage(0, baseDamage, "Start");
		final Damage damage = new Damage(baseDamage);
		
		if (element == EMagicElement.PHYSICAL) {
			// Physical is reduced by real armor but not affected by magic resist effects and attributes.
			// It still gains power from magic boost/magic damage AND the strength status effect/attack attribute AND is still reduces with magic reduction (below).
			AddDamage(damage, "PhysicalAttribute", GetPhysicalAttributeBonus(caster));
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
				AddDamage(damage, "FireAdept", 2);
			}
			
			if (element == EMagicElement.LIGHTNING && magic != null && magic.hasSkill(NostrumSkills.Lightning_Adept)) {
				AddDamage(damage, "LightningAdept", 1);
			}
			
			if (element == EMagicElement.EARTH && magic != null && magic.hasSkill(NostrumSkills.Earth_Adept)) {
				EffectInstance strength = caster.getActivePotionEffect(Effects.STRENGTH);
				if (strength != null) {
					// Matches strength attribute boost
					AddDamage(damage, "EarthAdeptStrength", 3 * (strength.getAmplifier() + 1));
				}
			}
				
			switch (element) {
			case ENDER:
				if (ender) {
					MulDamage(damage, "EnderXEnder", 0f);
					return 0.0f; // does not affect ender
				}
				MulDamage(damage, "EnderDamage", 1.2f); // return raw damage (+20%) not affected by armor
				break;
			case LIGHTNING:
				MulDamage(damage, "LightningDamage", .75f + ((float) armor / 20f)); // double in power for every 20 armor
				break;
			case FIRE:
				MulDamage(damage, "FireDamage", undead ? 1.5f : (flamy ? .5f : 1f)); // 1.5x damage against undead. Regular otherwise
				break;
			case EARTH:
				MulDamage(damage, "EarthDamage", 1f); // No change for earth damage
				break;
			case ICE:
				MulDamage(damage, "IceDamage", undead ? .6f : 1.3f); // More affective against everything except undead
				if (target.isBurning()) {
					MulDamage(damage, "IceDamageOnBurning", 2);
				}
				break;
			case WIND:
				MulDamage(damage, "WindDamage", light ? 1.8f : .8f); // 180% against light (endermen included) enemies
				break;
			default:
				//base;
				break;
			}
		}
		
		// Apply boosts and resist
		ModifiableAttributeInstance attr = caster.getAttribute(NostrumAttributes.magicDamage);
		if (attr != null && attr.getValue() != 0.0D) {
			MulDamage(damage, "MagicDamageAttribute", Math.max(0, Math.min(100, 1f + (float)(attr.getValue() / 100.0))));
		}
		
		// No magic resist for physical; it uses armor value
		if (element != EMagicElement.PHYSICAL) {
			attr = target.getAttribute(NostrumAttributes.magicResist);
			if (attr != null && attr.getValue() != 0.0D) {
				MulDamage(damage, "MagicResistAttribute", (float) Math.max(0.0D, Math.min(2.0D, 1.0D - (attr.getValue() / 100.0D))));
			}
		} else {
			MulDamage(damage, "PhysicalArmor", GetArmorModifier(target, damage));
		}
		
		// Apply armor reductions
		attr = target.getAttribute(NostrumAttributes.GetReduceAttribute(element));
		if (attr != null && attr.getValue() != 0.0D) {
			AddDamage(damage, "FlatReducAttribute", (float) -attr.getValue());
		}
		
		LogDamage(damage.base, damage.base, "Result");
		return damage.base;
	}
	
}
