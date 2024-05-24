package com.smanzana.nostrummagica.spells;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.attributes.NostrumAttributes;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.ISpellCastingTool;
import com.smanzana.nostrummagica.items.ISpellEquipment;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.triggers.BeamTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class SpellCasting {
	
	public static final boolean AttemptScrollCast(Spell spell, LivingEntity entity) {
		return AttemptCast(spell, entity, ItemStack.EMPTY, true, false);
	}
	
	public static final boolean AttemptToolCast(Spell spell, LivingEntity entity, ItemStack tool) {
		final boolean freeCast = entity instanceof PlayerEntity
				? ((PlayerEntity) entity).isCreative()
				: false;
		return AttemptCast(spell, entity, tool, freeCast, false);
	}
	
	public static final boolean CheckToolCast(Spell spell, LivingEntity entity, ItemStack tool) {
		final boolean freeCast = entity instanceof PlayerEntity
				? ((PlayerEntity) entity).isCreative()
				: false;
		return AttemptCast(spell, entity, tool, freeCast, true);
	}

	protected static final boolean AttemptCast(Spell spell, LivingEntity entity, ItemStack tool, boolean freeCast, boolean checking) {
		INostrumMagic att = NostrumMagica.getMagicWrapper(entity);
		@Nullable PlayerEntity playerCast = (entity instanceof PlayerEntity) ? (PlayerEntity) entity : null;
		
		if (att == null) {
			NostrumMagica.logger.warn("Could not look up entity magic wrapper");
			return false;
		}
		
		// Check that the player can cast this (if it's not a scroll/creative)
		if (!freeCast) {
			List<ITextComponent> problems = new ArrayList<>(4);
			if (!NostrumMagica.canCast(spell, att, problems)) {
				NostrumMagica.logger.warn("Got cast message from client with too low of stats. They should relog... " + entity);
				for (ITextComponent problem : problems) {
					entity.sendMessage(problem, Util.DUMMY_UUID);
				}
				return false;
			}
		}
		
		// Cast it!
		boolean seen = att.wasSpellDone(spell);
		SpellCastSummary summary = new SpellCastSummary(spell.getManaCost(), spell.getXP(seen));
		
		// Add player's base magic potency
		summary.addEfficiency((float) entity.getAttribute(NostrumAttributes.magicPotency).getValue() / 100f);
		
		// Add the player's personal bonuses
		summary.addCostRate(att.getManaCostModifier());
		
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
				summary.addCostRate(.2f - lrc); // Add however much we need to get to .2
		}
		
		// Visit an equipped spell armor
		ISpellEquipment.ApplyAll(entity, summary);
		
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
				return false;
			}
			
			reagents = CalculateRequiredReagents(spell, entity, summary);
			
			// Count and deduct reagents
			if (playerCast != null) {
				for (Entry<ReagentType, Integer> row : reagents.entrySet()) {
					int count = NostrumMagica.getReagentCount(playerCast, row.getKey());
					if (count < row.getValue()) {
						playerCast.sendMessage(new TranslationTextComponent("info.spell.bad_reagent", row.getKey().prettyName()), Util.DUMMY_UUID);
						return false;
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
					return false;
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
				
				if (cost > 0 && playerCast != null && dragons != null) {
					for (ITameDragon dragon : dragons) {
						LivingEntity ent = (LivingEntity) dragon;
						NostrumMagica.instance.proxy.spawnEffect(entity.world, new SpellComponentWrapper(BeamTrigger.instance()),
								null, entity.getPositionVec().add(0, entity.getEyeHeight(), 0),
								null, ent.getPositionVec().add(0, ent.getEyeHeight(), 0),
								new SpellComponentWrapper(EMagicElement.ICE), false, 0);
						
						int dAvail = dragon.getMana();
						if (dAvail >= cost) {
							dragon.addMana(-cost);
							cost = 0;
							break;
						} else {
							dragon.addMana(-dAvail);
							cost -= dAvail;
						}
					}
				}
				
				if (cost > 0) {
					return false;
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
		
			if (!tool.isEmpty() && tool.getItem() instanceof ISpellCastingTool) {
				((ISpellCastingTool) tool.getItem()).onFinishCastFromTool(entity, summary, tool);
			}
		}
		
		return true;
	}
	
	public static final int CalculateEffectiveSpellWeight(Spell spell, @Nullable LivingEntity caster, SpellCastSummary summary) {
		final int base = spell.getWeight();
		final int bonus = summary.getWeightDiscount();
		return Math.max(0, base - bonus);
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
		int spellWeight = CalculateEffectiveSpellWeight(spell, caster, summary);
		if (spellWeight <= 0) {
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
