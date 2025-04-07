package com.smanzana.nostrummagica.client.gui.container;

import java.util.List;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.item.SpellTomePage;
import com.smanzana.nostrummagica.item.equipment.CasterWandItem;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.ModifyMessage;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellComponentWrapper;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.tile.ModificationTableTileEntity;
import com.smanzana.nostrummagica.util.ContainerUtil;
import com.smanzana.nostrummagica.util.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.util.RenderFuncs;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

public class ModificationTableGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/modification_table.png");
	
	private static final int GUI_WIDTH = 202;
	private static final int GUI_HEIGHT = 221;
	private static final int PLAYER_INV_HOFFSET = 23;
	private static final int PLAYER_INV_VOFFSET = 140;
	private static final int SLOT_MAIN_HOFFSET = 95;
	private static final int SLOT_MAIN_VOFFSET = 24;
	private static final int SLOT_INPUT_HOFFSET = 155;
	private static final int SLOT_INPUT_VOFFSET = 111;
	
	//23, 136
	
	private static final int PANEL_HOFFSET = 3;
	private static final int PANEL_VOFFSET = 49;
	private static final int PANEL_WIDTH = 196;
	//private static final int PANEL_HEIGHT = 82;
	
	private static final int BUTTON_TEXT_HOFFSET = 202;
	private static final int BUTTON_TEXT_VOFFSET = 47;
	private static final int BUTTON_WIDTH = 10;
	private static final int BUTTON_HEIGHT = 10;
	private static final int LARGE_BUTTON_WIDTH = 20;
	private static final int LARGE_BUTTON_HEIGHT = 10;
	
	private static final int SUBMIT_HOFFSET = 175;
	private static final int SUBMIT_VOFFSET = 116;
	private static final int SUBMIT_WIDTH = 18;
	private static final int SUBMIT_HEIGHT = 10;
	private static final int SUBMIT_TEXT_HOFFSET = 0;
	private static final int SUBMIT_TEXT_VOFFSET = 221;
	
	public static class ModificationTableContainer extends AbstractContainerMenu {
		
		public static final String ID = "modification_table";
		
		// Kept just to report to server which TE is doing crafting
		protected BlockPos pos;
		protected Player player;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected ModificationTableTileEntity inventory;
		protected boolean isValid; // has input needed to combine
		protected InputSlot inputSlot; // Input slot. Changes what it can accept
		protected int floatIndex = 0;
		protected boolean boolIndex = false;
		protected int modIndex = 0;
		
		// Was going to pull these into an enum, but would rather abstract out all of this into IModifiableItem or such interfaces
		protected boolean scrollMode;
		protected boolean tomeMode;
		protected boolean wandMode;
		
		protected boolean hasBool;
		protected boolean hasFloat;
		protected SpellComponentWrapper component;
		
		public ModificationTableContainer(int windowId, Player player, Container playerInv, ModificationTableTileEntity tableInventory, BlockPos pos) {
			super(NostrumContainers.ModificationTable, windowId);
			this.inventory = tableInventory;
			this.player = player;
			this.pos = pos;
			this.scrollMode = false;
			
			this.addSlot(new Slot(inventory, 0, SLOT_MAIN_HOFFSET, SLOT_MAIN_VOFFSET) {
				
				@Override
				public boolean mayPlace(@Nonnull ItemStack stack) {
					return container.canPlaceItem(this.index, stack);
				}
				
				@Override
				public void set(@Nonnull ItemStack stack) {
					super.set(stack);
					floatIndex = 0;
					
					if (!stack.isEmpty() && stack.getItem() instanceof SpellScroll) {
						// Shouldn't be null since we disallow null in the slot... but let's just be safe. This is UI code.
						Spell spell = SpellScroll.GetSpell(stack);
						if (spell != null) {
							floatIndex = spell.getIconIndex();							
						}
					}
					
					validate();
				}
				
				@Override
				public void onTake(Player playerIn, ItemStack stack) {
					validate();
					floatIndex = 0;
					super.onTake(playerIn, stack);
				}
			});
			
			this.inputSlot = new InputSlot(this, inventory, 1, SLOT_INPUT_HOFFSET, SLOT_INPUT_VOFFSET);
			this.addSlot(inputSlot);
			
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlot(new Slot(playerInv, x + y * 9 + 9, PLAYER_INV_HOFFSET + (x * 18), PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlot(new Slot(playerInv, x, PLAYER_INV_HOFFSET + x * 18, 58 + (PLAYER_INV_VOFFSET)));
			}
			
			validate();
		}
		
		public static final ModificationTableContainer FromNetwork(int windowId, Inventory playerInv, FriendlyByteBuf buf) {
			return new ModificationTableContainer(windowId, playerInv.player, playerInv, ContainerUtil.GetPackedTE(buf), buf.readBlockPos());
		}
		
		public static final IPackedContainerProvider Make(ModificationTableTileEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new ModificationTableContainer(windowId, player, playerInv, table, table.getBlockPos());
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
				buffer.writeBlockPos(table.getBlockPos());
			});
		}
		
		@Override
		public @Nonnull ItemStack quickMoveStack(Player playerIn, int fromSlot) {
			Slot slot = (Slot) this.slots.get(fromSlot);
			
			if (slot != null && slot.hasItem()) {
				ItemStack cur = slot.getItem();
				
				if (slot.container == this.inventory) {
					// Trying to take our items
					if (playerIn.getInventory().add(cur)) {
						slot.set(ItemStack.EMPTY);
						slot.onTake(playerIn, cur);
					}
				} else {
					// Trying to add an item
					Slot mainSlot = this.getSlot(0);
					if (!mainSlot.hasItem()) {
						if (mainSlot.mayPlace(cur))
							mainSlot.set(cur.split(1));
					} else if (!inputSlot.hasItem()) {
						if (inputSlot.mayPlace(cur))
							inputSlot.set(cur.split(1));
					}
				}
				
				if (cur.isEmpty() || cur.getCount() <= 0) {
					slot.set(ItemStack.EMPTY);
				}
			}
			
			return ItemStack.EMPTY;
		}
		
		@Override
		public boolean canDragTo(Slot slotIn) {
			return slotIn.container != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean stillValid(Player playerIn) {
			return true;
		}
		
		public void validate() {
			modIndex++;
			if (this.inventory.getMainSlot().isEmpty()) {
				this.isValid = false;
				this.scrollMode = false;
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellTome) {
				this.scrollMode = false;
				this.wandMode = false;
				this.tomeMode = true;
				ItemStack inputItem = inputSlot.getItem();
				isValid = (SpellTome.getModifications(inventory.getMainSlot()) > 0);
				if (!isValid || inputItem.isEmpty() || !(inputItem.getItem() instanceof SpellTomePage)) {
					this.isValid = false;
				}
				
				inputSlot.setRequired(ItemStack.EMPTY);
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellScroll) {
				boolean hasChange = false;
				ItemStack inputItem = inputSlot.getItem();
				ItemStack required = new ItemStack(Items.RED_DYE);
				Ingredient realRequired = Ingredient.of(Tags.Items.DYES);
				
				
				inputSlot.setRequired(required);
				isValid = (SpellScroll.GetSpell(inventory.getMainSlot()) != null);
				
				if (isValid) { // and therefore, spell is not null
					int cur = SpellScroll.GetSpell(inventory.getMainSlot()).getIconIndex();
					hasChange = cur != (int) this.floatIndex;
				}
				
				if (!isValid || !hasChange || inputItem.isEmpty() || !(realRequired.test(inputItem))) {
					this.isValid = false;
				}
				
				hasBool = false;
				hasFloat = true;
				
				scrollMode = true;
				this.wandMode = false;
				this.tomeMode = false;
				
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof CasterWandItem) {
				final ItemStack stack = inventory.getMainSlot();
				final ItemStack inputItem = inputSlot.getItem();
				
				scrollMode = false;
				wandMode = true;
				tomeMode = false;
				
				hasBool = false;
				hasFloat = false;
				
				isValid = false;
				if (inputItem.isEmpty()) {
					// Allow removal if we actually have a spell
					isValid = CasterWandItem.GetSpell(stack) != null;
				} else if (inputItem.getItem() instanceof SpellScroll
						&& SpellScroll.GetSpell(inputItem) != null) {
					isValid = CasterWandItem.CanStoreSpell(stack, SpellScroll.GetSpell(inputItem));
				}
				
				// Show scroll if nothing's in the wand
				if (CasterWandItem.GetSpell(stack) == null) {
					inputSlot.setRequired(new ItemStack(NostrumItems.spellScroll));
				} else {
					inputSlot.setRequired(ItemStack.EMPTY);
				}
				
				return;
			}
		}
		
		public void setMain(ItemStack item) {
			this.inventory.setItem(0, item);
			isValid = false;
			validate();
		}
		
		public void setInput(ItemStack item) {
			this.inventory.setItem(1, item);
			isValid = false;
			validate();
		}

	}
	
	@OnlyIn(Dist.CLIENT)
	public static class ModificationGui extends AutoGuiContainer<ModificationTableContainer> {

		private ModificationTableContainer container;
		private int localModIndex = 0;
		
		private SpellCastSummary summary;
		private @Nonnull ItemStack pageShadow;
		private NonNullList<ItemStack> shadows;
		
		private Button submitButton;
		
		public ModificationGui(ModificationTableContainer container, Inventory playerInv, Component name) {
			super(container, playerInv, name);
			this.container = container;
			this.imageWidth = GUI_WIDTH;
			this.imageHeight = GUI_HEIGHT;
			pageShadow = new ItemStack(NostrumItems.spellTomePage);
			shadows = NonNullList.create();
			shadows.add(new ItemStack(NostrumItems.spellTomeNovice)); // hasto be index 0
			shadows.add(new ItemStack(NostrumItems.spellScroll)); // has to be index 1
			shadows.add(new ItemStack(NostrumItems.casterWand));
		}
		
		@Override
		public void init() {
			super.init();
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			submitButton = new SubmitButton(
					horizontalMargin + SUBMIT_HOFFSET,
					verticalMargin + SUBMIT_VOFFSET, this);
			this.refreshButtons();
			this.localModIndex = container.modIndex;
		}
		
		@Override
		protected void renderBg(PoseStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			if (localModIndex != container.modIndex) {
				this.refreshButtons();
				this.localModIndex = container.modIndex;
			}
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			
			RenderSystem.setShaderTexture(0, TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			if (container.scrollMode) {
				
			} else if (container.tomeMode) {
				// Draw tome info
				int x, y;
				y = verticalMargin + PANEL_VOFFSET + 10;
				ItemStack tome = container.inventory.getMainSlot();
				if (!tome.isEmpty()) {
					Component nameComp = tome.getHoverName();
					String name = nameComp == null ? null : nameComp.getString();
					if (name == null || name.length() == 0)
						name = "Spell Tome";
					int len = mc.font.width(name);
					x = horizontalMargin + PANEL_HOFFSET + (PANEL_WIDTH / 2) - (len / 2);
					mc.font.drawShadow(matrixStackIn, name, x, y, 0xFFFFFFFF);
					y += 20;
					
					x = horizontalMargin + PANEL_HOFFSET + 5;
					int valX = horizontalMargin + PANEL_HOFFSET + 75;
					int topY = y;
					
					if (summary == null || !container.isValid) {
						summary = new SpellCastSummary(0, 0);
						SpellTome.applyEnhancements(tome, summary, container.player);
					}
					
					// Efficiency
					mc.font.drawShadow(matrixStackIn, "Efficiency: ", x, y, 0xFFA0A0A0);
					mc.font.draw(matrixStackIn, String.format("%+03.0f%%", (summary.getEfficiency() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.font.lineHeight;
					
					// LMC
					mc.font.drawShadow(matrixStackIn, "Mana Cost: ", x, y, 0xFFA0A0A0);
					mc.font.draw(matrixStackIn, String.format("%+03.0f%%", (summary.getCostRate() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.font.lineHeight;
					
					// LRC
					mc.font.drawShadow(matrixStackIn, "Reagent Cost: ", x, y, 0xFFA0A0A0);
					mc.font.draw(matrixStackIn, String.format("%+03.0f%%", (summary.getReagentCost() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.font.lineHeight;
					
					// XP
					mc.font.drawShadow(matrixStackIn, "Bonus XP: ", x, y, 0xFFA0A0A0);
					mc.font.draw(matrixStackIn, String.format("%+03.0f%%", (summary.getXpRate() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.font.lineHeight;
					
					y = topY;
					x += 100;
					valX += 100;
					
					// Level
					mc.font.drawShadow(matrixStackIn, "Level: ", x, y, 0xFFA0A0A0);
					mc.font.draw(matrixStackIn, "" + SpellTome.getLevel(tome), valX, y, 0xFFFFFFFF);
					y += 2 + mc.font.lineHeight;
					
					// Modifications
					mc.font.drawShadow(matrixStackIn, "Modifications: ", x, y, 0xFFA0A0A0);
					mc.font.draw(matrixStackIn, "" + SpellTome.getModifications(tome), valX, y, 0xFFFFFFFF);
							
				}
			} else if (container.wandMode) {
				int x, y;
				y = verticalMargin + PANEL_VOFFSET + 10;
				final ItemStack wand = container.inventory.getMainSlot();
				final ItemStack input = container.inventory.getInputSlot();
				if (!wand.isEmpty()) {
					final List<Component> info;
					if (input.isEmpty()) {
						if (CasterWandItem.GetSpell(wand) == null) {
							info = TextUtils.GetTranslatedList("modification.caster_wand.intro");
						} else {
							info = TextUtils.GetTranslatedList("modification.caster_wand.remove");
						}
					} else if (input.getItem() instanceof SpellScroll && SpellScroll.GetSpell(input) != null) {
						final Spell scrollSpell = SpellScroll.GetSpell(input);
						if (CasterWandItem.GetSpell(wand) == null) {
							info = TextUtils.GetTranslatedList("modification.caster_wand.addspell", scrollSpell.getName());
						} else {
							info = TextUtils.GetTranslatedList("modification.caster_wand.replacespell", scrollSpell.getName());
						}
					} else {
						info = TextUtils.GetTranslatedList("modification.caster_wand.nospell");
					}
					
					x = horizontalMargin + PANEL_HOFFSET + 5;
					for (Component line : info) {
						mc.font.draw(matrixStackIn, line, x, y, 0xFFFFFFFF);
						y += mc.font.lineHeight + 1;
					}
				}
			}
			
			if (!container.inputSlot.hasItem()) {
				ItemStack shadow = container.inputSlot.required;
				if (!shadow.isEmpty()) {
					RenderFuncs.RenderGUIItem(shadow, matrixStackIn, 
						horizontalMargin + container.inputSlot.x,
						verticalMargin + container.inputSlot.y,
						-100);
				}
			}
			
			if (container.inventory.getMainSlot().isEmpty()) {
				ItemStack display;
//				if ((System.currentTimeMillis() / 1000) % 2 == 0) {
//					display = new ItemStack(SpellTome.instance());
//				}
				final int idx = Math.abs(((int) System.currentTimeMillis() / 1000) % shadows.size());
				display = shadows.get(idx);
				
				RenderFuncs.RenderGUIItem(display, matrixStackIn, 
						horizontalMargin + SLOT_MAIN_HOFFSET,
						verticalMargin + SLOT_MAIN_VOFFSET,
						-100);
				
				int color = 0x55FFFFFF;
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, 1);
				RenderFuncs.drawRect(matrixStackIn, 
						horizontalMargin + SLOT_MAIN_HOFFSET,
						verticalMargin + SLOT_MAIN_VOFFSET,
						horizontalMargin + SLOT_MAIN_HOFFSET + 16,
						verticalMargin + SLOT_MAIN_VOFFSET + 16,
						color);
				matrixStackIn.popPose();
			}
			
			if (!container.isValid) {
				int color = 0x55FFFFFF;
				if ((!container.inputSlot.required.isEmpty() && container.inputSlot.hasItem())
						|| (container.inputSlot.required.isEmpty() && container.inputSlot.hasItem()))
					color = 0x90FF5050;
				matrixStackIn.pushPose();
				matrixStackIn.translate(0, 0, 1);
				RenderFuncs.drawRect(matrixStackIn, 
						horizontalMargin + container.inputSlot.x,
						verticalMargin + container.inputSlot.y,
						horizontalMargin + container.inputSlot.x + 16,
						verticalMargin + container.inputSlot.y + 16,
						color);
				matrixStackIn.popPose();
			}
		}
		
		@Override
		protected void renderLabels(PoseStack matrixStackIn, int mouseX, int mouseY) {
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			if (container.isValid) {
				
				int submitX = horizontalMargin + SUBMIT_HOFFSET;
				int submitY = verticalMargin + SUBMIT_VOFFSET;
				
				if (mouseX >= submitX && mouseY >= submitY
						&& mouseX <= submitX + SUBMIT_WIDTH
						&& mouseY <= submitY + SUBMIT_HEIGHT) {
					RenderFuncs.drawRect(matrixStackIn, SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x20FFFFFF);
				}
			}
			
		}
		
		protected void onSubmitButton() {
			if (container.isValid) {
				if (container.scrollMode) {
					float fVal = (float) container.floatIndex;
					NetworkHandler.sendToServer(
							new ModifyMessage(container.pos, container.boolIndex, fVal));
					container.inventory.modify(container.boolIndex, fVal);
				} else {
					NetworkHandler.sendToServer(
							new ModifyMessage(container.pos, false, 0));
					container.inventory.modify(false, 0);
				}
			}
		}
		
		protected void onToggleButton(ToggleButton button) {
			container.boolIndex = ((ToggleButton) button).val;
			container.validate();
		}
		
		protected void onFloatButton(FloatButton button) {
			if (container.scrollMode) {
				container.floatIndex =((FloatButton) button).val;
			}
			
			container.validate();
		}
			
		protected void refreshButtons() {
			this.clearWidgets();
			
			int horizontalMargin = (width - imageWidth) / 2;
			int verticalMargin = (height - imageHeight) / 2;
			int y = verticalMargin + PANEL_VOFFSET + 33;
			int x = horizontalMargin + PANEL_HOFFSET + 50;
			
			if (!container.inventory.getMainSlot().isEmpty()) {
				if (container.hasBool) {
					this.addRenderableWidget(new ToggleButton(x, y, false, this));
					this.addRenderableWidget(new ToggleButton(x + 15, y, true, this));
					
					y += 25;
				}
				if (container.hasFloat) {
					if (container.scrollMode) {
						final int margin = 2;
						
						// more condensed
						x = horizontalMargin + PANEL_HOFFSET + margin;
						y = verticalMargin + PANEL_VOFFSET + margin;
						
						final int numHorizontal = (PANEL_WIDTH - (margin * 2)) / 16;
						
						for (int i = 0; i < SpellIcon.numIcons; i++) {
							FloatButton button = new FloatButton(x + ((i % numHorizontal) * 16), y + ((i / numHorizontal) * 16), i, i, this);
							
							this.addRenderableWidget(button);
						}
					}
				}
			}
			
			this.addRenderableWidget(submitButton);
		}
		
		private static class ToggleButton extends Button {

			private boolean val;
			private ModificationGui gui;
			
			public ToggleButton(int x, int y, boolean val, ModificationGui gui) {
				super(x, y, 200, 20, TextComponent.EMPTY, (b) -> {
					gui.onToggleButton((ToggleButton)b);
				});
				this.val = val;
				this.width = BUTTON_WIDTH;
				this.height = BUTTON_HEIGHT;
				this.gui = gui;
			}
			
			@Override
			public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
				if (visible) {
					
					float tint = 1f;
					RenderSystem.setShaderTexture(0, TEXT);
					if (parX >= this.x && parY >= this.y
							&& parX <= this.x + this.width
							&& parY <= this.y + this.height) {
						tint = .8f;
					}
					
					int x = 0;
					if (!val)
						x += 2 * BUTTON_WIDTH;
					if (val != gui.container.boolIndex)
						x += BUTTON_WIDTH;
					
					RenderFuncs.blit(matrixStackIn, this.x, this.y,
							BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET,
							this.width, this.height,
							tint, tint, tint, 1f);
				}
			}
		}
		
		private static class FloatButton extends Button {
			private int val;
			//protected float actualVal;
			private ModificationGui gui;
			
			public FloatButton(int x, int y, int val, float actual, ModificationGui gui) {
				super(x, y, 200, 20, TextComponent.EMPTY, (b) -> {
					gui.onFloatButton((FloatButton) b);
				});
				this.val = val;
				//this.actualVal = actual;
				this.width = 16;
				this.height = 16;
				this.gui = gui;
			}
			
			@Override
			public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
				if (visible) {
					
					// In scroll mode, float buttons are buttons that match spell icon idx
					final Minecraft mc = Minecraft.getInstance();
					if (gui.container.scrollMode) {
						// In scroll mode, we show the icon they can select
						float tint = 1f;
						RenderSystem.setShaderTexture(0, TEXT);
						if (parX >= this.x && parY >= this.y
								&& parX <= this.x + this.width
								&& parY <= this.y + this.height) {
							tint = .8f;
						}
						
						int x = 0;
						if (gui.container.floatIndex != this.val)
							x += LARGE_BUTTON_WIDTH;
						
						RenderFuncs.blit(matrixStackIn, this.x, this.y, this.width, this.height, BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET + BUTTON_HEIGHT,
								LARGE_BUTTON_WIDTH, LARGE_BUTTON_HEIGHT, 256, 256,
								tint, tint, tint, 1f);
						
						SpellIcon.get(this.val).render(mc, matrixStackIn, this.x + 2, this.y + 2, this.width - 4, this.height - 4, tint, tint, tint, 1f);
						
					}
				}
			}
		}
		
		private static class SubmitButton extends Button {
			
			private ModificationGui gui;
			
			public SubmitButton(int x, int y, ModificationGui gui) {
				super(x, y, 200, 20, TextComponent.EMPTY, (b) -> {
					gui.onSubmitButton();
				});
				this.width = LARGE_BUTTON_WIDTH;
				this.height = LARGE_BUTTON_HEIGHT;
				this.gui = gui;
			}
			
			@Override
			public void render(PoseStack matrixStackIn, int parX, int parY, float partialTicks) {
				if (visible) {
					
					float tint = 1f;
					if (parX >= this.x && parY >= this.y
							&& parX <= this.x + this.width
							&& parY <= this.y + this.height) {
						tint = .8f;
					}
					
					RenderSystem.setShaderTexture(0, TEXT);
					int y = 0;
					if (gui.container.isValid)
						y += SUBMIT_HEIGHT;
					RenderFuncs.blit(matrixStackIn, this.x, this.y,
							SUBMIT_TEXT_HOFFSET, SUBMIT_TEXT_VOFFSET + y,
							this.width, this.height,
							tint, tint, tint, 1f);
				}
			}
			
		}
	}
	
	private static class InputSlot extends Slot {
		
		private ItemStack required = ItemStack.EMPTY;
		private ModificationTableContainer container;

		public InputSlot(ModificationTableContainer container, Container inventoryIn, int index, int x, int y) {
			super(inventoryIn, index, x, y);
			this.container = container;
		}
		
		@Override
		public boolean mayPlace(@Nonnull ItemStack stack) {
			if (stack.isEmpty())
				return true;
			
			//if (!container.inventory.getWorld().isRemote)
				return true; // Just accept whatever on the server
			
//			if (required == null)
//				return stack.getItem() instanceof SpellTomePage;
//			
//			return OreDictionary.itemMatches(required, stack, true);
		}
		
		@Override
		public void set(@Nonnull ItemStack stack) {
			super.set(stack);
			
			container.validate();
		}
		
		@Override
		public void onTake(Player playerIn, ItemStack stack) {
			container.validate();
			super.onTake(playerIn, stack);
		}
		
		@Override
		public int getMaxStackSize() {
			return 1;
		}
		
		public void setRequired(@Nonnull ItemStack required) {
			this.required = required;
		}
		
	}
}