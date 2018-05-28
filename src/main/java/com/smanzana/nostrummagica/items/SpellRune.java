package com.smanzana.nostrummagica.items;

import java.util.List;

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

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellRune extends Item implements ILoreTagged {
	
	@SideOnly(Side.CLIENT)
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
			
			if (stack != null) {
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
	
	private static class RuneRecipe implements IRecipe {

		@Override
		public boolean matches(InventoryCrafting inv, World worldIn) {
			boolean foundTwo = false; // Found at least two runes
			boolean shape = false;
			EMagicElement element = null;
			EAlteration alteration = null;
			int count = 0;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack == null)
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
		public ItemStack getCraftingResult(InventoryCrafting inv) {
			SpellShape shape = null;
			EMagicElement element = null;
			EAlteration alteration = null;
			SpellPartParam params = null;
			int count = 0;
			for (int i = 0; i < inv.getSizeInventory(); i++) {
				ItemStack stack = inv.getStackInSlot(i);
				if (stack == null)
					continue;
				
				if (!(stack.getItem() instanceof SpellRune))
					return null;
				
				if (SpellRune.isTrigger(stack))
					return null;
				
				if (SpellRune.isShape(stack)) {
					if (shape != null) {
						// We already found a shape
						return null;
					}
					shape = SpellShape.get(SpellRune.getPieceName(stack));
					if (shape == null)
						return null;
					
					params = SpellRune.getPieceParam(stack);
					
					EMagicElement shapeElem = SpellRune.getPieceShapeElement(stack);
					if (element != null && shapeElem != null && shapeElem != element) {
						return null; // multiple elements
					}
					if (shapeElem != null)
						element = shapeElem;
					EAlteration alt = SpellRune.getPieceShapeAlteration(stack);
					if (alt != null && alteration != null) {
						// Can't have two alterations
						return null;
					}
					if (alt != null && alteration == null)
						alteration = alt;
					
					int shapeCount = reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (count + shapeCount > 4)
						return null;
					count += shapeCount;
				} else if (SpellRune.isElement(stack)) {
					EMagicElement elem = SpellRune.getElement(stack);
					if (elem == null)
						return null; // CORRUPT
					if (element != null && elem != element) 
						return null; // Different element types
					
					element = elem;
					int c = reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (c + count > 4)
						return null;
					count += c;
				} else if (SpellRune.isAlteration(stack)) {
					EAlteration alt = SpellRune.getAlteration(stack);
					if (alt == null)
						return null; // CORRUPT
					if (alteration != null)
						return null;
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

		@Override
		public int getRecipeSize() {
			return 4;
		}

		@Override
		public ItemStack getRecipeOutput() {
			return SpellRune.getRune(EMagicElement.FIRE, 1);
		}

		@Override
		public ItemStack[] getRemainingItems(InventoryCrafting inv) {
			return new ItemStack[inv.getSizeInventory()];
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
	
	private static SpellRune instance = null;
	public static SpellRune instance() {
		if (instance == null)
			instance = new SpellRune();
		
		return instance;
	}

	public static void init() {
		GameRegistry.addRecipe(new RuneRecipe());
	}
	
	public SpellRune() {
		super();
		this.setUnlocalizedName(ID);
		this.setMaxDamage(0);
		this.setMaxStackSize(1);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setHasSubtypes(true);
	}
	
	@Override
	public String getUnlocalizedName(ItemStack stack) {
		return this.getUnlocalizedName() + "." + getPieceName(stack).toLowerCase();
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {
		if (isElement(stack)) {
			tooltip.add(TextFormatting.DARK_GRAY + "Element" + TextFormatting.RESET);
			int count = getPieceElementCount(stack);
			if (count != 0)
				tooltip.add(TextFormatting.DARK_GREEN + "Power " + count + TextFormatting.RESET);
		} else if (isAlteration(stack)) {
			tooltip.add(TextFormatting.AQUA + "Alteration" + TextFormatting.RESET);
		} else if (isTrigger(stack)) {
			tooltip.add(TextFormatting.DARK_BLUE + "Trigger" + TextFormatting.RESET);
			
			SpellPartParam params = getPieceParam(stack);
			SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
			if (comp.getTrigger().supportsBoolean() && params.flip) {
				tooltip.add("Flip: On");
			}
			if (comp.getTrigger().supportedFloats() != null) {
				float[] vals = comp.getTrigger().supportedFloats();
				if (params.level != vals[0])
					tooltip.add("Level: " + params.level);
			}
			
		} else {
			tooltip.add(TextFormatting.DARK_RED + "Shape" + TextFormatting.RESET);
			EMagicElement elem = getPieceShapeElement(stack);
			if (elem != null)
				tooltip.add(TextFormatting.DARK_GRAY + elem.getName() + TextFormatting.RESET);
			int count = getPieceElementCount(stack);
			if (count != 0)
				tooltip.add(TextFormatting.DARK_GREEN + "Power " + count + TextFormatting.RESET);
			EAlteration alteration = getPieceShapeAlteration(stack);
			if (alteration != null)
				tooltip.add(TextFormatting.AQUA + alteration.getName() + TextFormatting.RESET);
		}
		
	}
	
	/**
     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
     */
    @SideOnly(Side.CLIENT)
    @Override
	public void getSubItems(Item itemIn, CreativeTabs tab, List<ItemStack> subItems) {
    	// Should be synced to client proxy registering variants
    	for (EMagicElement type : EMagicElement.values()) {
    		subItems.add(getRune(type, 1));
    	}
    	for (EAlteration type : EAlteration.values()) {
    		subItems.add(getRune(type));
    	}
    	for (SpellShape type : SpellShape.getAllShapes()) {
    		subItems.add(getRune(type));
    	}
    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
    		subItems.add(getRune(type));
    	}
	}
    
    public static ItemStack getRune(EMagicElement element, int power) {
    	return getRune(element, power, 1);
    }
    
    public static ItemStack getRune(EMagicElement element, int power, int count) {
    	ItemStack stack = new ItemStack(instance, count);
    	NBTTagCompound nbt = new NBTTagCompound();
    	
    	nbt.setString(NBT_TYPE, "element");
    	nbt.setString(NBT_NAME, element.name());
    	nbt.setInteger(NBT_ELEMENT_COUNT, power);
    	
    	stack.setTagCompound(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(EAlteration alteration) {
    	return getRune(alteration, 1);
    }
    
    public static ItemStack getRune(EAlteration alteration, int count) {
    	ItemStack stack = new ItemStack(instance, count);
    	NBTTagCompound nbt = new NBTTagCompound();
    	
    	nbt.setString(NBT_TYPE, "alteration");
    	nbt.setString(NBT_NAME, alteration.name());
    	
    	stack.setTagCompound(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(SpellShape shape) {
    	return getRune(shape, 1);
    }
    
    public static ItemStack getRune(SpellShape shape, int count) {
    	ItemStack stack = new ItemStack(instance, count);
    	NBTTagCompound nbt = new NBTTagCompound();
    	
    	nbt.setString(NBT_TYPE, "shape");
    	nbt.setString(NBT_NAME, shape.getShapeKey());
    	
    	stack.setTagCompound(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(SpellTrigger trigger) {
    	return getRune(trigger, 1);
    }
    
    public static ItemStack getRune(SpellTrigger trigger, int count) {
    	ItemStack stack = new ItemStack(instance, count);
    	NBTTagCompound nbt = new NBTTagCompound();
    	
    	nbt.setString(NBT_TYPE, "trigger");
    	nbt.setString(NBT_NAME, trigger.getTriggerKey());
    	
    	stack.setTagCompound(nbt);
    	return stack;
    }
    
    public static ItemStack getRune(SpellPart part, int count) {
    	ItemStack stack;
    	if (part.isTrigger())
    		stack = getRune(part.getTrigger());
    	else
    		stack = getRune(part.getShape());
    	
    	NBTTagCompound nbt = stack.getTagCompound();
    	
    	if (part.getParam() != null) {
    		nbt.setFloat(NBT_PARAM_VAL, part.getParam().level);
    		nbt.setBoolean(NBT_PARAM_FLIP, part.getParam().flip);
    	}
    	
    	if (!part.isTrigger()) {
    		nbt.setString(NBT_SHAPE_ELEMENT, part.getElement().name());
    		nbt.setInteger(NBT_ELEMENT_COUNT, part.getElementCount());
    	}
    	
    	return stack;
    }
    
    private static String getPieceName(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return "";
    	
    	return piece.getTagCompound().getString(NBT_NAME);
    }
    
    private static String getPieceType(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return "";
    	
    	return piece.getTagCompound().getString(NBT_TYPE);
    }
    
    private static EMagicElement getPieceShapeElement(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return null;
    	
    	if (!piece.getTagCompound().hasKey(NBT_SHAPE_ELEMENT, NBT.TAG_STRING))
    		return null;
    	
    	try {
    		return EMagicElement.valueOf(piece.getTagCompound().getString(NBT_SHAPE_ELEMENT));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get element from rune");
    		return EMagicElement.PHYSICAL;
    	}
    }
    
    private static void setPieceShapeElement(ItemStack piece, EMagicElement element) {
    	if (!piece.hasTagCompound())
    		return;
    	
    	piece.getTagCompound().setString(NBT_SHAPE_ELEMENT, element.name());
    }
    
    private static EAlteration getPieceShapeAlteration(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return null;
    	
    	if (!piece.getTagCompound().hasKey(NBT_SHAPE_ALTERATION, NBT.TAG_STRING))
    		return null;
    	
    	try {
    		return EAlteration.valueOf(piece.getTagCompound().getString(NBT_SHAPE_ALTERATION));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get alteration from rune");
    		return null;
    	}
    }
    
    private static void setPieceShapeAlteration(ItemStack piece, EAlteration alteration) {
    	if (!piece.hasTagCompound())
    		return;
    	
    	piece.getTagCompound().setString(NBT_SHAPE_ALTERATION, alteration.name());
    }
    
    private static int getPieceElementCount(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return 0;
    	
    	return piece.getTagCompound().getInteger(NBT_ELEMENT_COUNT);
    }
    
    private static void setPieceElementCount(ItemStack piece, int count) {
    	if (!piece.hasTagCompound())
    		return;
    	
    	piece.getTagCompound().setInteger(NBT_ELEMENT_COUNT, count);
    }
    
    public static SpellPartParam getPieceParam(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return new SpellPartParam(0f, false);
    	
    	float level = piece.getTagCompound().getFloat(NBT_PARAM_VAL);
    	boolean flip = piece.getTagCompound().getBoolean(NBT_PARAM_FLIP);
    	return new SpellPartParam(level, flip);
    }
    
    public static void setPieceParam(ItemStack piece, SpellPartParam params) {
    	if (!piece.hasTagCompound())
    		return;
    	
    	piece.getTagCompound().setFloat(NBT_PARAM_VAL, params.level);
    	piece.getTagCompound().setBoolean(NBT_PARAM_FLIP, params.flip);
    }
    
    public EnumActionResult onItemUse(ItemStack stack, EntityPlayer playerIn, World worldIn, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ)
    {
    	// Probably wnat this later
    	return super.onItemUse(stack, playerIn, worldIn, pos, hand, facing, hitX, hitY, hitZ);
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
		if (rune == null || !(rune.getItem() instanceof SpellRune))
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
