package com.smanzana.nostrummagica.items;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.SpellShapePartProperties;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellEffectPart;
import com.smanzana.nostrummagica.spells.components.SpellShapePart;
import com.smanzana.nostrummagica.spells.components.shapes.SpellShape;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public abstract class SpellRune extends Item implements ILoreTagged {
	
	protected static final Map<SpellComponentWrapper, SpellRune> TypeMap = new HashMap<>();
	
	public static final void SetRuneForType(SpellComponentWrapper type, SpellRune rune) {
		TypeMap.put(type, rune);
	}
	
	public static final @Nullable SpellRune GetRuneForType(SpellComponentWrapper type) {
		return TypeMap.get(type);
	}
	
	protected static final String ID_PREFIX = "rune_";
	
	protected SpellRune() {
		super(NostrumItems.PropBase());
	}
	
	public abstract SpellComponentWrapper getComponent();
	
	public abstract String makeRegistryName();
	
	public static ItemStack getRune(SpellComponentWrapper wrapper) {
		Item rune = GetRuneForType(wrapper);
		return new ItemStack(rune);
	}
	
	public static ItemStack getRune(EMagicElement element) {
		return getRune(new SpellComponentWrapper(element));
	}
	
	public static ItemStack getRune(EAlteration alteration) {
		return getRune(new SpellComponentWrapper(alteration));
	}
	
	public static ItemStack getRune(SpellShape shape) {
		return getRune(new SpellComponentWrapper(shape));
	}
	
//	public static ItemStack getRune(SpellShape shape, EMagicElement element, int elementCount, @Nullable EAlteration alteration) {
//		PackedShapeSpellRune rune = GetPackedRuneForType(shape);
//		ItemStack stack = new ItemStack(rune);
//		
//		rune.setNestedElement(stack, element, elementCount);
//		if (alteration != null) {
//			rune.setNestedAlteration(stack, alteration);
//		}
//		
//		return stack;
//	}
	
	/**
	 * Get a rune that matches the spell part handed in.
	 * @param part
	 * @return
	 */
	public static ItemStack getRune(SpellShapePart part) {
		return ShapeSpellRune.MakeForPart(part);
	}
	
	/**
	 * Get a rune that matches the spell part handed in.
	 * @param part
	 * @return
	 */
	public static List<ItemStack> getRune(SpellEffectPart part) {
		List<ItemStack> runes = new ArrayList<>(4);
		for (int i = 0; i < part.getElementCount(); i++) {
			runes.add(getRune(part.getElement()));
		}
		
		if (part.getAlteration() != null) {
			runes.add(getRune(part.getAlteration()));
		}
		
		return runes;
	}
	
	public static @Nullable SpellShapePartProperties GetPieceShapeParam(ItemStack piece) {
		if (piece.getItem() instanceof ShapeSpellRune) {
			return ((ShapeSpellRune) piece.getItem()).getPieceShapeParam(piece);
		}
		
		return null;
	}
	
	public static void setPieceParam(ItemStack piece, SpellShapePartProperties params) {
		if (piece.getItem() instanceof ShapeSpellRune) {
			((ShapeSpellRune) piece.getItem()).setPieceShapeParam(piece, params);
		}
	}
	
	public static boolean isShape(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ShapeSpellRune;
	}
	
	public static boolean isAlteration(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof AlterationSpellRune;
	}
	
	public static boolean isElement(ItemStack stack) {
		return !stack.isEmpty() && stack.getItem() instanceof ElementSpellRune;
	}
	
	public static @Nullable EAlteration getAlteration(ItemStack stack) {
		if (isAlteration(stack)) {
			return ((AlterationSpellRune) stack.getItem()).getAlteration();
		}
		return null;
	}
	
	public static @Nullable EMagicElement getElement(ItemStack stack) {
		if (isElement(stack)) {
			return ((ElementSpellRune) stack.getItem()).getElement();
		}
		return null;
	}
	
	@Override
	public ActionResultType onItemUse(ItemUseContext context) {
		// Probably wnat this later
		return super.onItemUse(context);
	}
	
	protected abstract @Nullable SpellShapePart getSpellShapePart(ItemStack stack);
	
	/**
	 * Works with shape runes.
	 * Pulls out the data and makes a spell part, complete with
	 * spell part params.
	 * @param stack
	 * @return null on error
	 */
	public static @Nullable SpellShapePart getShapePart(ItemStack stack) {
		SpellShapePart part = null;
		
		if (!stack.isEmpty() && stack.getItem() instanceof SpellRune) {
			part = ((SpellRune) stack.getItem()).getSpellShapePart(stack);
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
		return new Lore().add("Runes are used to make new spells.", "There are three types of runes: Shapes, Elements, and Alterations.", "Shapes determine who is affected.", "Elements are added to shapes to give the effect an element.", "Alterations morph the effect of the spell.", "Every element and alteration combination is different.");
	}

	@Override
	public Lore getDeepLore() {
		return new Lore().add("Runes are used to make new spells.", "There are three types of runes: Shapes, Elements, and Alterations.", "Shapes determine who is affected.", "Elements are added to shapes to give the effect an element.", "Alterations morph the effect of the spell.", "Every element and alteration combination is different.", "After gaining mastery of an element, trigger, shape, or alteration, you can create runes any time you want.");
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
	
	public static final class ElementSpellRune extends SpellRune {
		
		protected final EMagicElement element;
		
		public ElementSpellRune(EMagicElement element) {
			super();
			this.element = element;
		}
		
		public EMagicElement getElement() {
			return this.element;
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
			tooltip.add(new StringTextComponent("Element").mergeStyle(TextFormatting.DARK_GRAY));
			
		}
		
		@Override
		protected SpellShapePart getSpellShapePart(ItemStack stack) {
			return null;
		}
	}
	
	public static final class AlterationSpellRune extends SpellRune {
		
		protected final EAlteration alteration;
		
		public AlterationSpellRune(EAlteration alteration) {
			super();
			this.alteration = alteration;
		}
		
		public EAlteration getAlteration() {
			return this.alteration;
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
			tooltip.add(new StringTextComponent("Alteration").mergeStyle(TextFormatting.AQUA));
			tooltip.add(new StringTextComponent("Weight " + alteration.getWeight()).mergeStyle(TextFormatting.DARK_PURPLE));
		}

		@Override
		protected SpellShapePart getSpellShapePart(ItemStack stack) {
			return null;
		}
	}
	
	public static class ShapeSpellRune extends SpellRune {
		
		protected static final String ID_FIX = "shape_";
		private static final String NBT_SHAPE_PROPS = "shape_props";
		
		protected final SpellShape shape;

		public ShapeSpellRune(SpellShape shape) {
			super();
			this.shape = shape;
		}

		public SpellShape getShape() {
			return this.shape;
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
			tooltip.add(new StringTextComponent("Shape").mergeStyle(TextFormatting.DARK_RED));
			tooltip.add(new StringTextComponent("Weight " + this.getShape().getWeight()).mergeStyle(TextFormatting.DARK_PURPLE));
			SpellShapePartProperties params = getPieceShapeParam(stack);
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
		
		public SpellShapePartProperties getPieceShapeParam(ItemStack piece) {
			if (!piece.hasTag() || !piece.getTag().contains(NBT_SHAPE_PROPS)) {
				return shape.getDefaultProperties();
			}
			
			return SpellShapePartProperties.FromNBT(piece.getTag().getCompound(NBT_SHAPE_PROPS));
		}
		
		public void setPieceShapeParam(ItemStack stack, SpellShapePartProperties params) {
			CompoundNBT nbt = stack.getTag();
			if (nbt == null) {
				nbt = new CompoundNBT();
			}
			
			nbt.put(NBT_SHAPE_PROPS, params.toNBT(null));
			
			stack.setTag(nbt);
		}

		public static ItemStack MakeForPart(SpellShapePart part) {
			ItemStack stack;
			stack = getRune(part.getShape());
			((ShapeSpellRune) stack.getItem()).setPieceShapeParam(stack, part.getProperties());			
			
			return stack;
		}

		@Override
		protected SpellShapePart getSpellShapePart(ItemStack stack) {
			return new SpellShapePart(this.shape, this.getPieceShapeParam(stack));
		}
	}
}
