package com.smanzana.nostrummagica.spells;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;

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
		Spell spell = new Spell(spellName, true);
		SpellPart part;
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			part = SpellRune.getPart(stack);
			if (part == null) {
				NostrumMagica.logger.error("Got null SpellPart from rune: " + stack + " :: " + (stack.hasTag() ? stack.getTag().toString() : "NO NBT"));
				return null;
			} else {
				spell.addPart(part);
			}
		}
		
		return spell;
	}
}
