package com.smanzana.nostrummagica.items;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.renderer.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.crafting.IRecipeSerializer;
import net.minecraft.item.crafting.SpecialRecipe;
import net.minecraft.item.crafting.SpecialRecipeSerializer;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.Constants.NBT;

public class SpellRune extends Item implements ILoreTagged {
	
	@OnlyIn(Dist.CLIENT)
	public static class ModelMesher implements ItemMeshDefinition {

		@Override
		public ModelResourceLocation getModelLocation(ItemStack stack) {
			// Must be kept in sync with ClientProxy variant registration
			/*
			 * for (EMagicElement type : EMagicElement.values()) {
    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name()));
	    	}
	    	for (EAlteration type : EAlteration.values()) {
	    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name()));
	    	}
	    	for (SpellShape type : SpellShape.getAllShapes()) {
	    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getShapeKey()));
	    	}
	    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
	    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getTriggerKey()));
	    	}
			 */
			String suffix = "blank";
			
			if (!stack.isEmpty()) {
				suffix = getPieceName(stack);
				if (suffix == null || suffix.trim().isEmpty())
					suffix = "blank";
				else
					suffix = suffix.toLowerCase();
			}
			
			return new ModelResourceLocation(
					new ResourceLocation(NostrumMagica.MODID, "rune_" + suffix),
					"inventory");
		}

	}
	
	public static class RuneRecipe extends SpecialRecipe {
		
		protected static final SpecialRecipeSerializer<RuneRecipe> Serializer = IRecipeSerializer.register("crafting_special_nostrum_rune", new SpecialRecipeSerializer<>(RuneRecipe::new));
		
		public RuneRecipe() {
			this(new ResourceLocation(NostrumMagica.MODID, "nostrum.recipe.rune"));
		}
		
		public RuneRecipe(ResourceLocation idIn) {
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
					
					if (element != null || alteration != null)
						foundTwo = true;
					
					EMagicElement shapeElem = SpellRune.getPieceShapeElement(stack);
					if (element != null && shapeElem != null && shapeElem != element) {
						return false; // multiple elements
					}
					if (shapeElem != null && element == null)
						element = shapeElem;
					EAlteration alt = SpellRune.getPieceShapeAlteration(stack);
					if (alt != null && alteration != null) {
						// Can't have two alterations
						return false;
					}
					if (alt != null && alteration == null)
						alteration = alt;
					int shapeCount = reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (count + shapeCount > 4)
						return false;
					count += shapeCount;
				} else if (SpellRune.isElement(stack)) {
					EMagicElement elem = SpellRune.getElement(stack);
					if (elem == null)
						return false; // CORRUPT
					if (element != null && elem != element) 
						return false; // Different element types
					
					if (shape || alteration != null || element != null)
						foundTwo = true;
					
					element = elem;
					
					int c = reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (c + count > 4)
						return false;
					count += c;
				} else if (SpellRune.isAlteration(stack)) {
					EAlteration alt = SpellRune.getAlteration(stack);
					if (alt == null)
						return false; // CORRUPT
					if (alteration != null)
						return false;
					alteration = alt;
					
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
					shape = SpellShape.get(SpellRune.getPieceName(stack));
					if (shape == null)
						return ItemStack.EMPTY;
					
					params = SpellRune.getPieceParam(stack);
					
					EMagicElement shapeElem = SpellRune.getPieceShapeElement(stack);
					if (element != null && shapeElem != null && shapeElem != element) {
						return ItemStack.EMPTY; // multiple elements
					}
					if (shapeElem != null)
						element = shapeElem;
					EAlteration alt = SpellRune.getPieceShapeAlteration(stack);
					if (alt != null && alteration != null) {
						// Can't have two alterations
						return ItemStack.EMPTY;
					}
					if (alt != null && alteration == null)
						alteration = alt;
					
					int shapeCount = reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (count + shapeCount > 4)
						return ItemStack.EMPTY;
					count += shapeCount;
				} else if (SpellRune.isElement(stack)) {
					EMagicElement elem = SpellRune.getElement(stack);
					if (elem == null)
						return ItemStack.EMPTY; // CORRUPT
					if (element != null && elem != element) 
						return ItemStack.EMPTY; // Different element types
					
					element = elem;
					int c = reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (c + count > 4)
						return ItemStack.EMPTY;
					count += c;
				} else if (SpellRune.isAlteration(stack)) {
					EAlteration alt = SpellRune.getAlteration(stack);
					if (alt == null)
						return ItemStack.EMPTY; // CORRUPT
					if (alteration != null)
						return ItemStack.EMPTY;
					alteration = alt;
				}
			}
			
			if (shape != null) {
				ItemStack rune = SpellRune.getRune(shape);
				SpellRune.setPieceParam(rune, params);
				if (element != null)
					SpellRune.setPieceShapeElement(rune, element);
				if (alteration != null)
					SpellRune.setPieceShapeAlteration(rune, alteration);
				
				// 1 => 1
				// 2 => 2
				// 3 => 2
				// 4 => 3
				// log2(count) + 1
				// log2(count) = log(count) / log(2)
				int elemcount = 1 + (int) (Math.log(count) / Math.log(2));
				
				SpellRune.setPieceElementCount(rune, elemcount);
				return rune;
			} else if (element != null) {
				int elemcount = 1 + (int) (Math.log(count) / Math.log(2));
				
				ItemStack rune = SpellRune.getRune(element, elemcount);
				return rune;
			} else {
				ItemStack rune = SpellRune.getRune(alteration);
				return rune;
			}
		}

//		@Override
//		public ItemStack getRecipeOutput() {
//			return SpellRune.getRune(EMagicElement.FIRE, 1);
//		}

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
			return Serializer;
		}
		
	}
	
	public static final String ID = "nostrum_rune";
	private static final String NBT_TYPE = "type"; // all
	private static final String NBT_NAME = "name"; // all
	private static final String NBT_PARAM_VAL = "param_value"; // shapes/triggers
	private static final String NBT_PARAM_FLIP = "param_flip"; // shapes/triggers
	private static final String NBT_SHAPE_ALTERATION = "shape_alteration"; // shapes
	private static final String NBT_SHAPE_ELEMENT = "shape_element"; // shapes
	private static final String NBT_ELEMENT_COUNT = "e_count"; // shapes
	
	public SpellRune() {
		super(NostrumItems.PropUnstackable());
	}
	
	// TODO decide about this
	@Override
	public String getTranslationKey(ItemStack stack) {
		return this.getDefaultTranslationKey() + "." + getPieceName(stack).toLowerCase();
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (isElement(stack)) {
			tooltip.add(new StringTextComponent("Element").applyTextStyle(TextFormatting.DARK_GRAY));
			int count = getPieceElementCount(stack);
			if (count != 0)
				tooltip.add(new StringTextComponent("Power " + count).applyTextStyle(TextFormatting.DARK_GREEN));
		} else if (isAlteration(stack)) {
			tooltip.add(new StringTextComponent("Alteration").applyTextStyle(TextFormatting.AQUA));
		} else if (isTrigger(stack)) {
			tooltip.add(new StringTextComponent("Trigger").applyTextStyle(TextFormatting.DARK_BLUE));
			
			SpellPartParam params = getPieceParam(stack);
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
			
			if (comp.getTrigger().supportsBoolean() && params.flip) {
				tooltip.add(new StringTextComponent(comp.getTrigger().supportedBooleanName() + ": On"));
			}
			if (comp.getTrigger().supportedFloats() != null) {
				float[] vals = comp.getTrigger().supportedFloats();
				if (params.level != 0f && params.level != vals[0])
					tooltip.add(new StringTextComponent(comp.getTrigger().getDisplayName() + ": " + params.level));
			}
			
		} else {
			tooltip.add(new StringTextComponent("Shape").applyTextStyle(TextFormatting.DARK_RED));
			EMagicElement elem = getPieceShapeElement(stack);
			if (elem != null) {
				tooltip.add(new StringTextComponent(elem.getName()).applyTextStyle(TextFormatting.DARK_GRAY));
			}
			int count = getPieceElementCount(stack);
			if (count != 0) {
				tooltip.add(new StringTextComponent("Power ").applyTextStyle(TextFormatting.DARK_GREEN));
			}
			EAlteration alteration = getPieceShapeAlteration(stack);
			if (alteration != null) {
				tooltip.add(new StringTextComponent(alteration.getName()).applyTextStyle(TextFormatting.AQUA));
			}
			
			SpellPartParam params = getPieceParam(stack);
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
			if (comp.getShape().supportsBoolean() && params.flip) {
				tooltip.add(new StringTextComponent(comp.getShape().supportedBooleanName() + ": On"));
			}
			if (comp.getShape().supportedFloats() != null) {
				float[] vals = comp.getShape().supportedFloats();
				if (params.level != 0f && params.level != vals[0])
					tooltip.add(new StringTextComponent(comp.getShape().getDisplayName() + ": " + params.level));
			}
		}
		
	}
	
//	/**
//     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
//     */
//    @OnlyIn(Dist.CLIENT)
//    @Override
//	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> subItems) {
//    	if (this.isInCreativeTab(tab)) {
//	    	// Should be synced to client proxy registering variants
//	    	for (EMagicElement type : EMagicElement.values()) {
//	    		subItems.add(getRune(type, 1));
//	    	}
//	    	for (EAlteration type : EAlteration.values()) {
//	    		subItems.add(getRune(type));
//	    	}
//	    	for (SpellShape type : SpellShape.getAllShapes()) {
//	    		subItems.add(getRune(type));
//	    	}
//	    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
//	    		subItems.add(getRune(type));
//	    	}
//    	}
//	}
    
    public static ItemStack getRune(EMagicElement element, int power) {
    	return getRune(element, power, 1);
    }
    
    public static ItemStack getRune(EMagicElement element, int power, int count) {
    	ItemStack stack = new ItemStack(NostrumItems.spellRune, count);
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putString(NBT_TYPE, "element");
    	nbt.putString(NBT_NAME, element.name());
    	nbt.putInt(NBT_ELEMENT_COUNT, power);
    	
    	stack.setTag(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(EAlteration alteration) {
    	return getRune(alteration, 1);
    }
    
    public static ItemStack getRune(EAlteration alteration, int count) {
    	ItemStack stack = new ItemStack(NostrumItems.spellRune, count);
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putString(NBT_TYPE, "alteration");
    	nbt.putString(NBT_NAME, alteration.name());
    	
    	stack.setTag(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(SpellShape shape) {
    	return getRune(shape, 1);
    }
    
    public static ItemStack getRune(SpellShape shape, int count) {
    	ItemStack stack = new ItemStack(NostrumItems.spellRune, count);
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putString(NBT_TYPE, "shape");
    	nbt.putString(NBT_NAME, shape.getShapeKey());
    	
    	stack.setTag(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(SpellTrigger trigger) {
    	return getRune(trigger, 1);
    }
    
    public static ItemStack getRune(SpellTrigger trigger, int count) {
    	ItemStack stack = new ItemStack(NostrumItems.spellRune, count);
    	CompoundNBT nbt = new CompoundNBT();
    	
    	nbt.putString(NBT_TYPE, "trigger");
    	nbt.putString(NBT_NAME, trigger.getTriggerKey());
    	
    	stack.setTag(nbt);
    	return stack;
    }
    
    /**
     * Get [count] runes that matches the spell part handed in.
     * Produces [count] copies of the part in a single rune. For example,
     * if count is 5 and part is a single fire II rune, it'll make one itemstack
     * with count 5 that is a single run with two fire runes embedded in it.
     * @param part
     * @param count
     * @return
     */
    public static ItemStack getRune(SpellPart part, int count) {
    	ItemStack stack;
    	if (part.isTrigger())
    		stack = getRune(part.getTrigger());
    	else
    		stack = getRune(part.getShape());
    	
    	CompoundNBT nbt = stack.getTag();
    	
    	if (part.getParam() != null) {
    		nbt.putFloat(NBT_PARAM_VAL, part.getParam().level);
    		nbt.putBoolean(NBT_PARAM_FLIP, part.getParam().flip);
    	}
    	
    	if (!part.isTrigger()) {
    		nbt.putString(NBT_SHAPE_ELEMENT, part.getElement().name());
    		nbt.putInt(NBT_ELEMENT_COUNT, part.getElementCount());
    		if (part.getAlteration() != null) {
    			nbt.putString(NBT_SHAPE_ALTERATION, part.getAlteration().name());
    		}
    	}
    	
    	return stack;
    }
    
    /**
     * Takes a rune and breaks it into all the runes that would be required to make it.
     * So a single rune with fire II would produce a single rune an two fire runes.
     * Each rune keeps its modifications.
     * @param rune
     * @param output
     * @return
     */
    public static NonNullList<ItemStack> decomposeRune(ItemStack rune, @Nullable NonNullList<ItemStack> output) {
    	if (output == null) {
    		output = NonNullList.from(ItemStack.EMPTY);
    	}
    	
    	if (isTrigger(rune)
    			|| isElement(rune)
    			|| isAlteration(rune)) {
    		output.add(rune); // Nothing to do
    	} else {
    		// Is shape. Pop out and elements or alterations
    		@Nullable EAlteration alt = getPieceShapeAlteration(rune);
    		if (alt != null) {
    			output.add(getRune(alt, 1));
    			setPieceShapeAlteration(rune, null);
    		}
    		@Nullable EMagicElement elem = getPieceShapeElement(rune);
    		if (elem != null) {
    			int elemCount = getPieceElementCount(rune);
    			output.add(getRune(elem, elemCount));
    			setPieceShapeElement(rune, null);
    			setPieceElementCount(rune, 0);
    		}
    		output.add(rune); // Elem and alt are stripped away at this point if they were present
    	}
    	
    	return output;
    }
    
    private static String getPieceName(ItemStack piece) {
    	if (!piece.hasTag())
    		return "";
    	
    	return piece.getTag().getString(NBT_NAME);
    }
    
    private static String getPieceType(ItemStack piece) {
    	if (!piece.hasTag())
    		return "";
    	
    	return piece.getTag().getString(NBT_TYPE);
    }
    
    private static EMagicElement getPieceShapeElement(ItemStack piece) {
    	if (!piece.hasTag())
    		return null;
    	
    	if (!piece.getTag().contains(NBT_SHAPE_ELEMENT, NBT.TAG_STRING))
    		return null;
    	
    	try {
    		return EMagicElement.valueOf(piece.getTag().getString(NBT_SHAPE_ELEMENT));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get element from rune");
    		return EMagicElement.PHYSICAL;
    	}
    }
    
    private static void setPieceShapeElement(ItemStack piece, @Nullable EMagicElement element) {
    	if (!piece.hasTag())
    		return;
    	
    	if (element != null) {
    		piece.getTag().putString(NBT_SHAPE_ELEMENT, element.name());
    	} else {
    		piece.getTag().remove(NBT_SHAPE_ELEMENT);
    	}
    }
    
    private static EAlteration getPieceShapeAlteration(ItemStack piece) {
    	if (!piece.hasTag())
    		return null;
    	
    	if (!piece.getTag().contains(NBT_SHAPE_ALTERATION, NBT.TAG_STRING))
    		return null;
    	
    	try {
    		return EAlteration.valueOf(piece.getTag().getString(NBT_SHAPE_ALTERATION));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get alteration from rune");
    		return null;
    	}
    }
    
    private static void setPieceShapeAlteration(ItemStack piece, @Nullable EAlteration alteration) {
    	if (!piece.hasTag())
    		return;
    	
    	if (alteration != null) {
    		piece.getTag().putString(NBT_SHAPE_ALTERATION, alteration.name());
    	} else {
    		piece.getTag().remove(NBT_SHAPE_ALTERATION);
    	}
    }
    
    private static int getPieceElementCount(ItemStack piece) {
    	if (!piece.hasTag())
    		return 0;
    	
    	return piece.getTag().getInt(NBT_ELEMENT_COUNT);
    }
    
    private static void setPieceElementCount(ItemStack piece, int count) {
    	if (!piece.hasTag())
    		return;
    	
    	if (count > 0) {
    		piece.getTag().putInt(NBT_ELEMENT_COUNT, count);
    	} else {
    		piece.getTag().remove(NBT_ELEMENT_COUNT);
    	}
    }
    
    public static SpellPartParam getPieceParam(ItemStack piece) {
    	if (!piece.hasTag())
    		return new SpellPartParam(0f, false);
    	
    	float level = piece.getTag().getFloat(NBT_PARAM_VAL);
    	boolean flip = piece.getTag().getBoolean(NBT_PARAM_FLIP);
    	return new SpellPartParam(level, flip);
    }
    
    public static void setPieceParam(ItemStack piece, SpellPartParam params) {
    	if (!piece.hasTag())
    		return;
    	
    	piece.getTag().putFloat(NBT_PARAM_VAL, params.level);
    	piece.getTag().putBoolean(NBT_PARAM_FLIP, params.flip);
    }
    
    @Override
    public ActionResultType onItemUse(ItemUseContext context) {
    	// Probably wnat this later
    	return super.onItemUse(context);
	}
    
    public static boolean isShape(ItemStack stack) {
    	return getPieceType(stack).equalsIgnoreCase("shape");
    }
    
	public static boolean isTrigger(ItemStack stack) {
    	return getPieceType(stack).equalsIgnoreCase("trigger");
    }
    
	public static boolean isAlteration(ItemStack stack) {
    	return getPieceType(stack).equalsIgnoreCase("alteration");
    }
    
	public static boolean isElement(ItemStack stack) {
    	return getPieceType(stack).equalsIgnoreCase("element");
    }
    
	public static boolean isSpellWorthy(ItemStack stack) {
		if (isTrigger(stack))
			return true;
		if (isAlteration(stack) || isElement(stack))
			return false;
		
		// For shapes, we ALSO need an element
		if (null == getPieceShapeElement(stack))
			return false;
		
		return true;
	}
	
	/**
	 * Works with trigger or shape runes.
	 * Pulls out the data and makes a spell part, complete with
	 * spell part params.
	 * @param stack
	 * @return null on error
	 */
	public static SpellPart getPart(ItemStack stack) {
		SpellPart part;
		if (isTrigger(stack)) {
			SpellTrigger trigger = SpellTrigger.get(getPieceName(stack));
			if (trigger == null)
				return null;
			
			part = new SpellPart(trigger);
		} else {
			SpellShape shape = SpellShape.get(getPieceName(stack));
			if (shape == null)
				return null;
			
			EMagicElement element = getPieceShapeElement(stack);
			//if (element == null)
			//	return null;
			
			int count = getPieceElementCount(stack);
			if (count < 1)
				count = 1;
			
			EAlteration alteration = getPieceShapeAlteration(stack);
			
			part = new SpellPart(shape, element, count, alteration);
		}
		
		part.setParam(getPieceParam(stack));
		
		return part;
	}
	
	/**
	 * Returns the element this rune holds. This is NOT the element
	 * nested within a shape.
	 * @param stack
	 * @return
	 */
	public static EMagicElement getElement(ItemStack stack) {
		if (!isElement(stack))
			return null;
		
		String name = getPieceName(stack);
		
		if (name == null || name.trim().isEmpty())
			return null;
		
		try {
    		return EMagicElement.valueOf(name);
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get element from rune");
    		return EMagicElement.PHYSICAL;
    	}
	}
	
	public static EAlteration getAlteration(ItemStack stack) {
		if (!isAlteration(stack))
			return null;
		
		String name = getPieceName(stack);
		
		if (name == null || name.trim().isEmpty())
			return null;
		
		try {
    		return EAlteration.valueOf(name);
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get alteration from rune");
    		return null;
    	}
	}
	
	@Override
	public String getLoreKey() {
		return "nostrum_spell_rune";
	}

	@Override
	public String getLoreDisplayName() {
		return "Spell Runes";
	}

	@Override
	public Lore getBasicLore() {
		return new Lore().add("Runes are used to make new spells.", "There are four types of runes: Triggers, Shapes, Elements, and Alterations.", "Triggers define the stages of the spell and when to advance.", "Shapes determine who is affected.", "Elements are added to shapes to give the effect an element.", "Alterations morph the effect of the spell.", "Every element and alteration combination is different.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Runes are used to make new spells.", "There are four types of runes: Triggers, Shapes, Elements, and Alterations.", "Triggers define the stages of the spell and when to advance.", "Shapes determine who is affected.", "Elements are added to shapes to give the effect an element.", "Alterations morph the effect of the spell.", "Every element and alteration combination is different.", "After gaining mastery of an element, trigger, shape, or alteration, you can create runes any time you want.");
	}
	
	public static SpellComponentWrapper toComponentWrapper(ItemStack rune) {
		if (rune.isEmpty() || !(rune.getItem() instanceof SpellRune))
			return null;
		
		if (isElement(rune)) {
			return new SpellComponentWrapper(getElement(rune));
		}
		if (isAlteration(rune)) {
			return new SpellComponentWrapper(getAlteration(rune));
		}
		
		SpellPart part = getPart(rune);
		if (part.isTrigger())
			return new SpellComponentWrapper(part.getTrigger());
		else
			return new SpellComponentWrapper(part.getShape());
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
	
}
