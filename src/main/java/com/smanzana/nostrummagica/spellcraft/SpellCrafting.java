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
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;
import com.smanzana.nostrummagica.spells.components.shapes.SpellShape;

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
		
		// All shapes must be first. (This isn't enforced or required by the rest of spellcrafting logic.)
		boolean foundElement = false;
		for (int i = startIdx; i < startIdx + slotCount; i++) {
			stack = inventory.getStackInSlot(i);
			if (stack.isEmpty()) {
				break;
			}
			
			if (SpellRune.isElement(stack)) {
				foundElement = true;
			} else if (SpellRune.isAlteration(stack)) {
				if (!foundElement) {
					errorsOut.add("Alteration in slot " + (i - (startIdx-1)) + " should have elements before it");
					valid = false;
				}
			} else if (SpellRune.isShape(stack)) {
				if (foundElement) {
					errorsOut.add("Shape in slot " + (i - (startIdx-1)) + " is not allowed after effects have started");
					valid = false;
				}
			}
		}
		
		// Verify we found at least one good shape
		if (!foundElement) {
			valid = false;
			errorsOut.add("Spell must contain at least one element");
		}
		
		return valid;
	}
	
	public static @Nullable Spell CreateSpellFromRunes(SpellCraftContext context, @Nullable SpellCraftPattern pattern,
			String spellName, IInventory inventory, int startIdx, int slotCount,
			@Nullable List<String> errorsOut, @Nullable List<SpellPartSummary> partSummaryOut) {
		List<SpellPart> parts = new ArrayList<>(slotCount);
		boolean parseSuccess = ParseRunes(inventory, startIdx, slotCount, context, pattern, parts, errorsOut);
		
		// Convert any parts that were made to summaries (even on failure)
		if (partSummaryOut != null) {
			CalculateManaCost(parts);
			CalculateWeight(parts);
			
			for (SpellPart part : parts) {
				partSummaryOut.add(new SpellPartSummary(part));
			}
			
			if (!parseSuccess) {
				final int badIdx;
				if (parts.isEmpty()) {
					badIdx = 0;
				} else {
					badIdx = parts.get(parts.size() - 1).endIdx + 1;
				}
				partSummaryOut.add(new SpellPartSummary(badIdx, badIdx));
			}
		}
		
		if (!parseSuccess) {
			return null;
		}
		
		return CreateSpellFromPartsInternal(spellName, parts, true);
	}
	
//	public static Spell CreateSpellFromParts(SpellCraftContext context, @Nullable SpellCraftPattern pattern, String spellName, List<SpellPart> parts, boolean trans) {
//		return CreateSpellFromPartsInternal(context, pattern, spellName, parts, trans);
//	}
	
	protected static Spell CreateSpellFromPartsInternal(String spellName, List<SpellPart> parts, boolean trans) {
		
		final int manaCost = CalculateManaCost(parts);
		final int weight = CalculateWeight(parts);
		Spell spell = new Spell(spellName, trans, manaCost, weight);
		for (SpellPart part : parts) {
			if (part.isShape()) {
				spell.addPart(part.getShapePart());
			} else {
				spell.addPart(part.getEffectPart());
			}
		}
		
		return spell;
	}
	
	public static final int CalculateManaCost(SpellShapePart part) {
		return CalculateManaCost(part, 1f);
	}
	
	protected static final int CalculateManaCost(SpellShapePart part, float multiplier) {
		float cost = multiplier * (float) part.getShape().getManaCost();
		return (int) Math.ceil(cost);
	}
	
	public static final int CalculateManaCost(SpellEffectPart part) {
		return CalculateManaCost(part, 1f);
	}
	
	protected static final int CalculateManaCost(SpellEffectPart part, float multiplier) {
		float cost = 0f;
		
		cost += multiplier * 10f;
		if (part.getElementCount() > 1)
			cost += multiplier * (float) (20 * (part.getElementCount() - 1));
		if (part.getAlteration() != null)
			cost += multiplier * (float) part.getAlteration().getCost();
			
		return (int) Math.ceil(cost);
	}
	
	protected static int CalculateManaCost(SpellPart part, float multiplier) {
		int cost;
		if (part.isShape()) {
			cost = Math.max(0, CalculateManaCost(part.getShapePart(), multiplier * part.getManaRate()));
			part.finalMana = cost;
			return cost;
		} else {
			cost = Math.max(0, CalculateManaCost(part.getEffectPart(), multiplier * part.getManaRate()));
			part.finalMana = cost;
			return cost;
		}
	}
	
	protected static int CalculateManaCost(List<SpellPart> parts) {
		// Rolling multiplier makes it more expensive for one long spell vs many small
		// (rate of 1.1x)
		int cost = 0;
		float multiplier = 1f;
		
		for (SpellPart part : parts) {
			cost += CalculateManaCost(part, multiplier);
			multiplier *= 1.1;
		}
		return cost;
	}
	
	public static int CalculateManaCostFromRunes(SpellCraftContext context, @Nullable SpellCraftPattern pattern, IInventory inventory, int startIdx, int slotCount) {
		List<SpellPart> parts = new ArrayList<>(slotCount);
		ParseRunes(inventory, startIdx, slotCount, context, pattern, parts, null);
		// Not checking return to run on whatever we CAN parse
		return CalculateManaCost(parts);
	}
	
	public static final int CalculateWeight(SpellShape shape) {
		// Shapes report their own weight
		return shape.getWeight();
	}
	
	public static final int CalculateWeight(SpellShapePart part) {
		return CalculateWeight(part.getShape());
	}
	
	public static final int CalculateWeight(EMagicElement element, int elementCount, @Nullable EAlteration alteration) {
		// Elements are free but alterations may have weight
		if (alteration != null) {
			return alteration.getWeight();
		} else {
			return 0;
		}
	}
	
	public static final int CalculateWeight(SpellEffectPart part) {
		return CalculateWeight(part.getElement(), part.getElementCount(), part.getAlteration());
	}
	
	protected static int CalculateWeight(SpellPart part) {
		int cost;
		if (part.isShape()) {
			cost = Math.max(0, CalculateWeight(part.getShapePart()) + part.getWeightBonus());
			part.finalWeight = cost;
			return cost;
		} else {
			cost = Math.max(0, CalculateWeight(part.getEffectPart()) + part.getWeightBonus());
			part.finalWeight = cost;
			return cost;
		}
	}
	
	protected static int CalculateWeight(List<SpellPart> parts) {
		int weight = 0;
		for (SpellPart part : parts) {
			weight += CalculateWeight(part);
		}
		return weight;
	}
	
	public static int CalculateWeightFromRunes(SpellCraftContext context, @Nullable SpellCraftPattern pattern, IInventory inventory, int startIdx, int slotCount) {
		List<SpellPart> parts = new ArrayList<>(slotCount);
		ParseRunes(inventory, startIdx, slotCount, context, pattern, parts, null);
		// Not checking return to run on whatever we CAN parse
		return CalculateWeight(parts);
	}
	
	/**
	 * Parses a sequence of runes in an inventory into shape and effect parts.
	 * @param context
	 * @param pattern
	 * @param inventory
	 * @param startIdx
	 * @param slotCount
	 * @param ingredient
	 * @return whether parsing was successful (didn't reach any invalid sequence)
	 */
	protected static final boolean ParseRunes(IInventory inventory, int startIdx, int slotCount,
			@Nullable SpellCraftContext context, @Nullable SpellCraftPattern pattern,
			List<SpellPart> partsOut, @Nullable List<String> errorsOut) {
		
		@Nullable EMagicElement element = null;
		int elementCount = 0;
		int weightBonus = 0;
		float manaRate = 1f;
		int elemBeginIdx = -1;
		
		int i;
		for (i = 0; i < slotCount; i++) {
			final int slotIdx = startIdx + i;
			ItemStack stack = inventory.getStackInSlot(slotIdx);
			if (stack.isEmpty()) {
				break;
			}
			
			if (!(stack.getItem() instanceof SpellRune)) {
				if (errorsOut != null) {
					errorsOut.add("Item in slot " + slotIdx + " is not a rune");
				}
				break;
			}
			
			// Possibly transform with pattern
			SpellIngredient base = MakeIngredient(stack);
			if (base == null) {
				if (errorsOut != null) {
					errorsOut.add("Invalid rune in slot " + slotIdx);
				}
				break;
			}
			
			SpellIngredient ingredient;
			if (context != null && pattern != null) {
				ISpellCraftModifier modifier = pattern.getModifier(context, i);
				if (modifier != null && modifier.canModify(context, base)) {
					SpellIngredientBuilder builder = new SpellIngredientBuilder(base);
					modifier.modify(context, base, builder);
					ingredient = builder.build();
				} else {
					ingredient = base;
				}
			} else {
				ingredient = base;
			}
			
			
			// Now interpret
			if (ingredient.shape != null) {
				if (element != null) {
					partsOut.add(new SpellPart(elemBeginIdx, i - 1, element, elementCount, null, weightBonus, manaRate));
					element = null;
					elementCount = 0;
					weightBonus = 0;
					manaRate = 1f;
					elemBeginIdx = -1;
				}
				
				partsOut.add(new SpellPart(i, i, ingredient.shape, ingredient.weight, ingredient.manaRate));
			} else if (ingredient.alteration != null) {
				if (element != null) {
					weightBonus += ingredient.weight;
					manaRate += (ingredient.manaRate - 1f);
					partsOut.add(new SpellPart(elemBeginIdx, i, element, elementCount, ingredient.alteration, weightBonus, manaRate));
					element = null;
					elementCount = 0;
					weightBonus = 0;
					manaRate = 1f;
					elemBeginIdx = -1;
				} else {
					if (errorsOut != null) {
						errorsOut.add("Alteration in slot " + slotIdx + " must be proceeded by element runes");
					}
					return false;
				}
			} else {
				EMagicElement runeElement = ingredient.element == null ? EMagicElement.PHYSICAL : ingredient.element;
				if ((element != null && element != runeElement)
						|| elementCount == 3) {
					partsOut.add(new SpellPart(elemBeginIdx, i - 1, element, elementCount, null, weightBonus, manaRate));
					elementCount = 0;
					weightBonus = 0;
					manaRate = 1f;
					elemBeginIdx = -1;
				}
				
				element = runeElement;
				elementCount += 1 + ingredient.elementCountBonus;
				weightBonus += ingredient.weight;
				manaRate += (ingredient.manaRate-1f);
				if (elemBeginIdx == -1) {
					elemBeginIdx = i;
				}
				while (elementCount > 3) { // Don't go at 3 so that an alteration can come next
					partsOut.add(new SpellPart(elemBeginIdx, i, element, 3, null, weightBonus, manaRate));
					elementCount -= 3;
					weightBonus = 0;
					manaRate = 1f;
					elemBeginIdx = i; // Even if multiple start here, it'll be all on this idx
				}
				
				if (elementCount == 0) {
					element = null;
					weightBonus = 0;
					manaRate = 1f;
					elemBeginIdx = -1;
				}
			}
		}
		
		// Check for leftover non-full effect
		if (element != null) {
			partsOut.add(new SpellPart(elemBeginIdx, i-1, element, elementCount, null, weightBonus, manaRate));
		}
		
		return true;
	}
	
	public static final @Nullable SpellIngredient MakeIngredient(ItemStack rune) {
		if (SpellRune.isShape(rune)) {
			return new SpellIngredient(SpellRune.getShapePart(rune), 0, 1f);
		} else if (SpellRune.isAlteration(rune)) {
			return new SpellIngredient(SpellRune.getAlteration(rune), 0, 1f);
		} else if (SpellRune.isElement(rune)) {
			return new SpellIngredient(SpellRune.getElement(rune), 0, 1f, 0);
		} else {
			return null; // Error
		}
	}
	
	protected static final class SpellPart {
		public final int startIdx;
		public final int endIdx;
		
		public final @Nullable SpellShapePart shape;
		// OR
		public final @Nullable SpellEffectPart effect;
		
		public final int weightBonus;
		public final float manaRate;
		
		// Not-so-abstraction-happy place to stash results
		public int finalWeight;
		public int finalMana;
		
		protected SpellPart(int startIdx, int endIdx, SpellShapePart shape, SpellEffectPart effect, int weightBonus, float manaRate) {
			this.startIdx = startIdx;
			this.endIdx = endIdx;
			this.shape = shape;
			this.effect = effect;
			this.weightBonus = weightBonus;
			this.manaRate = manaRate;
		}
		
		public SpellPart(int startIdx, int endIdx, SpellShapePart shape, int weightBonus, float manaRate) {
			this(startIdx, endIdx, shape, null, weightBonus, manaRate);
		}
		
		public SpellPart(int startIdx, int endIdx, SpellShape shape, SpellShapePartProperties props, int weightBonus, float manaRate) {
			this(startIdx, endIdx, new SpellShapePart(shape, props), weightBonus, manaRate);
		}
		
		public SpellPart(int startIdx, int endIdx, SpellShape shape, int weightBonus, float manaRate) {
			this(startIdx, endIdx, shape, shape.getDefaultProperties(), weightBonus, manaRate);
		}
		
		public SpellPart(int startIdx, int endIdx, SpellEffectPart effect, int weightBonus, float manaRate) {
			this(startIdx, endIdx, null, effect, weightBonus, manaRate);
		}
		
		public SpellPart(int startIdx, int endIdx, EMagicElement element, int elementCount, @Nullable EAlteration alteration, int weightBonus, float manaRate) {
			this(startIdx, endIdx, new SpellEffectPart(element, elementCount, alteration), weightBonus, manaRate);
		}
		
		public boolean isShape() {
			return this.shape != null;
		}

//		public @Nullable SpellShape getShape() {
//			return shape != null ? shape.getShape() : null;
//		}
//		
//		public @Nullable SpellShapePartProperties getShapeProperties() {
//			return shape != null ? shape.getProperties() : null;
//		}
//
//		public @Nullable EMagicElement getElement() {
//			return shape == null ? effect.getElement() : null;
//		}
//
//		public int getElementCount() {
//			return shape == null ? effect.getElementCount() : 0;
//		}
//
//		public @Nullable EAlteration getAlteration() {
//			return shape == null ? effect.getAlteration() : null;
//		}
		
		public @Nullable SpellShapePart getShapePart() {
			return shape;
		}
		
		public @Nullable SpellEffectPart getEffectPart() {
			return effect;
		}

		public int getWeightBonus() {
			return weightBonus;
		}

		public float getManaRate() {
			return manaRate;
		}
	}
	
	public static final class SpellPartSummary {
		
		protected final int startIdx;
		protected final int lastIdx;
		protected final boolean isError;
		protected final @Nullable SpellEffectPart effect;
		protected final @Nullable SpellShapePart shape;
		protected final int weight;
		protected final int mana;
		
		private SpellPartSummary(int startIdx, int lastIdx, boolean isError, SpellEffectPart effect,
				SpellShapePart shape, int weight, int mana) {
			this.startIdx = startIdx;
			this.lastIdx = lastIdx;
			this.weight = weight;
			this.mana = mana;
			
			// One of shape and effect have to be null
			if (!isError && !((effect == null) ^ (shape == null))) {
				isError = true;
				effect = null;
				shape = null;
			}
			
			this.isError = isError;
			this.effect = effect;
			this.shape = shape;
		}
		
		/**
		 * ERROR part
		 * @param startIdx
		 * @param lastIdx
		 */
		public SpellPartSummary(int startIdx, int lastIdx) {
			this(startIdx, lastIdx, true, null, null, 0, 0);
		}
		
		public SpellPartSummary(SpellPart part) {
			this(part.startIdx, part.endIdx, false, part.getEffectPart(), part.getShapePart(), 
					part.finalWeight, part.finalMana
					);
		}
		
		public int getStartIdx() {
			return this.startIdx;
		}
		
		public int getLastIdx() {
			return this.lastIdx;
		}
		
		public boolean isError() {
			return this.isError;
		}
		
		public boolean isShape() {
			return !isError() && this.shape != null;
		}
		
		public @Nullable SpellEffectPart getEffect() {
			return isError() ? null : this.effect;
		}
		
		public @Nullable SpellShapePart getShape() {
			return isError() ? null : this.shape;
		}
		
		public int getWeight() {
			return this.weight;
		}
		
		public int getMana() {
			return this.mana;
		}
	}
}
