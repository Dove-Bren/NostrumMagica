package com.smanzana.nostrummagica.item;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RuneShaperGui;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreenTabs;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperty;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.world.level.Level;
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
		super(NostrumItems.PropRuneBase());
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
	
	public static @Nullable SpellShapeProperties GetPieceShapeParam(ItemStack piece) {
		if (!piece.isEmpty() && piece.getItem() instanceof ShapeSpellRune) {
			return ((ShapeSpellRune) piece.getItem()).getPieceShapeParam(piece);
		}
		
		return null;
	}
	
	public static void setPieceParam(ItemStack piece, SpellShapeProperties params) {
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
	
	public static @Nullable SpellShape getShape(ItemStack stack) {
		if (isShape(stack)) {
			return ((ShapeSpellRune) stack.getItem()).getShape();
		}
		return null;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		// Probably wnat this later
		return super.useOn(context);
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
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(new TextComponent("Element").withStyle(ChatFormatting.DARK_GRAY));
			
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
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			final boolean extra = Screen.hasShiftDown();
			tooltip.add(new TextComponent("Alteration").withStyle(ChatFormatting.AQUA));
			if (extra) {
				tooltip.add(new TextComponent("Weight " + alteration.getWeight()).withStyle(ChatFormatting.DARK_PURPLE));
				tooltip.add(new TextComponent(alteration.getCost() + " Mana").withStyle(ChatFormatting.GREEN));
			}
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
		
		@SuppressWarnings("unchecked")
		protected <T> void addTooltipProperty(List<Component> tooltip, SpellShapeProperty<T> property, SpellShape shape, Object value) {
			tooltip.add(property.getDisplayName(shape)
					.append(new TextComponent(": "))
					.append(property.getDisplayValue(shape, (T) value))
					);
		}
		
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			final boolean extra = Screen.hasShiftDown();
			final @Nullable AbstractContainerMenu openContainer = (NostrumMagica.instance.proxy.getPlayer() != null)
					? NostrumMagica.instance.proxy.getPlayer().containerMenu : null;
			SpellShapeProperties params = getPieceShapeParam(stack);
			tooltip.add(new TextComponent("Shape").withStyle(ChatFormatting.DARK_RED));
			if (shape.getAttributes(params).terminal) {
				tooltip.add(new TextComponent("Terminal Shape").withStyle(ChatFormatting.GRAY));
			}
			if (extra
					|| (openContainer != null && openContainer instanceof SpellCreationGui.SpellCreationContainer)) {
				tooltip.add(new TextComponent("Weight " + this.getShape().getWeight(params)).withStyle(ChatFormatting.DARK_PURPLE));
				tooltip.add(new TextComponent(this.getShape().getManaCost(params) + " Mana").withStyle(ChatFormatting.GREEN));
			}
			
			if (extra
					|| (openContainer != null && openContainer instanceof RuneShaperGui.RuneShaperContainer)) {
				if (!shape.getDefaultProperties().getProperties().isEmpty()) {
					tooltip.add(new TextComponent("Rune Shaper Compatible").withStyle(ChatFormatting.GOLD));
				}
			}

			SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
			for (SpellShapeProperty<?> property : params.getProperties()) {
				Object value = params.getValue(property);
				if (!property.getDefault().equals(value)) {
					addTooltipProperty(tooltip, property, comp.getShape(), value);
				}
			}
		}
		
		public SpellShapeProperties getPieceShapeParam(ItemStack piece) {
			SpellShapeProperties props = shape.getDefaultProperties();
			if (piece.hasTag() && piece.getTag().contains(NBT_SHAPE_PROPS)) {
				props = props.fromNBT(piece.getTag().getCompound(NBT_SHAPE_PROPS));
			}
			
			return props;
		}
		
		public void setPieceShapeParam(ItemStack stack, SpellShapeProperties params) {
			CompoundTag nbt = stack.getTag();
			if (nbt == null) {
				nbt = new CompoundTag();
			}
			
			nbt.put(NBT_SHAPE_PROPS, params.toNBT());
			
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
