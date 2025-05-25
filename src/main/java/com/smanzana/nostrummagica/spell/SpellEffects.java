package com.smanzana.nostrummagica.spell;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.component.SpellAction;

import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

public final class SpellEffects {
	
	private SpellEffects() {}

	public static final SpellAction solveAction(EAlteration alteration,	EMagicElement element, int elementCount) {
		
		// Could do a registry with hooks here, if wanted it to be extensible
		
		if (alteration == null) {
			// Damage spell
			return new SpellAction().damage(element, (float) (elementCount + 1))
					.name("damage." + element.name().toLowerCase());
		}
		
		switch (alteration) {
		case RUIN:
			return solveRuin(element, elementCount);
		case CONJURE:
			return solveConjure(element, elementCount);
		case ENCHANT:
			return solveEnchant(element, elementCount);
		case GROWTH:
			return solveGrowth(element, elementCount);
		case INFLICT:
			return solveInflict(element, elementCount);
		case RESIST:
			return solveResist(element, elementCount);
		case SUMMON:
			return solveSummon(element, elementCount);
		case SUPPORT:
			return solveSupport(element, elementCount);
		case CORRUPT:
			return solveCorrupt(element, elementCount);
		}
		
		return null;
	}
	
	private static final SpellAction solveRuin(EMagicElement element, int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction().transmute(elementCount).name("transmute");
		case EARTH:
		case ENDER:
		case FIRE:
		case ICE:
		case LIGHTNING:
		case WIND:
			return new SpellAction().damage(element, 2f + (float) (2 * elementCount))
					.name("ruin." + element.name().toLowerCase());
		}
		
		return null;
	}
	
	private static final SpellAction solveInflict(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(MobEffects.WEAKNESS, duration, amp)
					.status(NostrumEffects.magicWeakness, duration, amp, (caster, target, eff) -> {
						// Only apply with physical inflict skill
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Physical_Inflict)) {
							return true;
						}
						return false;
					}).name("weakness");
		case EARTH:
			return new SpellAction().status(NostrumEffects.rooted, duration, (caster, target, eff) -> {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Earth_Inflict)) {
					return amp + 3;
				} else {
					return amp;
				}
			}).name("rooted");
		case ENDER:
			return new SpellAction().status(MobEffects.BLINDNESS, duration, amp).status(NostrumEffects.mobBlindness, duration, amp, (caster, target, eff) -> {
				// With the inflict skill (or if non-player), apply to mobs
				if (target instanceof Mob) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (!(caster instanceof Player) || (attr != null && attr.hasSkill(NostrumSkills.Ender_Inflict))) {
						return true;
					}
				}
				return false;
			}).resetTarget((caster, target, eff) -> {
				// With the inflict skill, reset target to none
				if (target instanceof Mob) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (!(caster instanceof Player) || (attr != null && attr.hasSkill(NostrumSkills.Ender_Inflict))) {
						return true;
					}
				}
				return false;
			}).name("blindness");
		case FIRE:
			// Note: damage is AFTER burn so that if burn takes away shields the damage comes through after
			return new SpellAction().burn(elementCount * 20 * 5).damage(EMagicElement.FIRE, 1f + ((float) amp/2f)).name("burn");
		case ICE:
			return new SpellAction().status(NostrumEffects.frostbite, duration, amp, (caster, target, eff) -> {
				if (caster == target) {
					return true;
				}
				
				// With the inflict skill, don't apply frostbite to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Inflict)) {
					return !NostrumMagica.IsSameTeam(caster, target);
				}
				return true;
			}).status(NostrumEffects.manaRegen, duration, amp, (caster, target, eff) -> {
				if (caster == target) {
					return false;
				}
				
				// With the inflict skill, apply mana regen to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Inflict)) {
					return NostrumMagica.IsSameTeam(caster, target);
				}
				return false;
			}).name("frostbite");
		case LIGHTNING:
			return new SpellAction().status(MobEffects.MOVEMENT_SLOWDOWN, (int) (duration * .7), amp)
					.status(NostrumEffects.immobilize, 30 + (10 * elementCount) , amp, (caster, target, eff) -> {
						// Only apply with lightning inflict skill
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Lightning_Inflict)) {
							return true;
						}
						return false;
					})
					.name("slowness");
		case WIND:
			return new SpellAction().status(MobEffects.POISON, duration, amp).name("poison");
		}
		
		return null;
	}
	
	private static final SpellAction solveResist(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(MobEffects.DAMAGE_RESISTANCE, duration, amp).name("resistance");
		case EARTH:
			return new SpellAction().status(MobEffects.DAMAGE_BOOST, duration, amp).name("strength");
		case ENDER:
			return new SpellAction().status(MobEffects.INVISIBILITY, duration, amp).name("invisibility");
		case FIRE:
			return new SpellAction().status(MobEffects.FIRE_RESISTANCE, duration, amp).name("fireresist");
		case ICE:
			return new SpellAction().dispel(elementCount * (int) (Math.pow(3, elementCount - 1))).name("dispel");
		case LIGHTNING:
			return new SpellAction().status(NostrumEffects.magicResist, duration, amp).name("magicresist");
		case WIND:
			return new SpellAction().push(5f + (2 * amp), elementCount).name("push");
		}
		
		return null;
	}
	
	private static final SpellAction solveSupport(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(MobEffects.ABSORPTION, duration * 5, amp).name("lifeboost");
		case EARTH:
			return new SpellAction().status(NostrumEffects.physicalShield, duration, (caster, target, eff) -> {
				// With the support skill, give 2 extra levels of shield
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Earth_Support)) {
					return amp + 2;
				}
				return amp;
			}).name("shield.physical");
		case ENDER:
			return new SpellAction().blink(15.0f * elementCount).name("blink");
		case FIRE:
			return new SpellAction().status(NostrumEffects.magicBoost, duration, amp).name("magicboost");
		case ICE:
			return new SpellAction().status(NostrumEffects.magicShield, duration, amp)
			.status(NostrumEffects.manaRegen, duration, 0, (caster, target, eff) -> {
				// With the support skill, also give mana regen
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Support)) {
					return true;
				}
				return false;
			}).name("shield.magic");
		case LIGHTNING:
			return new SpellAction().pull(5 * elementCount, elementCount).name("pull");
		case WIND:
			return new SpellAction().status(MobEffects.MOVEMENT_SPEED, duration, amp)
					.status(MobEffects.DIG_SPEED, duration, amp, (caster, target, eff) -> {
						// With the support skill, also give haste
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Wind_Support)) {
							return true;
						}
						return false;
					}).name("speed");
		}
		
		return null;
	}
	
	private static final SpellAction solveGrowth(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().healFood(4 * elementCount).status(NostrumEffects.naturesBlessing, duration * 5, amp, (caster, target, eff) -> {
				// Only apply with physical growth skill
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Physical_Growth)) {
					return true;
				}
				return false;
			}).name("food");
		case EARTH:
			return new SpellAction().status(MobEffects.REGENERATION, duration, amp).name("regen");
		case ENDER:
			return new SpellAction().swap().swapStatus((caster, target, eff) -> {
				// Only apply with ender growth skill
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ender_Growth)) {
					return true;
				}
				return false;
			}).name("swap");
		case FIRE:
			return new SpellAction().dropEquipment(elementCount, (caster, target, eff) -> {
				// Only apply with fire growth skill AND if a mob
				if (target instanceof Mob) {
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (attr != null && attr.hasSkill(NostrumSkills.Fire_Growth)) {
						return true;
					}
				}
				return false;
			}).burnArmor(elementCount).name("burnarmor"); // burn armor after dropping to not damage things before dropping them
		case ICE:
			return new SpellAction().heal(4f * elementCount).name("heal");
		case LIGHTNING:
			return new SpellAction().status(MobEffects.JUMP, duration, amp)
					.status(NostrumEffects.bonusJump, duration, 0, (caster, target, eff) -> {
						// With the growth skill, also give jump boost
						INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
						if (attr != null && attr.hasSkill(NostrumSkills.Lightning_Growth)) {
							return true;
						}
						return false;
					}).name("jumpboost");
		case WIND:
			return new SpellAction().propel(elementCount).name("propel");
		}
		
		return null;
	}
	
	private static final SpellAction solveEnchant(EMagicElement element, int elementCount) {
		return new SpellAction().enchant(element, elementCount).name("enchant." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveConjure(EMagicElement element, int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction().blockBreak(elementCount).name("break");
		case EARTH:
			return new SpellAction().grow(elementCount).name("grow");
		case ENDER:
			return new SpellAction().phase(elementCount).name("phase");
		case FIRE:
			return new SpellAction().cursedFire(elementCount).name("cursed_fire");
		case ICE:
			return new SpellAction().mysticWater(elementCount-1, elementCount * 2, 20 * 60).name("mystic_water");
		case LIGHTNING:
			return new SpellAction().lightning().name("lightningbolt");
		case WIND:
			return new SpellAction().wall(elementCount).name("mystic_air");
		}
		
		return null;
	}
	
	private static final SpellAction solveSummon(EMagicElement element, int elementCount) {
		return new SpellAction().summon(element, elementCount).name("summon." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveCorrupt(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(NostrumEffects.rend, duration, amp).name("rend");
		case EARTH:
			return new SpellAction().harvest(elementCount).name("harvest");
		case ENDER:
			return new SpellAction().status(NostrumEffects.disruption, duration, amp).name("disruption");
		case FIRE:
			return new SpellAction().status(NostrumEffects.sublimation, duration, amp).name("sublimation");
		case ICE:
			return new SpellAction().status(NostrumEffects.healResist, duration, (caster, target, eff) -> {
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Ice_Corrupt)) {
					return amp + 3;
				} else {
					return amp;
				}
			}).name("healresist");
		case LIGHTNING:
			return new SpellAction().status(NostrumEffects.magicRend, duration, amp).name("magicrend");
		case WIND:
			return new SpellAction().status(NostrumEffects.fastFall, duration, amp, (caster, target, eff) -> {
				// With the corrupt skill, don't apply fastfall to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Wind_Corrupt)) {
					return !NostrumMagica.IsSameTeam(caster, target);
				}
				return true;
			}).status(MobEffects.SLOW_FALLING, duration, amp, (caster, target, eff) -> {
				// With the corrupt skill, apply slowfall to friendlies
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Wind_Corrupt)) {
					return NostrumMagica.IsSameTeam(caster, target);
				}
				return false;
			}).name("fastfall");
		}
		
		return null;
	}
	
}
