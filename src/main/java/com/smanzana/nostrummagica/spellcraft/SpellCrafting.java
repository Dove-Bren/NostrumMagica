package com.smanzana.nostrummagica.spellcraft;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.LegacySpell;
import com.smanzana.nostrummagica.spells.LegacySpellPart;
import com.smanzana.nostrummagica.spells.components.LegacySpellShape;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SpellCrafting {
	
	public static boolean CanCraftSpells(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr != null && attr.isUnlocked() && attr.getCompletedResearches().contains("spellcraft");
	}
	
	public static boolean CheckForValidRunes(IInventory inventory, int startIdx, int slotCount, @Nonnull List<String> errorsOut) {
		@Nonnull ItemStack stack;
		boolean valid = true;
		
		// Verify first slot is a trigger
		stack = inventory.getStackInSlot(startIdx);
		if (stack.isEmpty() || !SpellRune.isTrigger(stack)) {
			errorsOut.add("Spell must begin with a trigger");
			valid = false;
		}
		
		// Check each next rune, paying attention to whether a shape is present
		boolean foundShape = false;
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			if (!SpellRune.isSpellWorthy(stack)) {
				errorsOut.add("Rune in slot " + (i - (startIdx-1)) + " is not allowed.");
				
				// This builds on the assumption that the two spellworthy types are triggers and packed shapes
				errorsOut.add("  -> Shapes, Elements, and Alterations must be combined into a Packed Shape first.");
				valid = false;
			} else if (SpellRune.isPackedShape(stack)) {
				foundShape = true;
			}
		}
		
		// Verify we found at least one good shape
		if (!foundShape) {
			valid = false;
			errorsOut.add("Spell must contain at least one shape");
		}
		
		return valid;
	}
	
	protected static List<SpellPartBuilder> MakeIngredients(@Nullable SpellCraftContext context, @Nullable SpellCraftPattern pattern, List<LegacySpellPart> parts) {
		List<SpellPartBuilder> ingredients = new ArrayList<>(parts.size());
		
		for (int i = 0; i < parts.size(); i++) {
			final LegacySpellPart part = parts.get(i);
			final SpellPartBuilder builder = new SpellPartBuilder(part);
			
			if (context != null && pattern != null) {
				@Nullable ISpellCraftModifier modifier = pattern.getModifier(context, i);
				if (modifier != null) {
					modifier.modify(context, part, builder);
				}
			}

			ingredients.add(builder);
		}
		return ingredients;
	}
	
	public static @Nullable LegacySpell CreateSpellFromRunes(SpellCraftContext context, @Nullable SpellCraftPattern pattern, String spellName, IInventory inventory, int startIdx, int slotCount) {
		List<LegacySpellPart> parts = new ArrayList<>(slotCount);
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			LegacySpellPart part = SpellRune.getPart(stack);
			if (part == null) {
				NostrumMagica.logger.error("Got null SpellPart from rune: " + stack + " :: " + (stack.hasTag() ? stack.getTag().toString() : "NO NBT"));
				return null;
			} else {
				parts.add(part);
			}
		}
		
		return CreateSpellFromParts(context, pattern, spellName, parts, true);
	}
	
	public static LegacySpell CreateSpellFromParts(SpellCraftContext context, @Nullable SpellCraftPattern pattern, String spellName, List<LegacySpellPart> parts, boolean trans) {
		return CreateSpellFromPartsInternal(context, pattern, spellName, parts, trans);
	}
	
	/**
	 * Specialized spell creation when there isn't a player context available.
	 * Examples include randomly-generated spells in dungeons.
	 * @param spellName
	 * @param parts
	 * @param trans
	 * @return
	 */
	public static LegacySpell CreateSpellFromPartsNoContext(String spellName, List<LegacySpellPart> parts, boolean trans) {
		return CreateSpellFromPartsInternal(null, null, spellName, parts, trans);
	}
	
	protected static LegacySpell CreateSpellFromPartsInternal(@Nullable SpellCraftContext context, @Nullable SpellCraftPattern pattern, String spellName, List<LegacySpellPart> parts, boolean trans) {
		
		List<SpellPartBuilder> ingredients = MakeIngredients(context, pattern, parts);
		
		final int manaCost = CalculateManaCost(ingredients);
		final int weight = CalculateWeight(ingredients);
		LegacySpell spell = new LegacySpell(spellName, trans, manaCost, weight);
		for (SpellPartBuilder ingredient : ingredients) {
			spell.addPart(ingredient.build());
		}
		
		return spell;
	}
	
	protected static int CalculateManaCost(List<SpellPartBuilder> parts) {
		// Triggers can report their  cost
		// Alterations are in enum
		// Shapes cost 10
		// First elem is free. Extra costs 20 ea
		// Rolling multiplier makes it more expensive for one long spell vs many small
		// (rate of 1.1x)
		int cost = 0;
		float multiplier = 1f;
		
		for (SpellPartBuilder part : parts) {
			cost += CalculateManaCost(part.build(), multiplier * part.getManaRate());
			multiplier *= 1.1;
		}
		
		return cost;
	}
	
	public static final int CalculateManaCost(LegacySpellPart part) {
		return CalculateManaCost(part, 1f);
	}
	
	protected static final int CalculateManaCost(LegacySpellPart part, float multiplier) {
		float cost = 0f;
		if (part.isTrigger())
			cost += multiplier * (float) part.getTrigger().getManaCost();
		else {
			cost += multiplier * 10f;
			if (part.getElementCount() > 1)
				cost += multiplier * (float) (20 * (part.getElementCount() - 1));
			if (part.getAlteration() != null)
				cost += multiplier * (float) part.getAlteration().getCost();
		}
		return (int) Math.ceil(cost);
	}
	
	public static int CalculateManaCostFromRunes(SpellCraftContext context, @Nullable SpellCraftPattern pattern, IInventory inventory, int startIdx, int slotCount) {
		
		List<LegacySpellPart> parts = new ArrayList<>(slotCount);
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			LegacySpellPart part = SpellRune.getPart(stack);
			if (part == null) {
				NostrumMagica.logger.error("Got null SpellPart from rune: " + stack + " :: " + (stack.hasTag() ? stack.getTag().toString() : "NO NBT"));
			} else {
				parts.add(part);
			}
		}
		
		return CalculateManaCost(MakeIngredients(context, pattern, parts));
	}
	
	public static final int CalculateWeight(LegacySpellShape shape, EMagicElement element, int elementCount, @Nullable EAlteration alteration) {
		// In shapes, the shape itself and alteration report their own cost.
		// Elements are free.
		int weight = shape.getWeight();
		if (alteration != null) {
			weight += alteration.getWeight();
		}
		return weight;
	}
	
	public static final int CalculateWeight(LegacySpellPart part) {
		final int weight;
		if (part.isTrigger()) {
			weight = part.getTrigger().getWeight();
		} else {
			weight = CalculateWeight(part.getShape(), part.getElement(), part.getElementCount(), part.getAlteration());
		}
		return weight;
	}
	
	protected static int CalculateWeight(List<SpellPartBuilder> parts) {
		// Triggers report their own cost.
		// In shapes, the shape itself and alteration report their own cost.
		// Elements are free.
		int weight = 0;
		for (SpellPartBuilder part : parts) {
			final int partWeight = Math.max(0, CalculateWeight(part.build()) + part.getWeightModifier());
			weight += partWeight;
		}
		return weight;
	}
	
	public static int CalculateWeightFromRunes(SpellCraftContext context, @Nullable SpellCraftPattern pattern, IInventory inventory, int startIdx, int slotCount) {
		
		List<LegacySpellPart> parts = new ArrayList<>(slotCount);
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			LegacySpellPart part = SpellRune.getPart(stack);
			if (part == null) {
				NostrumMagica.logger.error("Got null SpellPart from rune: " + stack + " :: " + (stack.hasTag() ? stack.getTag().toString() : "NO NBT"));
			} else {
				parts.add(part);
			}
		}
		
		return CalculateWeight(MakeIngredients(context, pattern, parts));
	}
}
