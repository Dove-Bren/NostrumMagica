package com.smanzana.nostrummagica.items;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

public abstract class SpellRune extends Item implements ILoreTagged {
	
	protected static final Map<SpellComponentWrapper, SpellRune> TypeMap = new HashMap<>();
	
	public static final void SetRuneForType(SpellComponentWrapper type, SpellRune rune) {
		TypeMap.put(type, rune);
	}
	
	public static final @Nullable SpellRune GetRuneForType(SpellComponentWrapper type) {
		return TypeMap.get(type);
	}
	
//	@OnlyIn(Dist.CLIENT)
//	public static class ModelMesher implements ItemMeshDefinition {
//
//		@Override
//		public ModelResourceLocation getModelLocation(ItemStack stack) {
//			// Must be kept in sync with ClientProxy variant registration
//			/*
//			 * for (EMagicElement type : EMagicElement.values()) {
//    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name()));
//	    	}
//	    	for (EAlteration type : EAlteration.values()) {
//	    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.name()));
//	    	}
//	    	for (SpellShape type : SpellShape.getAllShapes()) {
//	    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getShapeKey()));
//	    	}
//	    	for (SpellTrigger type : SpellTrigger.getAllTriggers()) {
//	    		list.add(new ResourceLocation(NostrumMagica.MODID, "rune_" + type.getTriggerKey()));
//	    	}
//			 */
//			String suffix = "blank";
//			
//			if (!stack.isEmpty()) {
//				suffix = getPieceName(stack);
//				if (suffix == null || suffix.trim().isEmpty())
//					suffix = "blank";
//				else
//					suffix = suffix.toLowerCase();
//			}
//			
//			return new ModelResourceLocation(
//					new ResourceLocation(NostrumMagica.MODID, "rune_" + suffix),
//					"inventory");
//		}
//
//	}
	
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
					if (elementRune.element == null)
						return false; // CORRUPT
					if (element != null && elementRune.element != element) 
						return false; // Different element types
					
					if (shape || alteration != null || element != null)
						foundTwo = true;
					
					element = elementRune.element;
					
					int c = 1;//reverseElementCount(getPieceElementCount(stack)); TODO remove trying to combine elements
					if (c + count > 4)
						return false;
					count += c;
				} else if (SpellRune.isAlteration(stack)) {
					AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
					if (altRune.alteration == null)
						return false; // CORRUPT
					if (alteration != null)
						return false;
					alteration = altRune.alteration;
					
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
					if (elementRune.element == null)
						return ItemStack.EMPTY; // CORRUPT
					if (element != null && elementRune.element != element) 
						return ItemStack.EMPTY; // Different element types
					
					element = elementRune.element;
					int c = 1;//reverseElementCount(SpellRune.getPieceElementCount(stack));
					if (c + count > 4)
						return ItemStack.EMPTY;
					count += c;
				} else if (SpellRune.isAlteration(stack)) {
					AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
					if (altRune.alteration == null)
						return ItemStack.EMPTY; // CORRUPT
					if (alteration != null)
						return ItemStack.EMPTY;
					alteration = altRune.alteration;
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
	
	protected static final String ID_PREFIX = "nostrum_rune";
	private static final String NBT_PARAM_VAL = "param_value"; // shapes/triggers
	private static final String NBT_PARAM_FLIP = "param_flip"; // shapes/triggers
	
	protected SpellRune() {
		super(NostrumItems.PropUnstackable());
	}
	
	public abstract SpellComponentWrapper getComponent();
	
	public abstract String makeRegistryName();
	
//	// TODO decide about this
//	@Override
//	public String getTranslationKey(ItemStack stack) {
//		return this.getDefaultTranslationKey() + "." + getPieceName(stack).toLowerCase();
//	}
	
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
	
	public static ItemStack getRune(SpellComponentWrapper wrapper) {
		Item rune = GetRuneForType(wrapper);
		return new ItemStack(rune);
	}
	
	public static ItemStack getRune(EMagicElement element, int unused) {
		return getRune(new SpellComponentWrapper(element));
	}
	
	public static ItemStack getRune(EAlteration alteration) {
		return getRune(new SpellComponentWrapper(alteration));
	}
	
	public static ItemStack getRune(SpellShape shape) {
		return getRune(new SpellComponentWrapper(shape));
	}
	
	public static ItemStack getRune(SpellTrigger trigger) {
		return getRune(new SpellComponentWrapper(trigger));
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
	public static ItemStack getRune(SpellPart part) {
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
			ShapeSpellRune shapeRune = (ShapeSpellRune) stack.getItem();
			shapeRune.setNestedElement(stack, part.getElement(), part.getElementCount());
			shapeRune.setNestedAlteration(stack, part.getAlteration());
		}
		
		return stack;
	}
	
	protected abstract NonNullList<ItemStack> decompose(ItemStack rune, @Nonnull NonNullList<ItemStack> output);
	
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
		
		if (!rune.isEmpty() && rune.getItem() instanceof SpellRune) {
			((SpellRune) rune.getItem()).decompose(rune, output);
		}
		
		return output;
	}
	
//	private static EMagicElement getPieceShapeElement(ItemStack piece) {
//		if (!piece.hasTag())
//			return null;
//		
//		if (!piece.getTag().contains(NBT_SHAPE_ELEMENT, NBT.TAG_STRING))
//			return null;
//		
//		try {
//			return EMagicElement.valueOf(piece.getTag().getString(NBT_SHAPE_ELEMENT));
//		} catch (IllegalArgumentException e) {
//			NostrumMagica.logger.error("Failed to get element from rune");
//			return EMagicElement.PHYSICAL;
//		}
//	}
//	
//	private static void setPieceShapeElement(ItemStack piece, @Nullable EMagicElement element) {
//		if (!piece.hasTag())
//			return;
//		
//		if (element != null) {
//			piece.getTag().putString(NBT_SHAPE_ELEMENT, element.name());
//		} else {
//			piece.getTag().remove(NBT_SHAPE_ELEMENT);
//		}
//	}
//	
//	private static EAlteration getPieceShapeAlteration(ItemStack piece) {
//		if (!piece.hasTag())
//			return null;
//		
//		if (!piece.getTag().contains(NBT_SHAPE_ALTERATION, NBT.TAG_STRING))
//			return null;
//		
//		try {
//			return EAlteration.valueOf(piece.getTag().getString(NBT_SHAPE_ALTERATION));
//		} catch (IllegalArgumentException e) {
//			NostrumMagica.logger.error("Failed to get alteration from rune");
//			return null;
//		}
//	}
//	
//	private static void setPieceShapeAlteration(ItemStack piece, @Nullable EAlteration alteration) {
//		if (!piece.hasTag())
//			return;
//		
//		if (alteration != null) {
//			piece.getTag().putString(NBT_SHAPE_ALTERATION, alteration.name());
//		} else {
//			piece.getTag().remove(NBT_SHAPE_ALTERATION);
//		}
//	}
//	
//	private static int getPieceElementCount(ItemStack piece) {
//		if (!piece.hasTag())
//			return 0;
//		
//		return piece.getTag().getInt(NBT_ELEMENT_COUNT);
//	}
//	
//	private static void setPieceElementCount(ItemStack piece, int count) {
//		if (!piece.hasTag())
//			return;
//		
//		if (count > 0) {
//			piece.getTag().putInt(NBT_ELEMENT_COUNT, count);
//		} else {
//			piece.getTag().remove(NBT_ELEMENT_COUNT);
//		}
//	}
	
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
	
	public static boolean isShape(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ShapeSpellRune;
	}
	
	public static boolean isTrigger(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof TriggerSpellRune;
	}
	
	public static boolean isAlteration(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof AlterationSpellRune;
	}
	
	public static boolean isElement(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ElementSpellRune;
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		// Probably wnat this later
		return super.onItemUse(context);
	}
	
	protected abstract boolean isSpellReady(ItemStack stack);
	
	public static boolean isSpellWorthy(ItemStack stack) {
		boolean isReady = false;
		
		if (!stack.isEmpty() && stack.getItem() instanceof SpellRune) {
			isReady = ((SpellRune) stack.getItem()).isSpellReady(stack);
		}
		
		return isReady;
	}
	
	protected abstract @Nullable SpellPart getSpellPart(ItemStack stack);
	
	/**
	 * Works with trigger or shape runes.
	 * Pulls out the data and makes a spell part, complete with
	 * spell part params.
	 * @param stack
	 * @return null on error
	 */
	public static SpellPart getPart(ItemStack stack) {
		SpellPart part = null;
		
		if (!stack.isEmpty() && stack.getItem() instanceof SpellRune) {
			((SpellRune) stack.getItem()).getSpellPart(stack);
		}
		
		return part;
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
		
		return ((SpellRune) rune.getItem()).getComponent();
	}

	@Override
	public InfoScreenTabs getTab() {
		return InfoScreenTabs.INFO_SPELLS;
	}
	
	public static final class TriggerSpellRune extends SpellRune {
		
		protected static final String ID_FIX = "trigger_";
		
		protected final SpellTrigger trigger;
		
		public TriggerSpellRune(SpellTrigger trigger) {
			super();
			this.trigger = trigger;
		}
		
		@Override
		public SpellComponentWrapper getComponent() {
			return new SpellComponentWrapper(trigger);
		}
		
		@Override
		public String makeRegistryName() {
			return SpellRune.ID_PREFIX + ID_FIX + this.trigger.getTriggerKey();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
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
		}
		
		@Override
		protected NonNullList<ItemStack> decompose(ItemStack rune, @Nonnull NonNullList<ItemStack> output) {
			output.add(rune); // Nothing to do
			return output;
		}
		
		@Override
		protected @Nullable SpellPart getSpellPart(ItemStack stack) {
			SpellPart part = null;
			
			part = new SpellPart(trigger);
			part.setParam(getPieceParam(stack));
			
			return part;
		}
		
		@Override
		protected boolean isSpellReady(ItemStack stack) {
			return true;
		}
	}
	
	public static final class ElementSpellRune extends SpellRune {
		
		protected final EMagicElement element;
		
		public ElementSpellRune(EMagicElement element) {
			super();
			this.element = element;
		}
		
		@Override
		public SpellComponentWrapper getComponent() {
			return new SpellComponentWrapper(element);
		}
		
		@Override
		public String makeRegistryName() {
			return SpellRune.ID_PREFIX + this.element.name().toLowerCase();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
			tooltip.add(new StringTextComponent("Element").applyTextStyle(TextFormatting.DARK_GRAY));
			int count = 1; //getPieceElementCount(stack); TODO
			if (count != 0)
				tooltip.add(new StringTextComponent("Power " + count).applyTextStyle(TextFormatting.DARK_GREEN));
			
		}
		
		@Override
		protected NonNullList<ItemStack> decompose(ItemStack rune, @Nonnull NonNullList<ItemStack> output) {
			output.add(rune); // Nothing to do
			return output;
		}
		
		@Override
		protected @Nullable SpellPart getSpellPart(ItemStack stack) {
			return null;
		}
		
		@Override
		protected boolean isSpellReady(ItemStack stack) {
			return false; // Can't be used in spells directly
		}
	}
	
	public static final class AlterationSpellRune extends SpellRune {
		
		protected final EAlteration alteration;
		
		public AlterationSpellRune(EAlteration alteration) {
			super();
			this.alteration = alteration;
		}
		
		@Override
		public SpellComponentWrapper getComponent() {
			return new SpellComponentWrapper(alteration);
		}
		
		@Override
		public String makeRegistryName() {
			return SpellRune.ID_PREFIX + this.alteration.name().toLowerCase();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
			tooltip.add(new StringTextComponent("Alteration").applyTextStyle(TextFormatting.AQUA));
		}
		
		@Override
		protected NonNullList<ItemStack> decompose(ItemStack rune, @Nonnull NonNullList<ItemStack> output) {
			output.add(rune); // Nothing to do
			return output;
		}
		
		@Override
		protected @Nullable SpellPart getSpellPart(ItemStack stack) {
			return null;
		}
		
		@Override
		protected boolean isSpellReady(ItemStack stack) {
			return false; // Can't be used in spells directly
		}
	}
	
	public static final class ShapeSpellRune extends SpellRune {
		
		protected static final String ID_FIX = "shape_";
		private static final String NBT_SHAPE_ALTERATION = "shape_alteration"; // shapes
		private static final String NBT_SHAPE_ELEMENT = "shape_element"; // shapes
		private static final String NBT_ELEMENT_COUNT = "e_count"; // shapes
		
		protected final SpellShape shape;

		public ShapeSpellRune(SpellShape shape) {
			super();
			this.shape = shape;
		}
		
		@Override
		public SpellComponentWrapper getComponent() {
			return new SpellComponentWrapper(shape);
		}
		
		@Override
		public String makeRegistryName() {
			return SpellRune.ID_PREFIX + ID_FIX + this.shape.getShapeKey();
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void addInformation(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
			tooltip.add(new StringTextComponent("Shape").applyTextStyle(TextFormatting.DARK_RED));
			EMagicElement elem = getNestedElement(stack);
			if (elem != null) {
				tooltip.add(new StringTextComponent(elem.getName()).applyTextStyle(TextFormatting.DARK_GRAY));
			}
			int count = getNestedElementCount(stack);
			if (count != 0) {
				tooltip.add(new StringTextComponent("Power ").applyTextStyle(TextFormatting.DARK_GREEN));
			}
			EAlteration alteration = getNestedAlteration(stack);
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
		
		@Override
		protected NonNullList<ItemStack> decompose(ItemStack rune, @Nonnull NonNullList<ItemStack> output) {
			// Is shape. Pop out and elements or alterations
			@Nullable EAlteration alt = getNestedAlteration(rune);
			if (alt != null) {
				output.add(getRune(alt));
				setNestedAlteration(rune, null);
			}
			@Nullable EMagicElement elem = getNestedElement(rune);
			if (elem != null) {
				int elemCount = getNestedElementCount(rune);
				output.add(getRune(elem, elemCount));
				setNestedElement(rune, null, 0);
			}
			output.add(rune); // Elem and alt are stripped away at this point if they were present
			
			return output;
		}
		
		@Override
		protected @Nullable SpellPart getSpellPart(ItemStack stack) {
			SpellPart part = null;
			EMagicElement element = getNestedElement(stack);
			//if (element == null)
			//	return null;
			
			int count = getNestedElementCount(stack);
			if (count < 1)
				count = 1;
			
			EAlteration alteration = getNestedAlteration(stack);
			
			part = new SpellPart(shape, element, count, alteration);
			
			part.setParam(getPieceParam(stack));
			
			return part;
		}
		
		@Override
		protected boolean isSpellReady(ItemStack stack) {
			// For shapes, we ALSO need an element
			return null != getNestedElement(stack);
		}
		
		public SpellShape getShape(ItemStack stack) {
			return this.shape;
		}
		
		public @Nullable EMagicElement getNestedElement(ItemStack stack) {
			if (!stack.hasTag())
				return null;
			
			if (!stack.getTag().contains(NBT_SHAPE_ELEMENT, NBT.TAG_STRING))
				return null;
			
			try {
				return EMagicElement.valueOf(stack.getTag().getString(NBT_SHAPE_ELEMENT).toUpperCase());
			} catch (IllegalArgumentException e) {
				NostrumMagica.logger.error("Failed to get element from rune");
				return EMagicElement.PHYSICAL;
			}
		}
		
		public int getNestedElementCount(ItemStack stack) {
			if (!stack.hasTag())
				return 0;
			
			return stack.getTag().getInt(NBT_ELEMENT_COUNT);
		}
		
		public void setNestedElement(ItemStack stack, @Nullable EMagicElement element, int count) {
			CompoundNBT tag = stack.getTag();
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			if (element != null) {
				tag.putString(NBT_SHAPE_ELEMENT, element.name());
				tag.putInt(NBT_ELEMENT_COUNT, count);
			} else {
				tag.remove(NBT_SHAPE_ELEMENT);
				tag.remove(NBT_ELEMENT_COUNT);
			}
			
			stack.setTag(tag);
		}
		
		public @Nullable EAlteration getNestedAlteration(ItemStack stack) {
			if (!stack.hasTag())
				return null;
			
			if (!stack.getTag().contains(NBT_SHAPE_ALTERATION, NBT.TAG_STRING))
				return null;
			
			try {
				return EAlteration.valueOf(stack.getTag().getString(NBT_SHAPE_ALTERATION).toUpperCase());
			} catch (IllegalArgumentException e) {
				NostrumMagica.logger.error("Failed to get element from rune");
				return null;
			}
		}
		
		public void setNestedAlteration(ItemStack stack, @Nullable EAlteration alteration) {
			CompoundNBT tag = stack.getTag();
			if (tag == null) {
				tag = new CompoundNBT();
			}
			
			if (alteration != null) {
				tag.putString(NBT_SHAPE_ALTERATION, alteration.name());
			} else {
				tag.remove(NBT_SHAPE_ALTERATION);
			}
			
			stack.setTag(tag);
		}
		
	}
	
}
