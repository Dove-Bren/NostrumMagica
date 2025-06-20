package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.BiConsumer;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.ISpellTargetBlock;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.effect.ElementalSpellBoostEffect;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.entity.ISpellHandlingEntity;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellAction.SpellActionResult;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.log.ESpellLogModifierType;
import com.smanzana.nostrummagica.spell.log.ISpellLogBuilder;
import com.smanzana.nostrummagica.util.NonNullEnumMap;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public final class SpellEffects {
	
	private SpellEffects() {}

	public static final SpellAction SolveAction(EAlteration alteration,	EMagicElement element, int elementCount) {
		
		// Could do a registry with hooks here, if wanted it to be extensible
		
		if (alteration == null) {
			return solveBase(element, elementCount);
		}
		
		switch (alteration) {
		case RUIN:
			return solveRuin(element, elementCount);
		case HARM:
			return solveHarm(element, elementCount);
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
		case EXTRACT:
			return solveExtract(element, elementCount);
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
			return new SpellAction().status(NostrumEffects.physicalShield, duration, (caster, target, eff) -> {
				// With the support skill, give 2 extra levels of shield
				INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
				if (attr != null && attr.hasSkill(NostrumSkills.Earth_Resist)) {
					return amp + 2;
				}
				return amp;
			}).name("shield.physical");
		case ENDER:
			return new SpellAction().status(MobEffects.INVISIBILITY, duration, amp).name("invisibility");
		case FIRE:
			return new SpellAction().status(MobEffects.FIRE_RESISTANCE, duration, amp).name("fireresist");
		case ICE:
			return new SpellAction().status(NostrumEffects.magicShield, duration, amp)
				.status(NostrumEffects.manaRegen, duration, 0, (caster, target, eff) -> {
					// With the support skill, also give mana regen
					INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
					if (attr != null && attr.hasSkill(NostrumSkills.Ice_Resist)) {
						return true;
					}
					return false;
				}).name("shield.magic");
		case LIGHTNING:
			return new SpellAction().status(NostrumEffects.magicResist, duration, amp).name("magicresist");
		case WIND:
			return new SpellAction().whirlwind((5 + (elementCount * 5)) * 20, 1f + (elementCount * .5f)).name("whirlwind");
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
			return new SpellAction().status(MobEffects.DAMAGE_BOOST, duration, amp).name("strength");
		case ENDER:
			return new SpellAction().blink(15.0f * elementCount).name("blink");
		case FIRE:
			return new SpellAction().status(NostrumEffects.magicBoost, duration, amp).name("magicboost");
		case ICE:
			return new SpellAction().status(NostrumEffects.swiftSwim, duration, amp).name("swift_swim");
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
			return new SpellAction().healMana(elementCount * 10).name("restore_mana");
		case WIND:
			return new SpellAction().propel(elementCount).name("propel");
		}
		
		return null;
	}
	
	private static final SpellAction solveEnchant(EMagicElement element, int elementCount) {
		return new SpellAction().enchant(element, elementCount).name("enchant." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveHarm(EMagicElement element, int elementCount) {
		// Damage spell
		return new SpellAction().damage(element, (float) (elementCount + 1))
				.name("damage." + element.name().toLowerCase());
	}
	
	private static final SpellAction solveBase(EMagicElement element, int elementCount) {
		switch (element) {
		case PHYSICAL:
			return new SpellAction().blockBreak(elementCount).knockback(elementCount).name("bash");
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
	
	private static final SpellAction solveExtract(EMagicElement element, int elementCount) {
		int duration = 20 * 15 * elementCount;
		int amp = elementCount - 1;
		switch (element) {
		case PHYSICAL:
			return new SpellAction().status(NostrumEffects.carapace, duration, amp).name("carapace");
		case EARTH:
			return new SpellAction().status(MobEffects.GLOWING, duration, amp).lightBlock().name("light");
		case ENDER:
			return new SpellAction().status(MobEffects.NIGHT_VISION, duration, amp).name("night_vision");
		case FIRE:
			return new SpellAction().stealHealth(element, 1 + elementCount).name("health_steal");
		case ICE:
			return new SpellAction().stealMana(element, 5 + (5 * elementCount)).name("mana_steal");
		case LIGHTNING:
			return new SpellAction().pull(5 * elementCount, elementCount).name("pull");
		case WIND:
			return new SpellAction().dispel(elementCount * (int) (Math.pow(3, elementCount - 1))).name("dispel");
		}
		
		return null;
	}
	
	public static record ApplyResult(boolean anySuccess,
			float damageTotal,
			float healTotal,
			Map<LivingEntity, Map<EMagicElement, Float>> totalAffectedEntities,
			Set<SpellLocation> totalAffectedLocations,
			Map<LivingEntity, EMagicElement> entityLastElement
			) {}
	
	public static final ApplyResult ApplySpellEffects(LivingEntity caster, List<SpellEffectPart> parts, float castEfficiency,
			List<LivingEntity> targets, List<SpellLocation> locations,
			ISpellLogBuilder log, BiConsumer<LivingEntity, SpellActionResult> onEnt, BiConsumer<SpellLocation, SpellActionResult> onBlock) {
		boolean first = true;
		boolean anySuccess = false;
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		float damageTotal = 0f;
		float healTotal = 0f;
		final Map<LivingEntity, Map<EMagicElement, Float>> totalAffectedEntities = new HashMap<>();
		final Set<SpellLocation> totalAffectedLocations = new HashSet<>();
		final Map<LivingEntity, EMagicElement> entityLastElement = new HashMap<>();
		
		for (SpellEffectPart part : parts) {
			SpellAction action = SpellEffects.SolveAction(part.getAlteration(), part.getElement(), part.getElementCount());
			float efficiency = castEfficiency + (part.getPotency() - 1f);
			
			log.pushModifierStack();
			
			// Apply part-specific bonuses that don't matter on targets here
			final float partBonus = getCasterEfficiencyBonus(caster, part, action, efficiency, log);
			efficiency += partBonus;
			
			if (attr != null && attr.isUnlocked()) {
				attr.setKnowledge(part.getElement(), part.getAlteration());
			}
			
			// Track what entities/positions actually have an affect applied to them
			final List<LivingEntity> affectedEnts = new ArrayList<>();
			final List<SpellLocation> affectedPos = new ArrayList<>();
			
			if (targets != null && !targets.isEmpty()) {
				for (LivingEntity targ : targets) {
					if (targ == null) {
						continue;
					}
					
					if (targ instanceof ISpellHandlingEntity spellHandler) {
						if (spellHandler.processSpellEffect(caster, part, action)) {
							log.effect(targ);
							anySuccess = true;
							totalAffectedEntities.computeIfAbsent(targ, e -> new NonNullEnumMap<>(EMagicElement.class, 0f));
							log.endEffect();
						}
					}
					
					log.effect(targ);
					log.pushModifierStack();
					
					// Apply per-target bonuses
					final float targBonus = getTargetEfficiencyBonus(caster, targ, part, action, efficiency, log);
					float perEfficiency = efficiency + targBonus;
					
					SpellActionResult result = action.apply(caster, targ, perEfficiency, log); 
					if (result.applied) {
						affectedEnts.add(targ);
						totalAffectedEntities.computeIfAbsent(targ, e -> new NonNullEnumMap<>(EMagicElement.class, 0f)).merge(part.getElement(), result.damage - result.heals, Float::sum);
						entityLastElement.put(targ, part.getElement());
						damageTotal += result.damage;
						healTotal += result.heals;
						anySuccess = true;
						if (onEnt != null) {
							onEnt.accept(targ, result);
						}
					}
					
					log.popModifierStack();
					
					log.endEffect();
				}
			} else if (locations != null && !locations.isEmpty()) {
				// use locations
				for (SpellLocation pos : locations) {
					// Possibly interact with spell aware blocks
					BlockState state = pos.world.getBlockState(pos.selectedBlockPos);
					if (state.getBlock() instanceof ISpellTargetBlock target) {
						if (target.processSpellEffect(pos.world, state, pos.selectedBlockPos, caster, pos, part, action)) {
							// Block consumed this effect
							anySuccess = true;
							log.effect(pos);
							totalAffectedLocations.add(pos);
							log.endEffect();
							continue;
						}
					}
					
					log.effect(pos);
					SpellActionResult result = action.apply(caster, pos, efficiency, log); 
					if (result.applied) {
						if (result.affectedPos != null) {
							affectedPos.add(result.affectedPos);
						}
						anySuccess = true;
						totalAffectedLocations.add(result.affectedPos);
						if (onBlock != null) {
							onBlock.accept(pos, result);
						}
					}
					log.endEffect();
				}
			} else {
				; // Drop it on the floor
			}
			
			// Evaluate showing vfx for each part
			if (first) {
				if (!affectedEnts.isEmpty())
				for (LivingEntity affected : affectedEnts) {
					NostrumMagica.Proxy.spawnSpellEffectVfx(affected.level, part,
							caster, null, affected, null);
				}
				
				if (!affectedPos.isEmpty())
				for (SpellLocation affectPos : affectedPos) {
					NostrumMagica.Proxy.spawnSpellEffectVfx(affectPos.world, part,
							caster, null, null, new Vec3(affectPos.selectedBlockPos.getX() + .5, affectPos.selectedBlockPos.getY(), affectPos.selectedBlockPos.getZ() + .5)
							);
				}
			}
			
			first = false;
			
			log.popModifierStack();
		}
		
		if (anySuccess) {
			if (attr != null && attr.hasSkill(NostrumSkills.Spellcasting_ElemLinger)) {
				for (Entry<LivingEntity, EMagicElement> entry : entityLastElement.entrySet()) {
					final MobEffect effect = ElementalSpellBoostEffect.GetForElement(entry.getValue());
					entry.getKey().addEffect(new MobEffectInstance(effect, 20 * 5, 0));
				}
			}
		} else {
			// Do an effect so it's clearer to caster that there was no effect at any tried location/entity.
			// Mirror "ents, then if not positions" from above.
			if (targets != null && !targets.isEmpty()) {
				for (LivingEntity targ : targets) {
					doFailEffect(targ.level, targ.position().add(0, .2 + targ.getBbHeight(), 0));
				}
			} else if (locations != null && !locations.isEmpty()) {
				for (SpellLocation pos : locations) {
					doFailEffect(pos.world, pos.hitPosition);
				}
			}
		}
		
		return new ApplyResult(anySuccess, damageTotal, healTotal, totalAffectedEntities, totalAffectedLocations, entityLastElement);
	}
	
	protected static final float getTargetEfficiencyBonus(LivingEntity caster, LivingEntity target, SpellEffectPart effect, SpellAction action, float base, ISpellLogBuilder log) {
		float bonus = 0f;
		
		if (effect.getElement() != EMagicElement.PHYSICAL) {
			final MobEffect boostEffect = ElementalSpellBoostEffect.GetForElement(effect.getElement().getOpposite());
			if (target.getEffect(boostEffect) != null) {
				final float amt = .25f * (1 + target.getEffect(boostEffect).getAmplifier());
				bonus += amt;
				target.removeEffect(boostEffect);
				log.addGlobalModifier(NostrumSkills.Spellcasting_ElemLinger, amt, ESpellLogModifierType.BONUS_SCALE);
			}
		}
		
		return bonus;
	}
	
	protected static final float getCasterEfficiencyBonus(LivingEntity caster, SpellEffectPart effect, SpellAction action, float base, ISpellLogBuilder log) {
		float bonus = 0f;
		INostrumMagic attr = NostrumMagica.getMagicWrapper(caster);
		
		if (attr != null)
		switch (effect.getElement()) {
		case EARTH:
			if (attr.hasSkill(NostrumSkills.Earth_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Earth_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		case ENDER:
			if (attr.hasSkill(NostrumSkills.Ender_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Ender_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		case FIRE:
			if (attr.hasSkill(NostrumSkills.Fire_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Fire_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		case ICE:
			if (attr.hasSkill(NostrumSkills.Ice_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Ice_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		case LIGHTNING:
			if (attr.hasSkill(NostrumSkills.Lightning_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Lightning_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		case PHYSICAL:
			if (attr.hasSkill(NostrumSkills.Physical_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Physical_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		case WIND:
			if (attr.hasSkill(NostrumSkills.Wind_Novice)) {
				bonus += .2f;
				log.addGlobalModifier(NostrumSkills.Wind_Novice, .2f, ESpellLogModifierType.BONUS_SCALE);
			}
			break;
		}
		
		return bonus;
	}
	
	protected static final void doFailEffect(Level world, Vec3 pos) {
		NostrumMagicaSounds.CAST_FAIL.play(world, pos.x(), pos.y(), pos.z());
		((ServerLevel) world).sendParticles(ParticleTypes.SMOKE, pos.x(), pos.y(), pos.z(), 10, 0, 0, 0, .05);
	}
}
