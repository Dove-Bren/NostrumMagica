package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.ISpellCraftPatternRenderer;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.crafting.ISpellCraftingInventory;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.utils.ColorUtil;
import com.smanzana.nostrummagica.utils.RenderFuncs;

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
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class SpellCreationGui {
	
	public static final int MaxNameLength = 20;
	
	public static abstract class SpellCreationContainer extends Container {
		
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
				
				boolean trigger = SpellRune.isTrigger(stack);
				if (!trigger && !SpellRune.isPackedShape(stack))
					return false;
				
				return (prev != null || trigger);
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
		
		
		protected boolean hasScroll; // has an acceptable scroll
		protected boolean spellValid; // grammar checks out
		protected List<ITextComponent> spellErrorStrings; // Updated on validate(); what's wrong?
		protected List<ITextComponent> reagentStrings; // Updated on validate; what reagents will be used. Only filled if successful
		protected int lastManaCost;
		protected int lastWeight;
		
		public SpellCreationContainer(ContainerType<? extends SpellCreationContainer> type, int windowId, PlayerEntity crafter, PlayerInventory playerInv, ISpellCraftingInventory tableInventory, BlockPos tablePos) {
			super(type, windowId);
			this.inventory = tableInventory;
			this.player = crafter;
			this.pos = tablePos;
			this.context = new SpellCraftContext(crafter, crafter.world, pos);
			
			spellErrorStrings = new LinkedList<>();
			
			// Dont auto call this; let children, so that they can set up things they need to first.
			//validate();
		}
		
		public abstract String getName();
		
		public abstract int getSpellIcon();
		
		public abstract @Nullable SpellCraftPattern getCraftPattern();
		
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
			ItemStack ret = super.slotClick(slotId, dragType, clickTypeIn, player);
			
			int unused; // is this useful?
			checkScroll();
			
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
							SpellComponentWrapper wrapper = SpellRune.toComponentWrapper(cur);
							boolean add = false;
							if (wrapper.isTrigger()) {
								// Can always add triggers
								add = true;
							} else if (SpellRune.isPackedShape(cur)) {
								// Must have a trigger in first slot already
								if (!inventory.getRuneSlotContents(0).isEmpty())
									add = true;
							}
							
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
			
			return 0;
		}
		
		protected void validate() {
			if (spellErrorStrings == null)
				spellErrorStrings = new LinkedList<>();
			if (reagentStrings == null)
				reagentStrings = new LinkedList<>();
			
			checkScroll();
			if (this.hasScroll) {
				Spell spell = makeSpell();
				spellValid = (spell != null);
			} else {
				spellValid = false;
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
			Spell spell = craftSpell(getCraftContext(), null, name, iconIdx, this.inventory, this.player, this.spellErrorStrings, this.reagentStrings, clear);
			
			if (spell == null)
				return null;
			
			this.lastManaCost = spell.getManaCost();
			this.lastWeight = spell.getWeight();
			
			if (this.lastWeight > this.getMaxWeight()) {
				this.spellErrorStrings.add(new StringTextComponent("Too much weight"));
				return null;
			}
			
			if (clear)
				this.inventory.clearSpellBoard();
			
			return spell;
		}
		
		public static Spell craftSpell(SpellCraftContext context, @Nullable SpellCraftPattern pattern, String name, int iconIdx, ISpellCraftingInventory inventory, PlayerEntity crafter,
				List<ITextComponent> spellErrorStrings, List<ITextComponent> reagentStrings,
				boolean deductReagents) {
			boolean fail = false;
			//INostrumMagic attr = NostrumMagica.getMagicWrapper(crafter);
			boolean locked = SpellCrafting.CanCraftSpells(crafter);
			spellErrorStrings.clear();
			reagentStrings.clear();
			
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
			if (!SpellCrafting.CheckForValidRunes(inventory, inventory.getRuneSlotStartingIndex(), inventory.getRuneSlotCount(), rawSpellErrors)) {
				// Dump raw errors into output strings and return
				for (String error : rawSpellErrors) {
					spellErrorStrings.add(new StringTextComponent(error));
				}
				return null;
			}
			
			// Stop here if already failing and avoid creating the spell
			if (fail) {
				return null;
			}
			
			// Actually make spell
			Spell spell = SpellCrafting.CreateSpellFromRunes(context, pattern, name, inventory, inventory.getRuneSlotStartingIndex(), inventory.getRuneSlotCount());
			
			// Do reagent check
			Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
			for (ReagentType type : reagents.keySet()) {
				if (type == null)
					continue;
				Integer count = reagents.get(type);
				if (count == null)
					continue;
				
				int left = takeReagent(crafter, inventory, type, count, false);
				if (left != 0) {
					spellErrorStrings.add(new StringTextComponent("Need " + left + " more " + type.prettyName()));
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
					
					int left = takeReagent(crafter, inventory, type, count, true);
					if (left != 0) {
						System.out.println("Couldn't take all " + type.name());
						spellErrorStrings.add(new StringTextComponent("Need " + left + " more " + type.prettyName()));
						return null;
					}
					
				}
			}
			
			return spell;
		}
		
		// if take, actually removes. Otherwise, just checks
		// returns amount needed still. 0 means all that were needed are there
		private static int takeReagent(PlayerEntity player, ISpellCraftingInventory craftingInventory, ReagentType type, int count, boolean take) {
			final IInventory inventory = player.inventory;
			for (int i = 0; i < inventory.getSizeInventory(); i++) {
				@Nonnull ItemStack stack = inventory.getStackInSlot(i);
				if (stack.isEmpty())
					continue;
				
				if (ReagentItem.FindType(stack) == type) {
					if (stack.getCount() > count) {
						if (take)
							inventory.decrStackSize(i, count);
						count = 0;
					} else {
						count -= stack.getCount();
						if (take)
							inventory.setInventorySlotContents(i, ItemStack.EMPTY);
					}
					
					if (count == 0)
						break;
				}
			}
			
			return count;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static abstract class SpellGui<T extends SpellCreationContainer> extends AutoGuiContainer<T> {
		
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
					pattern.addDescription(tooltip);
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
						renderer.drawPatternIcon(matrixStackIn, pattern, gui.getContainer().getCraftContext(), this.width-2, this.height-2, 1f, 1f, 1f, 1f);
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
		
		protected static boolean isValidChar(int codepoint) {
			return Character.isAlphabetic(codepoint) || Character.isDigit(codepoint) || Character.isSpaceChar(codepoint);
		}

		public abstract List<Rectangle2d> getGuiExtraAreas();
	}
}