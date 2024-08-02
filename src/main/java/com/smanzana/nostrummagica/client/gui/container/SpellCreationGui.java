package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.ISpellCraftPatternRenderer;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.client.gui.container.SpellCreationGui.SpellGui.SpellPartBar.IHoverHandler;
import com.smanzana.nostrummagica.client.gui.widget.FixedWidget;
import com.smanzana.nostrummagica.client.gui.widget.ParentWidget;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.item.BlankScroll;
import com.smanzana.nostrummagica.item.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.item.SpellRune;
import com.smanzana.nostrummagica.progression.skill.NostrumSkills;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellAction;
import com.smanzana.nostrummagica.spell.component.SpellAction.SpellActionProperties;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.NostrumSpellShapes;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape.SpellShapeAttributes;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting.SpellPartSummary;
import com.smanzana.nostrummagica.spellcraft.modifier.ISpellCraftModifier;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.util.ColorUtil;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.widget.Widget;
import net.minecraft.client.gui.widget.button.AbstractButton;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellCreationGui {
	
	public static final int MaxNameLength = 20;
	
	public static abstract class SpellCreationContainer extends net.minecraft.inventory.container.Container {
		
		protected static class ScrollSlot extends Slot {
			
			private final SpellCreationContainer container;
			
			public ScrollSlot(SpellCreationContainer container, IInventory inventory, int idx, int x, int y) {
				super(inventory, idx, x, y);
				this.container = container;
			}
			
			@Override
			public boolean isItemValid(@Nonnull ItemStack stack) {
				return (stack.isEmpty()
						|| stack.getItem() instanceof BlankScroll);
			}
			
			@Override
			public void putStack(@Nonnull ItemStack stack) {
				super.putStack(stack);
				
				container.validate();
			}
			
			@Override
			public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
				container.validate();
				return super.onTake(playerIn, stack);
			}
			
			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		}
		
		protected static class RuneSlot extends Slot {

			private final RuneSlot prev;
			private RuneSlot next;
			private final SpellCreationContainer container;
			
			public RuneSlot(SpellCreationContainer container, RuneSlot prev, IInventory inventoryIn, int index, int x, int y) {
				super(inventoryIn, index, x, y);
				this.prev = prev;
				this.container = container;
			}
			
			public void setNext(RuneSlot next) {
				this.next = next;
			}
			
			@Override
			public boolean isItemValid(@Nonnull ItemStack stack) {
				// Can put the item in if:
				// it's empty
				// OR previous slot is not null (not the first trigger-only slot)
				// OR it's a trigger rune
				// all ANDed with does the previous slot have a rune?
				if (!container.hasScroll)
					return false;
				
				if (prev != null &&
						!prev.getHasStack())
					return false;
				
				if (stack.isEmpty())
					return true;
				
				if (!(stack.getItem() instanceof SpellRune))
					return false;
				
				if (SpellRune.isShape(stack)) {
					return prev == null || SpellRune.isShape(prev.getStack());
				}
				if (SpellRune.isAlteration(stack)) {
					return prev != null && SpellRune.isElement(prev.getStack());
				}
				return true;
			}
			
			@Override
			@OnlyIn(Dist.CLIENT)
			public boolean isEnabled() {
				return (prev == null ||
						prev.getHasStack());
			}
			
			@Override
			public void putStack(@Nonnull ItemStack stack) {
				super.putStack(stack);
				
				container.validate();
			}
			
			@Override
			public @Nonnull ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
				// This is called AFTER things have been changed or swapped
				// Which means we just look to see if we have an item.
				// If not, take item from next
				if (!this.getHasStack() && next != null && next.getHasStack()) {
					this.putStack(next.getStack().copy());
					next.putStack(ItemStack.EMPTY);
					next.onTake(playerIn, this.getStack());
				}

				container.validate();
				
				return super.onTake(playerIn, stack);
			}
			
			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		}
		
		// Kept just to report to server which TE is doing crafting
		protected final BlockPos pos;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected final ISpellCraftingInventory inventory;
		protected final PlayerEntity player;
		protected final SpellCraftContext context; // Made once for efficiency
		
		protected final List<Consumer<Spell>> spellListeners;
		protected boolean hasScroll; // has an acceptable scroll
		protected boolean spellValid; // grammar checks out
		protected final List<ITextComponent> spellErrorStrings; // Updated on validate(); what's wrong?
		protected final List<ITextComponent> reagentStrings; // Updated on validate; what reagents will be used. Only filled if successful
		protected final List<SpellPartSummary> parts; // Updated on validate; what spell parts make up our spell?
		protected int lastManaCost;
		protected int lastWeight;
		
		public SpellCreationContainer(ContainerType<? extends SpellCreationContainer> type, int windowId, PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory, BlockPos tablePos) {
			super(type, windowId);
			this.inventory = tableInventory;
			this.player = crafter;
			this.pos = tablePos;
			this.context = new SpellCraftContext(crafter, crafter.world, pos);
			
			spellErrorStrings = new ArrayList<>();
			reagentStrings = new ArrayList<>();
			parts = new ArrayList<>(inventory.getRuneSlotCount());
			spellListeners = new ArrayList<>();
			
			// Dont auto call this; let children, so that they can set up things they need to first.
			//validate();
		}
		
		public abstract String getName();
		
		public abstract int getSpellIcon();
		
		public abstract @Nullable SpellCraftPattern getCraftPattern();
		
		public void addListener(Consumer<Spell> listener) {
			this.spellListeners.add(listener);
		}
		
		protected boolean isValidScroll(ItemStack stack) {
			return !stack.isEmpty() && stack.getItem() instanceof BlankScroll;
		}
		
		protected void checkScroll() {
			this.hasScroll = isValidScroll(this.inventory.getScrollSlotContents());
		}
		
		public boolean hasScroll() {
			return this.hasScroll;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			checkScroll();
			ItemStack ret = super.slotClick(slotId, dragType, clickTypeIn, player);
			return ret;
		}
		
		@Override
		public ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				
				if (slot.inventory == this.inventory) {
					// Trying to take from the table
					ItemStack dupe = cur.copy();
					if (playerIn.inventory.addItemStackToInventory(dupe)) {
						slot.putStack(ItemStack.EMPTY);
						slot.onTake(playerIn, dupe);
					}
				} else {
					// Trying to add an item
					if (cur.getItem() instanceof BlankScroll) {
						ItemStack existing = inventory.getScrollSlotContents();
						if (existing.isEmpty()) {
							inventory.setScrollSlotContents(cur.split(1));
							this.validate();
						}
					} else if (cur.getItem() instanceof SpellRune) {
						// Only allow adding if blank scroll is in place
						if (!this.hasScroll()) {
							// Do nothing
						} else if (!inventory.getRuneSlotContents(inventory.getRuneSlotCount() - 1).isEmpty()) {
							// If something's in last slot, we're full
							// Table will naturally shift things down
						} else {
							// If this is anything but shape or trigger, do nothing
							//SpellComponentWrapper wrapper = SpellRune.toComponentWrapper(cur);
							boolean add = true;
//							if (wrapper.isElement()) {
//								// Can always add elements
//								add = true;
//							} else if (wrapper.isAlteration()) {
//								add = 
//							} else if (SpellRune.isPackedShape(cur)) {
//								// Must have a trigger in first slot already
//								if (!inventory.getRuneSlotContents(0).isEmpty())
//									add = true;
//							}
							
							if (add) {
								int index = 0;
								while (!inventory.getRuneSlotContents(index).isEmpty())
									index++;
								
								inventory.setRuneSlotContents(index, cur.split(1));
								//cur = ItemStack.EMPTY;
								this.validate();
							}
						}
					}
				}
				
				if (cur.isEmpty()) {
					slot.putStack(ItemStack.EMPTY);
				}
			}
			
			return ItemStack.EMPTY;
		}
		
		public boolean hasProblems() {
			return !this.spellValid;
		}
		
		public List<ITextComponent> getProblems() {
			return this.spellErrorStrings;
		}
		
		public List<ITextComponent> getReagentStrings() {
			return this.reagentStrings;
		}
		
		public int getCurrentWeight() {
			if (!this.hasProblems()) {
				return this.lastWeight;
			}
			
			// Could cache
			return SpellCrafting.CalculateWeightFromRunes(getCraftContext(), getCraftPattern(), inventory, inventory.getRuneSlotStartingIndex(), inventory.getRuneSlotCount());
		}
		
		public int getMaxWeight() {
			return this.inventory.getMaxWeight(player);
		}
		
		public int getManaCost() {
			if (!this.hasProblems()) {
				return this.lastManaCost;
			}
			
			return SpellCrafting.CalculateManaCostFromRunes(getCraftContext(), getCraftPattern(), inventory, inventory.getRuneSlotStartingIndex(), inventory.getRuneSlotCount());
		}

		public int getFilledRuneSlots() {
			final int runeSlots = this.inventory.getRuneSlotCount();
			for (int i = 0; i < runeSlots; i++) {
				if (this.inventory.getRuneSlotContents(i).isEmpty()) {
					return i;
				}
			}
			return runeSlots;
		}
		
		protected void validate() {
			checkScroll();
			if (this.hasScroll) {
				Spell spell = makeSpell();
				spellValid = (spell != null);
				alertListeners(spell);
			} else {
				spellValid = false;
				alertListeners(null);
			}
		}
		
		private void alertListeners(@Nullable Spell spell) {
			for (Consumer<Spell> listener : this.spellListeners) {
				listener.accept(spell);
			}
		}
		
		public SpellCraftContext getCraftContext() {
			return this.context;
		}
		
		public Spell makeSpell() {
			return makeSpell(false);
		}
		
		public Spell makeSpell(boolean clear) {
			return makeSpell(this.getName(), this.getSpellIcon(), this.getCraftPattern(), clear);
		}
		
		public Spell makeSpell(String name, int iconIdx, @Nullable SpellCraftPattern pattern, boolean clear) {
			// Don't cache from validate... just in case...
			Spell spell = craftSpell(getCraftContext(), pattern, name, iconIdx, this.inventory, this.player, this.spellErrorStrings, this.reagentStrings, this.parts, clear);
			
			if (spell == null)
				return null;
			
			this.lastManaCost = spell.getManaCost();
			this.lastWeight = spell.getWeight();
			
			if (this.lastWeight > this.getMaxWeight()) {
				this.spellErrorStrings.add(new StringTextComponent("Too much weight"));
				return null;
			}
			
			List<ITextComponent> problems = new ArrayList<>();
			if (!getCraftContext().magic.hasSkill(NostrumSkills.Spellcraft_Proxy) && !NostrumMagica.canCast(spell, this.context.magic, problems)) {
				this.spellErrorStrings.add(new StringTextComponent("You could not cast this spell:"));
				for (ITextComponent problem : problems) {
					this.spellErrorStrings.add(problem);
				}
				return null;
			}
			
			if (clear)
				this.inventory.clearSpellBoard();
			
			return spell;
		}
		
		public static Spell craftSpell(SpellCraftContext context, @Nullable SpellCraftPattern pattern, String name, int iconIdx, ISpellCraftingInventory inventory, PlayerEntity crafter,
				List<ITextComponent> spellErrorStrings, List<ITextComponent> reagentStrings, List<SpellPartSummary> parts,
				boolean deductReagents) {
			boolean fail = false;
			//INostrumMagic attr = NostrumMagica.getMagicWrapper(crafter);
			boolean locked = !SpellCrafting.CanCraftSpells(crafter);
			
			// Lazily make these instead of guarding each place that might use it to see if it's null or not
			if (spellErrorStrings == null) {
				spellErrorStrings = new ArrayList<>();
			}
			if (reagentStrings == null) {
				reagentStrings = new ArrayList<>();
			}
			if (parts == null) {
				parts = new ArrayList<>();
			}
			spellErrorStrings.clear();
			reagentStrings.clear();
			parts.clear();
			
			if (locked) {
				spellErrorStrings.add(new StringTextComponent("The runes on the board don't respond to your hands"));
				return null;
			}
			
			if (name.trim().isEmpty()) {
				spellErrorStrings.add(new StringTextComponent("Must have a name"));
				fail = true;
			}
			
			if (iconIdx < 0) {
				spellErrorStrings.add(new StringTextComponent("Must have a spell icon selected"));
				fail = true;
			}
			
			List<String> rawSpellErrors = new ArrayList<>();
			if (!SpellCrafting.CheckForValidRunes(context, inventory, inventory.getRuneSlotStartingIndex(), inventory.getRuneSlotCount(), rawSpellErrors)) {
				fail = true;
			}
			
//			// Stop here if already failing and avoid creating the spell
//			if (fail) {
//				return null;
//			}
			
			// Actually make spell
			Spell spell = SpellCrafting.CreateSpellFromRunes(context, pattern, name, inventory, inventory.getRuneSlotStartingIndex(), inventory.getRuneSlotCount(), rawSpellErrors, parts);
			
			if (fail || spell == null) {
				// Dump raw errors into output strings and return
				for (String error : rawSpellErrors) {
					spellErrorStrings.add(new StringTextComponent(error));
				}
				return null;
			}
			
			// Do reagent check
			Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
			for (ReagentType type : reagents.keySet()) {
				if (type == null)
					continue;
				Integer count = reagents.get(type);
				if (count == null || count == 0)
					continue;
				
				final int available = NostrumMagica.getReagentCount(crafter, type);
				if (available < count) {
					spellErrorStrings.add(new StringTextComponent("Need " + (count-available) + " more " + type.prettyName()));
					reagentStrings.add(new StringTextComponent(count + " " + type.prettyName()).mergeStyle(TextFormatting.DARK_RED));
					fail = true;
				} else {
					reagentStrings.add(new StringTextComponent(count + " " + type.prettyName()));
				}
			}
			
			if (fail)
				return null;
			
			// Actual deduct reagents
			if (deductReagents) {
				System.out.println("Deducting reagents");
				for (ReagentType type : reagents.keySet()) {
					if (type == null)
						continue;
					Integer count = reagents.get(type);
					if (count == null)
						continue;
					
					if (!NostrumMagica.removeReagents(crafter, type, count)) {
						System.out.println("Couldn't take all " + type.name());
						spellErrorStrings.add(new StringTextComponent("Need more " + type.prettyName()));
						return null;
					}
					
				}
			}
			
			return spell;
		}
	}
	
	public static abstract class SpellGui<T extends SpellCreationContainer> extends AutoGuiContainer<T> implements IJEIAwareGuiContainer {
		
		private static final ResourceLocation TEXT_UTILS = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/spell_create.png");
		
		private static final int TEXT_UTILS_WIDTH = 64;
		private static final int TEXT_UTILS_HEIGHT = 64;
		
		private static final int TEXT_STATUS_GOOD_HOFFSET = 0;
		private static final int TEXT_STATUS_GOOD_VOFFSET = 0;
		private static final int TEXT_STATUS_GOOD_WIDTH = 10;
		private static final int TEXT_STATUS_GOOD_HEIGHT = 10;
		
		private static final int TEXT_STATUS_BAD_HOFFSET = TEXT_STATUS_GOOD_HOFFSET + TEXT_STATUS_GOOD_WIDTH;
		private static final int TEXT_STATUS_BAD_VOFFSET = 0;
		private static final int TEXT_STATUS_BAD_WIDTH = TEXT_STATUS_GOOD_WIDTH;
		private static final int TEXT_STATUS_BAD_HEIGHT = TEXT_STATUS_GOOD_HEIGHT;
		
		private static final int TEXT_ICONBACK_PRESSED_HOFFSET = 0;
		private static final int TEXT_ICONBACK_PRESSED_VOFFSET = 10;
		private static final int TEXT_ICONBACK_PRESSED_WIDTH = 20;
		private static final int TEXT_ICONBACK_PRESSED_HEIGHT = 20;
		
		private static final int TEXT_ICONBACK_HOFFSET = 0;
		private static final int TEXT_ICONBACK_VOFFSET = TEXT_ICONBACK_PRESSED_VOFFSET + TEXT_ICONBACK_PRESSED_HEIGHT;
		private static final int TEXT_ICONBACK_WIDTH = TEXT_ICONBACK_PRESSED_WIDTH;
		private static final int TEXT_ICONBACK_HEIGHT = TEXT_ICONBACK_PRESSED_HEIGHT;
		
		private static final int TEXT_SUBMIT_DISABLED_HOFFSET = 20;
		private static final int TEXT_SUBMIT_DISABLED_VOFFSET = 0;
		private static final int TEXT_SUBMIT_DISABLED_WIDTH = 18;
		private static final int TEXT_SUBMIT_DISABLED_HEIGHT = 10;
		
		private static final int TEXT_SUBMIT_HOFFSET = TEXT_SUBMIT_DISABLED_HOFFSET;
		private static final int TEXT_SUBMIT_VOFFSET = TEXT_SUBMIT_DISABLED_VOFFSET + TEXT_SUBMIT_DISABLED_HEIGHT;
		private static final int TEXT_SUBMIT_WIDTH = TEXT_SUBMIT_DISABLED_WIDTH;
		private static final int TEXT_SUBMIT_HEIGHT = TEXT_SUBMIT_DISABLED_HEIGHT;
		
		private static final int TEXT_SCALE_HOFFSET = 20;
		private static final int TEXT_SCALE_VOFFSET = 20;
		private static final int TEXT_SCALE_WIDTH = 32;
		private static final int TEXT_SCALE_HEIGHT = 32;
		
		private static final int TEXT_GUAGE_HOFFSET = 0;
		private static final int TEXT_GUAGE_VOFFSET = 56;
		private static final int TEXT_GUAGE_WIDTH = 64;
		private static final int TEXT_GUAGE_HEIGHT = 8;
		
//		private static final int TEXT_GUAGE_INNER_HMARGIN = 3;
//		private static final int TEXT_GUAGE_INNER_VMARGIN = 2;
//		private static final int TEXT_GUAGE_INNER_WIDTH = 58;
//		private static final int TEXT_GUAGE_INNER_HEIGHT = 4;
		
		private static final int TEX_PATTERN_HOFFSET = 38;
		private static final int TEX_PATTERN_VOFFSET = 0;
		private static final int TEX_PATTERN_WIDTH = 20;
		private static final int TEX_PATTERN_HEIGHT = 20;
		
		private static final int TEX_INFPANEL_BORDER_TL_HOFFSET = 52;
		private static final int TEX_INFPANEL_BORDER_TL_VOFFSET = 20;
		private static final int TEX_INFPANEL_BORDER_TL_WIDTH = 4;
		private static final int TEX_INFPANEL_BORDER_TL_HEIGHT = 4;
		
		private static final int TEX_INFPANEL_BORDER_TOP_HOFFSET = TEX_INFPANEL_BORDER_TL_HOFFSET + TEX_INFPANEL_BORDER_TL_WIDTH;
		private static final int TEX_INFPANEL_BORDER_TOP_VOFFSET = TEX_INFPANEL_BORDER_TL_VOFFSET;
		private static final int TEX_INFPANEL_BORDER_TOP_WIDTH = 1;
		private static final int TEX_INFPANEL_BORDER_TOP_HEIGHT = 4;
		
		private static final int TEX_INFPANEL_BORDER_TR_HOFFSET = TEX_INFPANEL_BORDER_TOP_HOFFSET + TEX_INFPANEL_BORDER_TOP_WIDTH;
		private static final int TEX_INFPANEL_BORDER_TR_VOFFSET = TEX_INFPANEL_BORDER_TOP_VOFFSET;
		private static final int TEX_INFPANEL_BORDER_TR_WIDTH = 4;
		private static final int TEX_INFPANEL_BORDER_TR_HEIGHT = 4;
		
		private static final int TEX_INFPANEL_BORDER_LEFT_HOFFSET = TEX_INFPANEL_BORDER_TL_HOFFSET;
		private static final int TEX_INFPANEL_BORDER_LEFT_VOFFSET = TEX_INFPANEL_BORDER_TL_VOFFSET + TEX_INFPANEL_BORDER_TL_HEIGHT;
		private static final int TEX_INFPANEL_BORDER_LEFT_WIDTH = 4;
		private static final int TEX_INFPANEL_BORDER_LEFT_HEIGHT = 1;
		
		private static final int TEX_INFPANEL_CENTER_HOFFSET = TEX_INFPANEL_BORDER_LEFT_HOFFSET + TEX_INFPANEL_BORDER_LEFT_WIDTH;
		private static final int TEX_INFPANEL_CENTER_VOFFSET = TEX_INFPANEL_BORDER_LEFT_VOFFSET;
		private static final int TEX_INFPANEL_CENTER_WIDTH = 1;
		private static final int TEX_INFPANEL_CENTER_HEIGHT = 1;
		
		private static final int TEX_INFPANEL_BORDER_RIGHT_HOFFSET = TEX_INFPANEL_CENTER_HOFFSET + TEX_INFPANEL_CENTER_WIDTH;
		private static final int TEX_INFPANEL_BORDER_RIGHT_VOFFSET = TEX_INFPANEL_CENTER_VOFFSET;
		private static final int TEX_INFPANEL_BORDER_RIGHT_WIDTH = 4;
		private static final int TEX_INFPANEL_BORDER_RIGHT_HEIGHT = 1;
		
		private static final int TEX_INFPANEL_BORDER_BL_HOFFSET = TEX_INFPANEL_BORDER_TL_HOFFSET;
		private static final int TEX_INFPANEL_BORDER_BL_VOFFSET = TEX_INFPANEL_BORDER_LEFT_VOFFSET + TEX_INFPANEL_BORDER_LEFT_HEIGHT;
		private static final int TEX_INFPANEL_BORDER_BL_WIDTH = 4;
		private static final int TEX_INFPANEL_BORDER_BL_HEIGHT = 4;
		
		private static final int TEX_INFPANEL_BORDER_BOTTOM_HOFFSET = TEX_INFPANEL_BORDER_BL_HOFFSET + TEX_INFPANEL_BORDER_BL_WIDTH;
		private static final int TEX_INFPANEL_BORDER_BOTTOM_VOFFSET = TEX_INFPANEL_BORDER_BL_VOFFSET;
		private static final int TEX_INFPANEL_BORDER_BOTTOM_WIDTH = 1;
		private static final int TEX_INFPANEL_BORDER_BOTTOM_HEIGHT = 4;
		
		private static final int TEX_INFPANEL_BORDER_BR_HOFFSET = TEX_INFPANEL_BORDER_BOTTOM_HOFFSET + TEX_INFPANEL_BORDER_BOTTOM_WIDTH;
		private static final int TEX_INFPANEL_BORDER_BR_VOFFSET = TEX_INFPANEL_BORDER_BOTTOM_VOFFSET;
		private static final int TEX_INFPANEL_BORDER_BR_WIDTH = 4;
		private static final int TEX_INFPANEL_BORDER_BR_HEIGHT = 4;
		
		private static final int TEX_BOOST_HOFFSET = 52;
		private static final int TEX_BOOST_VOFFSET = 29;
		private static final int TEX_BOOST_WIDTH = 9;
		private static final int TEX_BOOST_HEIGHT = 5;
		
		private static final int TEX_PENALTY_HOFFSET = TEX_BOOST_HOFFSET;
		private static final int TEX_PENALTY_VOFFSET = TEX_BOOST_VOFFSET + TEX_BOOST_HEIGHT;
		private static final int TEX_PENALTY_WIDTH = 9;
		private static final int TEX_PENALTY_HEIGHT = 5;
		
		protected static class SpellIconButton extends Button {
			
			private final int value;
			private final SpellGui<?> gui;
			
			public SpellIconButton(int x, int y, int width, int height, int val, SpellGui<?> gui) {
				super(x, y, width, height, StringTextComponent.EMPTY, (b) -> {
					gui.iconButtonClicked((SpellIconButton) b);
				});
				this.value = val;
				this.gui = gui;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				float tint = 1f;
				mc.getTextureManager().bindTexture(TEXT_UTILS);
				if (mouseX >= this.x && mouseY >= this.y
						&& mouseX <= this.x + this.width
						&& mouseY <= this.y + this.height) {
					tint = .8f;
				}
				
				final int u, v, wu, hv;
				if (gui.container.getSpellIcon() != this.value) {
					u = TEXT_ICONBACK_HOFFSET;
					v = TEXT_ICONBACK_VOFFSET;
					wu = TEXT_ICONBACK_WIDTH;
					hv = TEXT_ICONBACK_HEIGHT;
				} else {
					u = TEXT_ICONBACK_PRESSED_HOFFSET;
					v = TEXT_ICONBACK_PRESSED_VOFFSET;
					wu = TEXT_ICONBACK_PRESSED_WIDTH;
					hv = TEXT_ICONBACK_PRESSED_HEIGHT;
				}
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, wu, hv,
						this.width, this.height, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT,
						tint, tint, tint, 1f);
				
				SpellIcon.get(this.value).render(mc, matrixStackIn, this.x + 2, this.y + 2, this.width - 4, this.height - 4,
						tint, tint, tint, 1f);
			}
			
		}
		
		protected static class SubmitButton extends AbstractButton {
			private final SpellGui<?> gui;
			
			public SubmitButton(SpellGui<?> gui, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
			}

			@Override
			public void onPress() {
				gui.submitButtonClicked(this);
			}
			
			@Override
			public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
				if (!gui.getContainer().hasProblems()) {
					gui.func_243308_b(matrixStackIn, gui.getContainer().getReagentStrings(), mouseX, mouseY);
				}
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				mc.getTextureManager().bindTexture(TEXT_UTILS);
				final int u, v, wu, hv;
				final float tint;
				if (!gui.getContainer().hasProblems()) {
					u = TEXT_SUBMIT_HOFFSET;
					v = TEXT_SUBMIT_VOFFSET;
					wu = TEXT_SUBMIT_WIDTH;
					hv = TEXT_SUBMIT_HEIGHT;
					tint = this.isHovered() ? .8f : 1f;
				} else {
					u = TEXT_SUBMIT_DISABLED_HOFFSET;
					v = TEXT_SUBMIT_DISABLED_VOFFSET;
					wu = TEXT_SUBMIT_DISABLED_WIDTH;
					hv = TEXT_SUBMIT_DISABLED_HEIGHT;
					tint = 1f;
				}
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, wu, hv,
						this.width, this.height, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT,
						tint, tint, tint, 1f
						);
				
				if (this.isHovered()) {
					matrixStackIn.push();
					matrixStackIn.translate(0, 0, 100);
					this.renderToolTip(matrixStackIn, mouseX, mouseY);
					matrixStackIn.pop();
				}
			}
		}
		
		protected static class SpellStatusIcon extends Widget {
			
			private final SpellGui<?> gui;
			
			public SpellStatusIcon(SpellGui<?> gui, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
			}
			
			@Override
			public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
				if (gui.getContainer().hasProblems()) {
					List<ITextComponent> problems = gui.getContainer().getProblems();
					gui.func_243308_b(matrixStackIn, problems, mouseX, mouseY);
				}
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				mc.getTextureManager().bindTexture(TEXT_UTILS);
				final int u, v, wu, hv;
				if (this.gui.getContainer().hasProblems()) {
					u = TEXT_STATUS_BAD_HOFFSET;
					v = TEXT_STATUS_BAD_VOFFSET;
					wu = TEXT_STATUS_BAD_WIDTH;
					hv = TEXT_STATUS_BAD_HEIGHT;
				} else {
					u = TEXT_STATUS_GOOD_HOFFSET;
					v = TEXT_STATUS_GOOD_VOFFSET;
					wu = TEXT_STATUS_GOOD_WIDTH;
					hv = TEXT_STATUS_GOOD_HEIGHT;
				}
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						u, v, wu, hv,
						this.width, this.height, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT
						);
				
				if (this.isHovered()) {
					matrixStackIn.push();
					matrixStackIn.translate(0, 0, 100);
					this.renderToolTip(matrixStackIn, mouseX, mouseY);
					matrixStackIn.pop();
				}
			}
		}
		
		protected static class WeightStatus extends Widget {
			
			private final SpellGui<?> gui;
			
			public WeightStatus(SpellGui<?> gui, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
			}
			
			@Override
			public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
				final int weight = gui.getContainer().getCurrentWeight();
				final int maxWeight = gui.getContainer().getMaxWeight();
				gui.renderTooltip(matrixStackIn, new TranslationTextComponent("info.spellcraft.weight_tooltip", weight, maxWeight), mouseX, mouseY);
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				final int weight = gui.getContainer().getCurrentWeight();
				final int maxWeight = gui.getContainer().getMaxWeight();
				
				// Need to break up space better. Greedily taking up full height for icon
				final int iconHeight = height;
				final int iconWidth = iconHeight;
				
				final int meterWidth = width - (iconWidth + 1);
				final int meterHeight = 8;
				final int meterBarWidth = meterWidth - 2;
				final int meterBarHeight = 6;
				
				mc.getTextureManager().bindTexture(TEXT_UTILS);
				
				// Scale icon
				final float[] scaleColor = ColorUtil.ARGBToColor(this.getScaleIconColor(weight, maxWeight));
				RenderSystem.enableBlend();
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y,
						TEXT_SCALE_HOFFSET, TEXT_SCALE_VOFFSET, TEXT_SCALE_WIDTH, TEXT_SCALE_HEIGHT,
						iconWidth, iconWidth, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT,
						scaleColor[0], scaleColor[1], scaleColor[2], scaleColor[3]
						);
				RenderSystem.disableBlend();
				
				// Meter
				final int meterXOffset = x + iconWidth + 1;
				final int meterYOffset = y + (height - meterHeight) / 2;
				final int meterPixels = Math.min(meterBarWidth, (int) (((float) weight / (float) maxWeight) * meterBarWidth));
				RenderFuncs.drawRect(matrixStackIn,
						meterXOffset + (meterWidth-meterBarWidth)/2, y + (height - meterBarHeight)/2,
						meterXOffset + meterWidth - (meterWidth-meterBarWidth)/2, y + (height + meterBarHeight)/2,
						0xFF808080);
				RenderFuncs.drawRect(matrixStackIn,
						meterXOffset + (meterWidth-meterBarWidth)/2, y + (height - meterBarHeight)/2,
						meterXOffset + (meterWidth-meterBarWidth)/2 + meterPixels, y + (height + meterBarHeight)/2,
						this.getMeterColor(weight, maxWeight));
				
				mc.getTextureManager().bindTexture(TEXT_UTILS);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, meterXOffset, meterYOffset,
						TEXT_GUAGE_HOFFSET, TEXT_GUAGE_VOFFSET, TEXT_GUAGE_WIDTH, TEXT_GUAGE_HEIGHT,
						meterWidth, meterHeight, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT
						);
				
				if (this.isHovered()) {
					matrixStackIn.push();
					matrixStackIn.translate(0, 0, 100);
					this.renderToolTip(matrixStackIn, mouseX, mouseY);
					matrixStackIn.pop();
				}
			}
			
			protected int getScaleIconColor(int weight, int maxWeight) {
				return weight > maxWeight ? 0xFFFF0000 : 0xFF202020;
			}
			
			protected int getMeterColor(int weight, int maxWeight) {
				if (weight == maxWeight) {
					return 0xFFCCCC40;
				}
				
				if (weight < maxWeight) {
					return 0xFF3366FF;
				} else {
					return 0xFFFF0000;
				}
			}
		}
		
		protected static class PatternIcon extends Widget {

			private final SpellGui<?> gui;
			
			public PatternIcon(SpellGui<?> gui, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
			}
			
			@Override
			public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
				final SpellCraftPattern pattern = gui.getContainer().getCraftPattern();
				if (pattern != null) {
					List<ITextComponent> tooltip = new ArrayList<>(4);
					tooltip.add(pattern.getName());
					tooltip = pattern.addDescription(tooltip);
					gui.func_243308_b(matrixStackIn, tooltip, mouseX, mouseY);
				}
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				
				// Background
				mc.getTextureManager().bindTexture(TEXT_UTILS);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, 
						TEX_PATTERN_HOFFSET, TEX_PATTERN_VOFFSET, TEX_PATTERN_WIDTH, TEX_PATTERN_HEIGHT,
						width, height,
						TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT
						);
				
				final SpellCraftPattern pattern = gui.getContainer().getCraftPattern();
				if (pattern != null) {
					@Nullable ISpellCraftPatternRenderer renderer = ISpellCraftPatternRenderer.GetRenderer(pattern);
					if (renderer != null) {
						matrixStackIn.push();
						matrixStackIn.translate(x + 1, y + 1, 0);
						renderer.drawPatternIconInGui(matrixStackIn, pattern, this.width-2, this.height-2, 1f, 1f, 1f, 1f);
						matrixStackIn.pop();
					}
					
					if (this.isHovered()) {
						matrixStackIn.push();
						matrixStackIn.translate(0, 0, 100);
						this.renderToolTip(matrixStackIn, mouseX, mouseY);
						matrixStackIn.pop();
					}
				}
			}
		}
		
		private static class SpellPartSegment extends FixedWidget {

			private final SpellGui<?> gui;
			private final SpellPartSummary part;
			
			private final List<ITextComponent> tooltip;
			private final int color;
			private final @Nullable IHoverHandler onHover;
			
			private boolean wasHovered;
			
			public SpellPartSegment(SpellGui<?> gui, IHoverHandler onHover, SpellPartSummary part, int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				this.gui = gui;
				this.onHover = onHover;
				this.part = part;
				
				this.tooltip = new ArrayList<>(4);
				if (part.isError()) {
					tooltip.add(new StringTextComponent("Error"));
					color = 0xFFFF0000;
				} else if (part.isShape()) {
					color = 0xFFCCCC44;
					tooltip.add(new StringTextComponent("Shape"));
				} else {
					color = part.getEffect().getElement().getColor();
					tooltip.add(new StringTextComponent("Effect"));
				}
			}
			
			@Override
			public void renderToolTip(MatrixStack matrixStackIn, int mouseX, int mouseY) {
				if (tooltip != null) {
					gui.func_243308_b(matrixStackIn, tooltip, mouseX, mouseY);
				}
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				RenderFuncs.drawRect(matrixStackIn, x, y, x + width, y + height, 0xFF404040);
				RenderFuncs.drawRect(matrixStackIn, x + 1, y + 1, x + width - 1, y + height - 1, color);

				final int iconHeight = height;
				final int iconWidth = iconHeight*2;
				matrixStackIn.push();
				matrixStackIn.translate(x + (width-iconWidth)/2, y, 0);
				{
					if (part.getAttributes().elementalBoost) {
						gui.drawElementalBoost(matrixStackIn, iconWidth, iconHeight, part.getEffect().getElement());
					} else if (part.getAttributes().elementalInterference) {
						gui.drawElementalPenalty(matrixStackIn, iconWidth, iconHeight, part.getEffect().getElement());
					}
				}
				matrixStackIn.pop();
				
				if (this.isHovered()) {
					RenderFuncs.drawRect(matrixStackIn, x, y, x + width, y + height, 0x20FFFFFF);
					
					if (onHover != null) {
						onHover.onHover(part, matrixStackIn, mouseX, mouseY);
					} else {
						this.renderToolTip(matrixStackIn, mouseX, mouseY);
					}
				} else if (this.wasHovered) {
					if (onHover != null) {
						onHover.onHover(null, matrixStackIn, mouseX, mouseY);
					}
				}
				
				wasHovered = this.isHovered();
			}
			
			@Override
			public boolean mouseClicked(double mouseX, double mouseY, int button) {
				return false;
			}
		}
		
		protected static class SpellPartBar extends com.smanzana.nostrummagica.client.gui.widget.ParentWidget {
			
			public static interface IHoverHandler {
				public void onHover(@Nullable SpellPartSummary summary, MatrixStack matrixStackIn, int mouseX, int mouseY);
			}
			
			private final SpellGui<?> gui;
			private final List<SpellPartSegment> bars;
			private final Vector3i[] slots;
			private final int slotWidth;
			private final @Nullable IHoverHandler onHover;
			
			public SpellPartBar(SpellGui<?> gui, Vector3i[] slots, int slotWidth, @Nullable IHoverHandler onHover) {
				super(gui.getGuiLeft(), gui.guiTop, gui.xSize, gui.ySize, StringTextComponent.EMPTY);
				this.gui = gui;
				this.bars = new ArrayList<>(slots.length);
				this.slots = slots;
				this.slotWidth = slotWidth;
				this.onHover = onHover;
				
				gui.getContainer().addListener((spell) -> {
					this.refreshTo(gui.getContainer().parts);
				});
			}
			
			public void refreshTo(List<SpellPartSummary> parts) {
				this.clearChildren();
				for (SpellPartSummary part : parts) {
					final int startX;
					final int endX;
					final int y;
					final int barHeight;
					
					final Vector3i startSlot = slots[part.getStartIdx()];
					final Vector3i endSlot = slots[part.getLastIdx()];
					startX = startSlot.getX();
					endX = endSlot.getX() + slotWidth;
					y = startSlot.getY();
					barHeight = 4;
					
					SpellPartSegment segment = new SpellPartSegment(gui, onHover, part, gui.getGuiLeft() + startX, gui.getGuiTop() + y, (endX - startX), barHeight);
					bars.add(segment);
					this.addChild(segment);
				}
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				; // INVISIBLE
			}
		}
		
		public static class InfoPanel extends ParentWidget {
			
			public static interface InfoPanelContent {
				public void render(MatrixStack matrixStackIn, int width, int height, float partialTicks);
			}
			
			protected float red;
			protected float green;
			protected float blue;
			protected float alpha;
			
			protected InfoPanelContent content;
			
			public InfoPanel(int x, int y, int width, int height) {
				super(x, y, width, height, StringTextComponent.EMPTY);
				color(0xFFFFFFFF);
			}
			
			public InfoPanel color(int color) {
				float[] colors = ColorUtil.ARGBToColor(color);
				return color(colors[0], colors[1], colors[2], colors[3]);
			}
			
			public InfoPanel color(float red, float green, float blue, float alpha) {
				this.red = red;
				this.green = green;
				this.blue = blue;
				this.alpha = alpha;
				return this;
			}
			
			public InfoPanel setContent(InfoPanelContent content) {
				this.content = content;
				return this;
			}
			
			protected void renderBackground(MatrixStack matrixStackIn) {
				Minecraft.getInstance().getTextureManager().bindTexture(TEXT_UTILS);
				
				// Note: hardcoding border size to be 4
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, TEX_INFPANEL_BORDER_TL_HOFFSET, TEX_INFPANEL_BORDER_TL_VOFFSET, TEX_INFPANEL_BORDER_TL_WIDTH, TEX_INFPANEL_BORDER_TL_HEIGHT, 4, 4, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, width-4, 0, TEX_INFPANEL_BORDER_TR_HOFFSET, TEX_INFPANEL_BORDER_TR_VOFFSET, TEX_INFPANEL_BORDER_TR_WIDTH, TEX_INFPANEL_BORDER_TR_HEIGHT, 4, 4, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, height-4, TEX_INFPANEL_BORDER_BL_HOFFSET, TEX_INFPANEL_BORDER_BL_VOFFSET, TEX_INFPANEL_BORDER_BL_WIDTH, TEX_INFPANEL_BORDER_BL_HEIGHT, 4, 4, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, width-4, height-4, TEX_INFPANEL_BORDER_BR_HOFFSET, TEX_INFPANEL_BORDER_BR_VOFFSET, TEX_INFPANEL_BORDER_BR_WIDTH, TEX_INFPANEL_BORDER_BR_HEIGHT, 4, 4, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 4, 0, TEX_INFPANEL_BORDER_TOP_HOFFSET, TEX_INFPANEL_BORDER_TOP_VOFFSET, TEX_INFPANEL_BORDER_TOP_WIDTH, TEX_INFPANEL_BORDER_TOP_HEIGHT, width-8, 4, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 4, height-4, TEX_INFPANEL_BORDER_BOTTOM_HOFFSET, TEX_INFPANEL_BORDER_BOTTOM_VOFFSET, TEX_INFPANEL_BORDER_BOTTOM_WIDTH, TEX_INFPANEL_BORDER_BOTTOM_HEIGHT, width-8, 4, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 4, TEX_INFPANEL_BORDER_LEFT_HOFFSET, TEX_INFPANEL_BORDER_LEFT_VOFFSET, TEX_INFPANEL_BORDER_LEFT_WIDTH, TEX_INFPANEL_BORDER_LEFT_HEIGHT, 4, height-8, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, width-4, 4, TEX_INFPANEL_BORDER_RIGHT_HOFFSET, TEX_INFPANEL_BORDER_RIGHT_VOFFSET, TEX_INFPANEL_BORDER_RIGHT_WIDTH, TEX_INFPANEL_BORDER_RIGHT_HEIGHT, 4, height-8, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 4, 4, TEX_INFPANEL_CENTER_HOFFSET, TEX_INFPANEL_CENTER_VOFFSET, TEX_INFPANEL_CENTER_WIDTH, TEX_INFPANEL_CENTER_HEIGHT, width-8, height-8, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT, red, green, blue, alpha);
			}
			
			@Override
			public void renderButton(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				matrixStackIn.push();
				matrixStackIn.translate(x, y, 0);
				renderBackground(matrixStackIn);
				
				if (this.content != null) {
					// Find how much to offset tby based on children
					final int margin = 4;
					int yOffset = 0;
					for (Widget child : this.children) {
						yOffset = Math.max(yOffset, (child.y + child.getHeightRealms()) - this.y);
					}
					
					matrixStackIn.push();
					matrixStackIn.translate(margin, 2 + yOffset, 0);
					
					content.render(matrixStackIn, width - (2 * margin), height - (yOffset + 2 * margin), partialTicks);
					
					matrixStackIn.pop();
				}
				matrixStackIn.pop();
			}
		}
		
		protected static final void drawScrollMessage(MatrixStack matrixStackIn, int width, int height, FontRenderer fonter) {
			final String message = "Insert Blank Scroll";
			final int msgWidth = fonter.getStringWidth(message);
			
			RenderFuncs.drawRect(matrixStackIn, -width/2, -height/2, width/2, height/2, 0xDD000000);
			fonter.drawString(matrixStackIn, message, -msgWidth / 2, -fonter.FONT_HEIGHT/2, 0xFFFFFFFF);
		}
		
		private T container;
		
		public SpellGui(T container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
		}
		
		@Override
		public void init() {
			super.init();
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			super.drawGuiContainerForegroundLayer(matrixStackIn, mouseX, mouseY);
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			;			
		}
		
		protected void iconButtonClicked(SpellIconButton button) {
			this.onIconSelected(button.value);
			container.validate();
		}
		
		protected abstract void onIconSelected(int icon);
		
		protected void submitButtonClicked(SubmitButton button) {
			this.onSubmit();
		}
		
		protected abstract void onSubmit();

		@Override
		public abstract List<Rectangle2d> getGuiExtraAreas();
		
		protected static boolean isValidChar(int codepoint) {
			return Character.isAlphabetic(codepoint) || Character.isDigit(codepoint) || Character.isSpaceChar(codepoint);
		}
		
		private static final Map<EAlteration, Map<EMagicElement, SpellAction>> actionCache = new HashMap<>();
		protected static @Nullable SpellAction getKnownActionForPart(SpellEffectPart part) {
			INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
			final boolean known = attr != null && attr.hasKnowledge(part.getElement(), part.getAlteration());
			if (!known) {
				return null;
			}
			
			Map<EMagicElement, SpellAction> map = actionCache.get(part.getAlteration());
			if (map == null) {
				map = new EnumMap<>(EMagicElement.class);
				actionCache.put(part.getAlteration(), map);
			}
			
			SpellAction action = map.get(part.getElement());
			if (action == null) {
				action = Spell.solveAction(part.getAlteration(), part.getElement(), 1);
				map.put(part.getElement(), action);
			}
			
			return action;
		}
		
		protected void drawAffectEntity(MatrixStack matrixStackIn, int width, int height, float[] color) {
			SpellComponentIcon.get(NostrumSpellShapes.AtFeet)
				.draw(matrixStackIn, 0, 0, width, height, color[0], color[1], color[2], color[3]);
		}
		
		protected void drawAffectBlock(MatrixStack matrixStackIn, int width, int height, float[] color) {
			SpellComponentIcon.get(NostrumSpellShapes.Proximity)
				.draw(matrixStackIn, 0, 0, width, height, color[0], color[1], color[2], color[3]);
		}
		
		protected void drawElementalBoost(MatrixStack matrixStackIn, int width, int height, EMagicElement element) {
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT_UTILS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					TEX_BOOST_HOFFSET, TEX_BOOST_VOFFSET, TEX_BOOST_WIDTH, TEX_BOOST_HEIGHT,
					width/2, height, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT);
			
			SpellComponentIcon.get(element)
				.draw(matrixStackIn, width/2, 0, width/2, height);
		}
		
		protected void drawElementalPenalty(MatrixStack matrixStackIn, int width, int height, EMagicElement element) {
			Minecraft.getInstance().getTextureManager().bindTexture(TEXT_UTILS);
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0,
					TEX_PENALTY_HOFFSET, TEX_PENALTY_VOFFSET, TEX_PENALTY_WIDTH, TEX_PENALTY_HEIGHT,
					width/2, height, TEXT_UTILS_WIDTH, TEXT_UTILS_HEIGHT);
			
			SpellComponentIcon.get(element)
				.draw(matrixStackIn, width/2, 0, width/2, height);
		}
		
		protected final void renderSpellPanel(MatrixStack matrixStackIn, int width, int height, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			final FontRenderer fontRenderer = mc.fontRenderer;
			final T container = this.getContainer();
			final String summaryText = "Summary";
			final int summaryTextWidth = fontRenderer.getStringWidth(summaryText);
			fontRenderer.drawString(matrixStackIn, summaryText, (width - summaryTextWidth)/2, 0, 0xFF000000);
			matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
			
			matrixStackIn.scale(.5f, .5f, 1f);
			
			// Mana cost
			fontRenderer.drawString(matrixStackIn, "Mana Cost: " + container.getManaCost(), 0, 0, 0xFF000000);
			matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
			
			// Weight
			fontRenderer.drawString(matrixStackIn, "Weight: " + container.getCurrentWeight(), 0, 0, 0xFF000000);
			matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
			
			// Reagents
			if (!container.getReagentStrings().isEmpty()) {
				fontRenderer.drawString(matrixStackIn, "Reagents:", 0, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				for (ITextComponent string : container.getReagentStrings()) {
					fontRenderer.func_243248_b(matrixStackIn, string, 4, 0, 0xFF000000); //drawTextComponent()
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				}
			}
		}
		
		protected void renderModifierPanel(@Nullable ISpellCraftModifier modifier, MatrixStack matrixStackIn, int width, int height, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			final FontRenderer fontRenderer = mc.fontRenderer;
			final String modifierText = "Modifier";
			final int modifierTextWidth = fontRenderer.getStringWidth(modifierText);
			fontRenderer.drawString(matrixStackIn, modifierText, ((width-8) - modifierTextWidth)/2, 0, 0xFF000000);
			matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
			
			matrixStackIn.scale(.5f, .5f, 1f);
			
			if (modifier != null) {
				List<ITextComponent> lines = new ArrayList<>(4);
				lines = modifier.getDetails(lines);
				for (ITextComponent line : lines) {
					fontRenderer.func_243248_b(matrixStackIn, line, 0, 0, 0xFF000000);
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT + 1, 0);
				}
			} else {
				fontRenderer.drawString(matrixStackIn, "No Slot Modifier", 0, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
			}
		}
		
		protected void renderSpellPartPanel(SpellPartSummary part, MatrixStack matrixStackIn, int width, int height, float partialTicks) {
			final Minecraft mc = Minecraft.getInstance();
			final FontRenderer fontRenderer = mc.fontRenderer;
			final String titleText = part.isError() ? "Error" : part.isShape() ? "Shape" : "Effect";
			final int titleTextWidth = fontRenderer.getStringWidth(titleText);
			matrixStackIn.push();
			matrixStackIn.scale(.75f, .75f, 1f);
			int subWidth = (int) (width / (.75f));
			
			fontRenderer.drawString(matrixStackIn, titleText, (subWidth - titleTextWidth)/2, 0, 0xFF000000);
			matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
			
			matrixStackIn.scale((.5f/.75f), (.5f/.75f), 1f);
			subWidth = width * 2;
			if (!part.isError()) {
				
				// Mana cost
				fontRenderer.drawString(matrixStackIn, "Mana Cost: " + part.getMana(), 0, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				
				// Weight
				fontRenderer.drawString(matrixStackIn, "Weight: " + part.getWeight(), 0, 0, 0xFF000000);
				matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
				
				if (!part.isShape()) {
					final SpellEffectPart effect = part.getEffect();
					
					// Potency
					{
						final int color = (effect.getPotency() > 1 ? 0xFF44EE66 : 
							effect.getPotency() < 1f ? 0xFFCC4422
									: 0xFF000000);
						fontRenderer.drawString(matrixStackIn, String.format("Potency: %.0f%%", effect.getPotency() * 100f), 0, 0, color);
						matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					}
					
					
					final String name;
					final String desc;
					@Nullable SpellAction action = SpellGui.getKnownActionForPart(effect);
					if (action == null) {
						name = "Unknown Effect";
						desc = "You haven't seen this effect before. Make a spell with it to find out what it does!";
					} else {
						final String suffix = effect.getElementCount() <= 1 ? ""
								: effect.getElementCount() <= 2 ? " II"
								: " III";
						
						name = action.getName().getString() + suffix;
						desc = action.getDescription().getString();
					}
					
					int len = fontRenderer.getStringWidth(name);
					fontRenderer.drawString(matrixStackIn, name,
							(subWidth - len) / 2,
							0,
							0xFFFFFFFF);
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					
					if (action != null) {
						final SpellActionProperties props = action.getProperties();
						final int iconWidth = 12;
						final int iconHeight = 12;
						matrixStackIn.push();
						matrixStackIn.translate(subWidth / 2, 0, 0);
						matrixStackIn.translate(-(4 + iconWidth), 0, 0);
						
						float color[] = {1f, 1f, 1f, 1f};
						if (!props.affectsEntity) {
							color = new float[] {.3f, .3f, .3f, .4f};
						}
						drawAffectEntity(matrixStackIn, iconWidth, iconHeight, color);
						
						matrixStackIn.translate(12 + 4, 0, 0);
						if (props.affectsBlock) {
							color = new float[] {1f, 1f, 1f, 1f};
						} else {
							color = new float[] {.3f, .3f, .3f, .4f};
						}
						drawAffectBlock(matrixStackIn, iconWidth, iconHeight, color);
						matrixStackIn.pop();
						
						matrixStackIn.translate(0, iconHeight + 2, 0);
					}
					
					int yUsed = RenderFuncs.drawSplitString(matrixStackIn, fontRenderer, desc,
							0,
							0,
							subWidth,
							0xFFA0A0A0);
					matrixStackIn.translate(0, yUsed, 0);
				} else {
					final SpellShapePart shape = part.getShape();
					final String name;
					final String desc;
					name = shape.getShape().getDisplayName().getString();
					desc = shape.getShape().getDescription().getString();
					
					int len = fontRenderer.getStringWidth(name);
					fontRenderer.drawString(matrixStackIn, name,
							(subWidth - len) / 2,
							0,
							0xFFFFFFFF);
					matrixStackIn.translate(0, fontRenderer.FONT_HEIGHT, 0);
					
					{
						final SpellShapeAttributes props = shape.getShape().getAttributes(shape.getProperties());
						final int iconWidth = 12;
						final int iconHeight = 12;
						matrixStackIn.push();
						matrixStackIn.translate(subWidth / 2, 0, 0);
						matrixStackIn.translate(-(4 + iconWidth), 0, 0);
						
						float color[] = {1f, 1f, 1f, 1f};
						if (!props.selectsEntities) {
							color = new float[] {.3f, .3f, .3f, .4f};
						}
						drawAffectEntity(matrixStackIn, iconWidth, iconHeight, color);
						
						matrixStackIn.translate(12 + 4, 0, 0);
						if (props.selectsBlocks) {
							color = new float[] {1f, 1f, 1f, 1f};
						} else {
							color = new float[] {.3f, .3f, .3f, .4f};
						}
						drawAffectBlock(matrixStackIn, iconWidth, iconHeight, color);
						matrixStackIn.pop();
						
						matrixStackIn.translate(0, iconHeight + 2, 0);
					}
					
					int yUsed = RenderFuncs.drawSplitString(matrixStackIn, fontRenderer, desc,
							0,
							0,
							subWidth,
							0xFFA0A0A0);
					matrixStackIn.translate(0, yUsed, 0);
				}
			}
			matrixStackIn.pop();
		}
	}
}