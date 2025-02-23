package com.smanzana.nostrummagica.spell;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attribute.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.item.ISpellCastingTool;
import com.smanzana.nostrummagica.item.ISpellEquipment;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.stat.PlayerStat;
import com.smanzana.nostrummagica.stat.PlayerStatTracker;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.MinecraftForge;

public class SpellCasting {
	
	public static final class SpellCastResult {
		public final boolean succeeded;
		public final Spell spell;
		public final LivingEntity caster;
		public final SpellCastSummary summary;
		
		protected SpellCastResult(boolean succeeded, Spell spell, LivingEntity caster, SpellCastSummary summary) {
			this.succeeded = succeeded;
			this.spell = spell;
			this.caster = caster;
			this.summary = summary;
		}
		
		protected static final SpellCastResult fail(Spell spell, LivingEntity caster) {
			return fail(spell, caster, new SpellCastSummary(spell.getManaCost(), spell.getXP(true)));
		}
		
		protected static final SpellCastResult fail(Spell spell, LivingEntity caster, SpellCastSummary summary) {
			return new SpellCastResult(false, spell, caster, summary);
		}
	}
	
	public static final SpellCastResult AttemptScrollCast(Spell spell, LivingEntity entity) {
		return AttemptCast(spell, entity, ItemStack.EMPTY, true, false);
	}
	
	public static final SpellCastResult AttemptToolCast(Spell spell, LivingEntity entity, ItemStack tool) {
		final boolean freeCast = entity instanceof PlayerEntity
				? ((PlayerEntity) entity).isCreative()
				: false;
		return AttemptCast(spell, entity, tool, freeCast, false);
	}
	
	public static final SpellCastResult CheckToolCast(Spell spell, LivingEntity entity, ItemStack tool) {
		final boolean freeCast = entity instanceof PlayerEntity
				? ((PlayerEntity) entity).isCreative()
				: false;
		return AttemptCast(spell, entity, tool, freeCast, true);
	}
	
	private static final SpellCastResult EmitCastPostEvent(SpellCastResult result) {
		MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(result.spell, result.caster, result));
		return result;
	}

	protected static final SpellCastResult AttemptCast(Spell spell, LivingEntity entity, ItemStack tool, boolean freeCast, boolean checking) {
		INostrumMagic att = NostrumMagica.getMagicWrapper(entity);
		@Nullable PlayerEntity playerCast = (entity instanceof PlayerEntity) ? (PlayerEntity) entity : null;
		
		final SpellCastEvent.Pre event = new SpellCastEvent.Pre(spell, entity);
		if (MinecraftForge.EVENT_BUS.post(event)) {
			NostrumMagica.logger.debug("Spell cast cancelled");
		} else {
			spell = event.getSpell();
		}
		
		if (att == null) {
			NostrumMagica.logger.warn("Could not look up entity magic wrapper");
			return EmitCastPostEvent(SpellCastResult.fail(spell, entity));
		}
		
		// Check that the player can cast this (if it's not creative)
		if (!playerCast.isCreative()) {
			List<ITextComponent> problems = new ArrayList<>(4);
			if (!NostrumMagica.canCast(spell, att, problems)) {
				NostrumMagica.logger.warn("Got cast message from client with too low of stats. They should relog... " + entity);
				for (ITextComponent problem : problems) {
					entity.sendMessage(problem, Util.DUMMY_UUID);
				}
				return EmitCastPostEvent(SpellCastResult.fail(spell, entity));
			}
		}
		
		if (NostrumMagica.instance.getSpellCooldownTracker(entity.world).hasCooldown(playerCast, spell)) {
			NostrumMagica.logger.warn("Received spell cast while spell in cooldown: " + entity);
			return EmitCastPostEvent(SpellCastResult.fail(spell, entity));
		}
		
		// Cast it!
		boolean seen = att.wasSpellDone(spell);
		SpellCastSummary summary = new SpellCastSummary(spell.getManaCost(), spell.getXP(seen));
		
		// Add player's base magic potency
		summary.addEfficiency((float) entity.getAttribute(NostrumAttributes.magicPotency).getValue() / 100f);
		
		// Add cost reduction
		summary.addCostRate(att.getManaCostModifier());
		summary.addCostRate(-(float) entity.getAttribute(NostrumAttributes.manaCost).getValue() / 100f);
		
		// Add xp bonuses
		summary.addXPRate((float) entity.getAttribute(NostrumAttributes.xpBonus).getValue() / 100f);
		
		// Add tome enchancements
		if (!tool.isEmpty() && tool.getItem() instanceof ISpellCastingTool) {
			((ISpellCastingTool) tool.getItem()).onStartCastFromTool(entity, summary, tool);
		}
		
		if (freeCast) {
			// Negate reagent cost
			summary.addReagentCost(-summary.getReagentCost());
		} else {
			// Cap enhancements at 80% LRC
			float lrc = summary.getReagentCost();
			if (lrc < .2f)
				summary.addReagentCost(.2f - lrc); // Add however much we need to get to .2
		}
		
		// Visit an equipped spell armor
		ISpellEquipment.ApplyAll(entity, spell, summary);
		
		// Add skill bonuses
		boolean hasMasterElem = false;
		for (SpellEffectPart effect : spell.getSpellEffectParts()) {
			if (att.getElementalMastery(effect.getElement()).isGreaterOrEqual(EElementalMastery.MASTER)) {
				hasMasterElem = true;
				break;
			}
		}
		if (att.hasSkill(NostrumSkills.Spellcasting_ElemWeight) && hasMasterElem) {
			summary.addWeightDiscount(1);
		}
		if (att.hasSkill(NostrumSkills.Spellcasting_ElemMana) && hasMasterElem) {
			summary.addCostRate(-.1f);
		}
		if (att.hasSkill(NostrumSkills.Spellcasting_Weight1)) {
			summary.addWeightDiscount(1);
		}
		if (att.hasSkill(NostrumSkills.Spellcasting_Potency1)) {
			summary.addEfficiency(.1f);
		}
		
		int cost = Math.max(0, summary.getFinalCost());
		float xp = summary.getFinalXP();
		
		Map<ReagentType, Integer> reagents = null;
		Collection<ITameDragon> dragons = null; // more generally: mana HELPERS
		
		if (!freeCast) {
			// Take mana and reagents
			
			int mana = att.getMana();
			
			// Add dragon mana pool (for players)
			if (playerCast != null) {
				dragons = NostrumMagica.getNearbyTamedDragons(playerCast, 32, true);
				if (dragons != null && !dragons.isEmpty()) {
					for (ITameDragon dragon : dragons) {
						if (dragon.sharesMana(playerCast)) {
							mana += dragon.getMana();
						}
					}
				}
			}
			
			if (mana < cost) {
				return EmitCastPostEvent(SpellCastResult.fail(spell, entity, summary));
			}
			
			reagents = CalculateRequiredReagents(spell, entity, summary);
			
			// Count and deduct reagents
			if (playerCast != null) {
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(playerCast, row.getKey());
					if (count < row.getValue()) {
						playerCast.sendMessage(new TranslationTextComponent("info.spell.bad_reagent", row.getKey().prettyName()), Util.DUMMY_UUID);
						return EmitCastPostEvent(SpellCastResult.fail(spell, entity, summary));
					}
				}
				
				// actually deduct
				if (!checking) {
					for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
						NostrumMagica.removeReagents(playerCast, row.getKey(), row.getValue());
					}
				}
			}
			
			// Find some way to pay the mana cost
			if (checking) {
				int avail = att.getMana();
				if (avail >= cost) {
					cost = 0;
				} else {
					cost -= avail;
				}
				
				if (cost > 0 && playerCast != null && dragons != null) {
					for (ITameDragon dragon : dragons) {
						int dAvail = dragon.getMana();
						if (dAvail >= cost) {
							cost = 0;
							break;
						} else {
							cost -= dAvail;
						}
					}
				}
				
				if (cost > 0) {
					return EmitCastPostEvent(SpellCastResult.fail(spell, entity, summary));
				}
			} else {
				int avail = att.getMana();
				if (avail >= cost) {
					att.addMana(-cost);
					cost = 0;
				} else {
					att.addMana(-avail);
					cost -= avail;
				}
				
//				if (cost > 0 && playerCast != null && dragons != null) {
//					for (ITameDragon dragon : dragons) {
//						LivingEntity ent = (LivingEntity) dragon;
//						NostrumMagica.instance.proxy.spawnEffect(entity.world, new SpellComponentWrapper(BeamTrigger.instance()),
//								null, entity.getPositionVec().add(0, entity.getEyeHeight(), 0),
//								null, ent.getPositionVec().add(0, ent.getEyeHeight(), 0),
//								new SpellComponentWrapper(EMagicElement.ICE), false, 0);
//						
//						int dAvail = dragon.getMana();
//						if (dAvail >= cost) {
//							dragon.addMana(-cost);
//							cost = 0;
//							break;
//						} else {
//							dragon.addMana(-dAvail);
//							cost -= dAvail;
//						}
//					}
//				}
				
				if (cost > 0) {
					return EmitCastPostEvent(SpellCastResult.fail(spell, entity, summary));
				}
			}
		}
		
		if (!checking) {
			spell.cast(entity, summary.getEfficiency());
			
			// No xp if magic isn't unlocked
			if (!att.isUnlocked()) {
				xp = 0;
			}
			
			att.addXP(xp);
			
			for (SpellEffectPart effect : spell.getSpellEffectParts()) {
				final double attribute = (entity.getAttributeValue(NostrumAttributes.GetXPAttribute(effect.getElement()))
						+ entity.getAttributeValue(NostrumAttributes.xpAllElements))
						/ 100.0;
				int elemXP = effect.getElementCount();
				if (attribute > 0) {
					elemXP += (int) attribute;
					float partial = (float) (attribute - (int) attribute);
					if (partial > 0) {
						 elemXP += (NostrumMagica.rand.nextFloat() < partial ? 1 : 0);
					}
				}
				
				att.addElementXP(effect.getElement(), elemXP);
			}
		
			if (!tool.isEmpty() && tool.getItem() instanceof ISpellCastingTool) {
				((ISpellCastingTool) tool.getItem()).onFinishCastFromTool(entity, summary, tool);
			}
			
			if (!seen && entity instanceof PlayerEntity) {
				PlayerStatTracker.Update((PlayerEntity) entity, (stats) -> stats.incrStat(PlayerStat.UniqueSpellsCast));
			}
		}
		
		return EmitCastPostEvent(new SpellCastResult(true, spell, entity, summary));
	}
	
	public static final int CalculateEffectiveSpellWeight(Spell spell, @Nullable LivingEntity caster, SpellCastSummary summary) {
		final int base = spell.getWeight();
		final int bonus = summary.getWeightDiscount();
		return Math.max(0, base - bonus);
	}
	
	public static final int CalculateSpellCooldown(Spell spell, @Nullable LivingEntity caster, SpellCastSummary summary) {
		final int weight = CalculateEffectiveSpellWeight(spell, caster, summary);
		return 20 * (weight + 1);
	}
	
	public static final int CalculateSpellCooldown(SpellCastResult result) {
		return CalculateSpellCooldown(result.spell, result.caster, result.summary);
	}
	
	public static final int CalculateGlobalSpellCooldown(Spell spell, @Nullable LivingEntity caster, SpellCastSummary summary) {
		final int weight = CalculateEffectiveSpellWeight(spell, caster, summary);
		int base = 40;
		if (caster != null && NostrumMagica.getMagicWrapper(caster) != null) {
			if (NostrumMagica.getMagicWrapper(caster).hasSkill(NostrumSkills.Spellcasting_CooldownReduc)) {
				base = 10;
			}
		}
		return base + (5 * weight);
	}
	
	public static final int CalculateGlobalSpellCooldown(SpellCastResult result) {
		return CalculateGlobalSpellCooldown(result.spell, result.caster, result.summary);
	}
	
	public static final boolean CalculateSpellReagentFree(Spell spell, @Nullable LivingEntity caster, SpellCastSummary summary) {
		int spellWeight = CalculateEffectiveSpellWeight(spell, caster, summary);
		return spellWeight <= 0;
	}
	
	private static final Map<ReagentType, Integer> NoReagentCost;
	static { NoReagentCost = new EnumMap<>(ReagentType.class); for (ReagentType t : ReagentType.values()) NoReagentCost.put(t, 0); }
	
	/**
	 * Calculate the required reagents adjusted for any weight calculations and per the provided summary.
	 * @param spell
	 * @param caster
	 * @param tome
	 */
	private static final Map<ReagentType, Integer> CalculateRequiredReagents(Spell spell, @Nullable LivingEntity caster, SpellCastSummary summary) {
		// If weight is reduced to 0, no reagents needed. Otherwise, all reagents needed.
		if (CalculateSpellReagentFree(spell, caster, summary)) {
			return NoReagentCost;
		} else {
			Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
			ApplyReagentRate(reagents, summary.getReagentCost());
			return reagents;
		}
	}
	
	private static final void ApplyReagentRate(Map<ReagentType, Integer> reagents, float reagentCost) {
		// Take the total reagent cost rate and scale up/down the number of reagents needed
		int whole = (int) reagentCost;
		float frac = reagentCost - (int) reagentCost;
		for (ReagentType type : reagents.keySet()) {
			// rate 1 just means whatever cost is there * 1
			// rate .5 means 50% chance to avoid each one. So if 10
			// are required, each rolls with 50% chance of still being needed.
			// rate 1.5 means 100% of the requirements + a roll for each with
			// 50% chance of adding another.
			int cost;
			if (reagentCost <= 0f) {
				cost = 0;
			} else {
				int def = reagents.get(type);
				cost = (def * whole);
				for (; def > 0; def--) {
					if (NostrumMagica.rand.nextFloat() < frac)
						cost++;
				}
			}
			
			reagents.put(type, cost);
		}
	}
}
