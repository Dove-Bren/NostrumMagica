package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellRune.AlterationSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.ElementSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.ShapeSpellRune;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;

import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class RuneCombineRecipe extends SpecialRecipe {
	
	public static final String SERIALIZER_ID = "crafting_special_nostrum_rune";

	public RuneCombineRecipe() {
		this(new ResourceLocation(NostrumMagica.MODID, "nostrum.recipe.rune"));
	}
	
	public RuneCombineRecipe(ResourceLocation idIn) {
		super(idIn);
	}

	@Override
	public boolean matches(CraftingInventory inv, World worldIn) {
		@Nullable SpellShape shape = null;
		@Nullable EAlteration alteration = null;
		@Nullable EMagicElement element = null;
		int elementCount = 0;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			if (!(stack.getItem() instanceof SpellRune))
				return false;
			
			if (SpellRune.isTrigger(stack)) {
				return false;
			}
			
			if (SpellRune.isPackedShape(stack)) {
				return false;
			}
			
			if (SpellRune.isShape(stack)) {
				if (shape != null) {
					return false; // Already found a shape
				}
				
				ShapeSpellRune shapeRune = (ShapeSpellRune) stack.getItem();
				shape = shapeRune.getShape();
				continue;
			}
			
			if (SpellRune.isAlteration(stack)) {
				if (alteration != null) {
					return false; // Only one alteration allowed
				}
				
				AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
				alteration = altRune.getAlteration();
				continue;
			}
			
			if (SpellRune.isElement(stack)) {
				EMagicElement runeElem = ((ElementSpellRune) stack.getItem()).getElement();
				if (element != null && element != runeElem) {
					// Already have an element, and it's different than this rune's
					return false;
				}
				
				if (elementCount + 1 > 4) {
					// Have too many elements
					return false;
				}
				
				element = runeElem;
				elementCount += 1;
				continue;
			}
			
			NostrumMagica.logger.warn("Found unknown rune type while doing rune combine recipe");
			return false; // What is this?
		}
		
		// If we've got this far, we didn't find any dupes but we still may not have all required pieces. Check
		return shape != null
				&& element != null // implies count [1-4]
				// not checking alteration because it 's optional
				;
		
		
	}

	@Override
	public @Nonnull ItemStack getCraftingResult(CraftingInventory inv) {
		@Nullable SpellShape shape = null;
		@Nullable SpellPartParam params = null;
		@Nullable EAlteration alteration = null;
		@Nullable EMagicElement element = null;
		int elementCount = 0;
		
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			if (!(stack.getItem() instanceof SpellRune))
				return ItemStack.EMPTY;
			
			if (SpellRune.isTrigger(stack)) {
				return ItemStack.EMPTY;
			}
			
			if (SpellRune.isPackedShape(stack)) {
				return ItemStack.EMPTY;
			}
			
			if (SpellRune.isShape(stack)) {
				if (shape != null) {
					return ItemStack.EMPTY; // Already found a shape
				}
				
				ShapeSpellRune shapeRune = (ShapeSpellRune) stack.getItem();
				shape = shapeRune.getShape();
				params = SpellRune.getPieceParam(stack);
				continue;
			}
			
			if (SpellRune.isAlteration(stack)) {
				if (alteration != null) {
					return ItemStack.EMPTY; // Only one alteration allowed
				}
				
				AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
				alteration = altRune.getAlteration();
				continue;
			}
			
			if (SpellRune.isElement(stack)) {
				EMagicElement runeElem = ((ElementSpellRune) stack.getItem()).getElement();
				if (element != null && element != runeElem) {
					// Already have an element, and it's different than this rune's
					return ItemStack.EMPTY;
				}
				
				if (elementCount + 1 > 4) {
					// Have too many elements
					return ItemStack.EMPTY;
				}
				
				element = runeElem;
				elementCount += 1;
				continue;
			}
			
			NostrumMagica.logger.warn("Found unknown rune type while doing rune combine recipe");
			return ItemStack.EMPTY; // What is this?
		}
		
		// If we've got this far, we didn't find any dupes but we still may not have all required pieces. Check
		if (shape != null
				&& element != null // implies count [1-4]
				// not checking alteration because it 's optional
				) {
			
			// 1 => 1
			// 2 => 2
			// 3 => 2
			// 4 => 3
			// log2(count) + 1
			// log2(count) = log(count) / log(2)
			int elemLevel = 1 + (int) (Math.log(elementCount) / Math.log(2));
			
			ItemStack output = SpellRune.getRune(shape, element, elemLevel, alteration);
			SpellRune.setPieceParam(output, params);
			return output;
		}
		
		return ItemStack.EMPTY;
	}

	@Override
	public NonNullList<ItemStack> getRemainingItems(CraftingInventory inv) {
		return NonNullList.withSize(inv.getSizeInventory(), ItemStack.EMPTY);
	}

	@Override
	public boolean canFit(int width, int height) {
		return width * height >= 4;
	}

	@Override
	public IRecipeSerializer<?> getSerializer() {
		return NostrumCrafting.runeCombineSerializer;
	}
	
}
