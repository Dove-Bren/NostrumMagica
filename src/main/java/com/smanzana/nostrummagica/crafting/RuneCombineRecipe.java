package com.smanzana.nostrummagica.crafting;

import javax.annotation.Nonnull;

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
import net.minecraft.item.crafting.SpecialRecipeSerializer;
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
		boolean foundTwo = false; // Found at least two runes
		boolean shape = false;
		EMagicElement element = null;
		EAlteration alteration = null;
		int count = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			if (!(stack.getItem() instanceof SpellRune))
				return false;
			
			if (SpellRune.isTrigger(stack))
				return false;
			
			if (SpellRune.isShape(stack)) {
				if (shape) {
					// We already found a shape
					return false;
				}
				shape = true;
				
				ShapeSpellRune shapeRune = (ShapeSpellRune) stack.getItem();
				
				if (element != null || alteration != null)
					foundTwo = true;
				
				EMagicElement shapeElem = shapeRune.getNestedElement(stack);
				if (element != null && shapeElem != null && shapeElem != element) {
					return false; // multiple elements
				}
				if (shapeElem != null && element == null)
					element = shapeElem;
				EAlteration alt = shapeRune.getNestedAlteration(stack);
				if (alt != null && alteration != null) {
					// Can't have two alterations
					return false;
				}
				if (alt != null && alteration == null)
					alteration = alt;
				int shapeCount = reverseElementCount(shapeRune.getNestedElementCount(stack));
				if (count + shapeCount > 4)
					return false;
				count += shapeCount;
			} else if (SpellRune.isElement(stack)) {
				ElementSpellRune elementRune = (ElementSpellRune) stack.getItem();
				if (elementRune.getElement() == null)
					return false; // CORRUPT
				if (element != null && elementRune.getElement() != element) 
					return false; // Different element types
				
				if (shape || alteration != null || element != null)
					foundTwo = true;
				
				element = elementRune.getElement();
				
				int c = 1;//reverseElementCount(getPieceElementCount(stack)); TODO remove trying to combine elements
				if (c + count > 4)
					return false;
				count += c;
			} else if (SpellRune.isAlteration(stack)) {
				AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
				if (altRune.getAlteration() == null)
					return false; // CORRUPT
				if (alteration != null)
					return false;
				alteration = altRune.getAlteration();
				
				if (element != null || shape)
					foundTwo = true;
			}
		}
		
		return foundTwo && (shape || ((alteration == null) != (element == null)));
	}

	private int reverseElementCount(int pieceElementCount) {
		return (int) Math.pow(2, pieceElementCount - 1);
		// 1->1
		// 2->2
		// 3->4
	}

	@Override
	public @Nonnull ItemStack getCraftingResult(CraftingInventory inv) {
		SpellShape shape = null;
		EMagicElement element = null;
		EAlteration alteration = null;
		SpellPartParam params = null;
		int count = 0;
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack stack = inv.getStackInSlot(i);
			if (stack.isEmpty())
				continue;
			
			if (!(stack.getItem() instanceof SpellRune))
				return ItemStack.EMPTY;
			
			if (SpellRune.isTrigger(stack))
				return ItemStack.EMPTY;
			
			if (SpellRune.isShape(stack)) {
				if (shape != null) {
					// We already found a shape
					return ItemStack.EMPTY;
				}
				
				ShapeSpellRune shapeRune = (ShapeSpellRune) stack.getItem();
				shape = shapeRune.getShape(stack);
				if (shape == null)
					return ItemStack.EMPTY;
				
				params = SpellRune.getPieceParam(stack);
				
				EMagicElement shapeElem = shapeRune.getNestedElement(stack);
				if (element != null && shapeElem != null && shapeElem != element) {
					return ItemStack.EMPTY; // multiple elements
				}
				if (shapeElem != null)
					element = shapeElem;
				EAlteration alt = shapeRune.getNestedAlteration(stack);
				if (alt != null && alteration != null) {
					// Can't have two alterations
					return ItemStack.EMPTY;
				}
				if (alt != null && alteration == null)
					alteration = alt;
				
				int shapeCount = reverseElementCount(shapeRune.getNestedElementCount(stack));
				if (count + shapeCount > 4)
					return ItemStack.EMPTY;
				count += shapeCount;
			} else if (SpellRune.isElement(stack)) {
				ElementSpellRune elementRune = (ElementSpellRune) stack.getItem();
				if (elementRune.getElement() == null)
					return ItemStack.EMPTY; // CORRUPT
				if (element != null && elementRune.getElement() != element) 
					return ItemStack.EMPTY; // Different element types
				
				element = elementRune.getElement();
				int c = 1;//reverseElementCount(SpellRune.getPieceElementCount(stack));
				if (c + count > 4)
					return ItemStack.EMPTY;
				count += c;
			} else if (SpellRune.isAlteration(stack)) {
				AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
				if (altRune.getAlteration() == null)
					return ItemStack.EMPTY; // CORRUPT
				if (alteration != null)
					return ItemStack.EMPTY;
				alteration = altRune.getAlteration();
			}
		}
		
		if (shape != null) {
			ItemStack rune = SpellRune.getRune(shape);
			ShapeSpellRune direct = (ShapeSpellRune) rune.getItem();
			SpellRune.setPieceParam(rune, params);
			if (element != null) {
				// 1 => 1
				// 2 => 2
				// 3 => 2
				// 4 => 3
				// log2(count) + 1
				// log2(count) = log(count) / log(2)
				int elemcount = 1 + (int) (Math.log(count) / Math.log(2));
				direct.setNestedElement(rune, element, elemcount);
			}
			if (alteration != null) {
				direct.setNestedAlteration(rune, alteration);
			}
			
			return rune;
		} else if (element != null) {
			int elemcount = 1 + (int) (Math.log(count) / Math.log(2)); // TODO REMOVE
			int unused; // remove
			
			ItemStack rune = SpellRune.getRune(element, elemcount);
			return rune;
		} else {
			ItemStack rune = SpellRune.getRune(alteration);
			return rune;
		}
	}

//	@Override
//	public ItemStack getRecipeOutput() {
//		return SpellRune.getRune(EMagicElement.FIRE, 1);
//	}

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
