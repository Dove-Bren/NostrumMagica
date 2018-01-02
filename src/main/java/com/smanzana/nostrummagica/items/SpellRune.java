package com.smanzana.nostrummagica.items;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.client.renderer.ItemMeshDefinition;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellRune extends Item {
	
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
			}
			
			return new ModelResourceLocation(
					new ResourceLocation(NostrumMagica.MODID, "rune_" + suffix),
					"inventory");
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
		// TODO Auto-generated method stub
		
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
		return this.getUnlocalizedName() + "." + getPieceName(stack);
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
    
    private static EAlteration getPieceShapeAlteration(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return null;
    	
    	if (!piece.getTagCompound().hasKey(NBT_SHAPE_ELEMENT, NBT.TAG_STRING))
    		return null;
    	
    	try {
    		return EAlteration.valueOf(piece.getTagCompound().getString(NBT_SHAPE_ALTERATION));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get alteration from rune");
    		return null;
    	}
    }
    
    private static int getPieceElementCount(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return 0;
    	
    	return piece.getTagCompound().getInteger(NBT_ELEMENT_COUNT);
    }
    
    private static SpellPartParam getPieceParam(ItemStack piece) {
    	if (!piece.hasTagCompound())
    		return new SpellPartParam(0f, false);
    	
    	float level = piece.getTagCompound().getFloat(NBT_PARAM_VAL);
    	boolean flip = piece.getTagCompound().getBoolean(NBT_PARAM_FLIP);
    	return new SpellPartParam(level, flip);
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
		if (isTrigger(stack) || isAlteration(stack) || isElement(stack))
			return true;
		
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
			if (element == null)
				return null;
			
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
		
		try {
    		return EMagicElement.valueOf(getPieceName(stack));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get element from rune");
    		return EMagicElement.PHYSICAL;
    	}
	}
	
	public static EAlteration getAlteration(ItemStack stack) {
		if (!isAlteration(stack))
			return null;
		
		try {
    		return EAlteration.valueOf(getPieceName(stack));
    	} catch (IllegalArgumentException e) {
    		NostrumMagica.logger.error("Failed to get alteration from rune");
    		return null;
    	}
	}
	
}
