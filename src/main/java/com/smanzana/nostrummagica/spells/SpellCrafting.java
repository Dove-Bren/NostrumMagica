package com.smanzana.nostrummagica.spells;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;

public class SpellCrafting {
	
	public static boolean CanCraftSpells(PlayerEntity player) {
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr == null || !attr.isUnlocked() || !attr.getCompletedResearches().contains("spellcraft");
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
	
	public static @Nullable Spell CreateSpellFromRunes(String spellName, IInventory inventory, int startIdx, int slotCount) {
		
		List<SpellPart> parts = new ArrayList<>(slotCount);
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			SpellPart part = SpellRune.getPart(stack);
			if (part == null) {
				NostrumMagica.logger.error("Got null SpellPart from rune: " + stack + " :: " + (stack.hasTag() ? stack.getTag().toString() : "NO NBT"));
				return null;
			} else {
				parts.add(part);
			}
		}
		
		return CreateSpellFromParts(spellName, parts, true);
	}
	
	public static Spell CreateSpellFromParts(String spellName, List<SpellPart> parts, boolean trans) {
		final int manaCost = CalculateManaCost(parts);
		final int weight = CalculateWeight(parts);
		Spell spell = new Spell(spellName, trans, manaCost, weight);
		for (SpellPart part : parts) {
			spell.addPart(part);
		}
		return spell;
	}
	
	protected static int CalculateManaCost(List<SpellPart> parts) {
		// Triggers can report their  cost
		// Alterations are in enum
		// Shapes cost 10
		// First elem is free. Extra costs 20 ea
		// Rolling multiplier makes it more expensive for one long spell vs many small
		// (rate of 1.1x)
		float cost = 0f;
		float multiplier = 1f;
		
		for (SpellPart part : parts) {
			if (part.isTrigger())
				cost += multiplier * (float) part.getTrigger().getManaCost();
			else {
				cost += multiplier * 10f;
				if (part.getElementCount() > 1)
					cost += multiplier * (float) (20 * (part.getElementCount() - 1));
				if (part.getAlteration() != null)
					cost += multiplier * (float) part.getAlteration().getCost();
			}
			multiplier *= 1.1;
		}
		
		return (int) Math.ceil(cost);
	}
	
	public static final int CalculateWeight(SpellShape shape, EMagicElement element, int elementCount, @Nullable EAlteration alteration) {
		// In shapes, the shape itself and alteration report their own cost.
		// Elements are free.
		int weight = shape.getWeight();
		if (alteration != null) {
			weight += alteration.getWeight();
		}
		return weight;
	}
	
	protected static int CalculateWeight(List<SpellPart> parts) {
		// Triggers report their own cost.
		// In shapes, the shape itself and alteration report their own cost.
		// Elements are free.
		int weight = 0;
		for (SpellPart part : parts) {
			if (part.isTrigger()) {
				weight += part.getTrigger().getWeight();
			} else {
				weight += CalculateWeight(part.getShape(), part.getElement(), part.getElementCount(), part.getAlteration());
			}
		}
		return weight;
	}
	
	public static int CalculateWeightFromRunes(IInventory inventory, int startIdx, int slotCount) {
		
		List<SpellPart> parts = new ArrayList<>(slotCount);
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			SpellPart part = SpellRune.getPart(stack);
			if (part == null) {
				NostrumMagica.logger.error("Got null SpellPart from rune: " + stack + " :: " + (stack.hasTag() ? stack.getTag().toString() : "NO NBT"));
			} else {
				parts.add(part);
			}
		}
		
		return CalculateWeight(parts);
	}
}
