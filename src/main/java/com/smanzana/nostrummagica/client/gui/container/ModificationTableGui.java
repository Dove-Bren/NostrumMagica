package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.ModificationTable.ModificationTableEntity;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ModifyMessage;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.shapes.AoEShape;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreDictionary;

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
	
	public static class ModificationTableContainer extends Container {
		
		// Kept just to report to server which TE is doing crafting
		protected BlockPos pos;
		protected EntityPlayer player;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected ModificationTableEntity inventory;
		protected boolean isValid; // has input needed to combine
		protected InputSlot inputSlot; // Input slot. Changes what it can accept
		protected int floatIndex = 0;
		protected boolean boolIndex = false;
		protected int modIndex = 0;
		
		protected boolean runeMode;
		protected boolean hasBool;
		protected boolean hasFloat;
		
		public ModificationTableContainer(EntityPlayer player, IInventory playerInv, ModificationTableEntity tableInventory, BlockPos pos) {
			this.inventory = tableInventory;
			this.player = player;
			this.pos = pos;
			this.runeMode = false;
			
			this.addSlotToContainer(new Slot(inventory, 0, SLOT_MAIN_HOFFSET, SLOT_MAIN_VOFFSET) {
				
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					return inventory.isItemValidForSlot(this.slotNumber, stack);
				}
				
				@Override
				public void putStack(@Nullable ItemStack stack) {
					super.putStack(stack);
					floatIndex = 0;
					
					if (stack != null && stack.getItem() instanceof SpellRune) {
						SpellPartParam params = SpellRune.getPieceParam(stack);
						SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
						if (comp.getTrigger().supportedFloats() != null) {
							float[] vals = comp.getTrigger().supportedFloats();
							int i = 0;
							for (float val : vals) {
								if (val == params.level) {
									floatIndex = i;
									break;
								}
								i++;
									
							}
						}
						
						if (comp.getTrigger().supportsBoolean()) {
							boolIndex = params.flip;
						}
					}
					
					validate();
				}
				
				@Override
				public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
					validate();
					floatIndex = 0;
					super.onPickupFromSlot(playerIn, stack);
				}
			});
			
			this.inputSlot = new InputSlot(this, inventory, 1, SLOT_INPUT_HOFFSET, SLOT_INPUT_VOFFSET);
			this.addSlotToContainer(inputSlot);
			
			// Construct player inventory
			for (int y = 0; y < 3; y++) {
				for (int x = 0; x < 9; x++) {
					this.addSlotToContainer(new Slot(playerInv, x + y * 9 + 9, PLAYER_INV_HOFFSET + (x * 18), PLAYER_INV_VOFFSET + (y * 18)));
				}
			}
			// Construct player hotbar
			for (int x = 0; x < 9; x++) {
				this.addSlotToContainer(new Slot(playerInv, x, PLAYER_INV_HOFFSET + x * 18, 58 + (PLAYER_INV_VOFFSET)));
			}
			
			// isValid means there's something that can accept a spell
			// in the tome slot. Is there?
			isValid = false;
			ItemStack stack = this.inventory.getStackInSlot(0);
			if (stack != null && stack.getItem() instanceof BlankScroll)
				isValid = true;
			
			validate();
			
		}
		
		@Override
		public ItemStack transferStackInSlot(EntityPlayer playerIn, int fromSlot) {
			ItemStack prev = null;	
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				prev = cur.copy();
				
				
				
//				if (fromSlot == mySlot) {
//					// This is going FROM Brazier to player
//					if (!this.mergeItemStack(cur, 1, 11, true))
//						return null;
//					else
//						// From Player TO Brazier
//						if (!this.mergeItemStack(cur, 0, 0, false)) {
//							return null;
//						}
//				}
				
				if (cur.stackSize == 0) {
					slot.putStack((ItemStack) null);
				} else {
					slot.onSlotChanged();
				}
				
				if (cur.stackSize == prev.stackSize) {
					return null;
				}
				slot.onPickupFromSlot(playerIn, cur);
			}
			
			return prev;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean canInteractWith(EntityPlayer playerIn) {
			return true;
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, EntityPlayer player) {
			ItemStack ret = super.slotClick(slotId, dragType, clickTypeIn, player);
			
//			isValid = false;
//			ItemStack stack = this.inventory.getStackInSlot(0);
//			if (stack != null && (stack.getItem() instanceof SpellTome || stack.getItem() instanceof BlankScroll))
//				isValid = true;
			
			return ret;
		}
		
		public void validate() {
			modIndex++;
			if (this.inventory.getMainSlot() == null) {
				this.isValid = false;
				this.runeMode = false;
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellTome) {
				this.runeMode = false;
				ItemStack inputItem = inputSlot.getStack();
				if (inputItem == null || !(inputItem.getItem() instanceof SpellTomePage)) {
					this.isValid = false;
				}
				
				isValid = (SpellTome.getModifications(inventory.getMainSlot()) > 0);
				inputSlot.setRequired(null);
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellRune) {
				this.runeMode = true;
				SpellComponentWrapper component = SpellRune.toComponentWrapper(inventory.getMainSlot());
				
				hasBool = false;
				hasFloat = false;
				boolean hasChange = false;
				
				if (component.isTrigger()) {
					hasBool = component.getTrigger().supportsBoolean();
					hasFloat = component.getTrigger().supportedFloats() != null;
				} else {
					hasBool = component.getShape().supportsBoolean();
					hasFloat = component.getShape().supportedFloats() != null;
				}
				
				this.isValid = true;
				
				// Check that we've changed float selection
				if (hasFloat) {
					float cur = SpellRune.getPieceParam(inventory.getMainSlot()).level;
					float targ;
					if (component.isTrigger())
						targ = component.getTrigger().supportedFloats()[floatIndex];
					else
						targ = component.getShape().supportedFloats()[floatIndex];
					hasChange = targ != cur;
				}
				
				// If we've changed float, check required item is set
				if (hasFloat) {
					if (hasChange) {
						ItemStack required;
						if (component.isTrigger())
							required = component.getTrigger().supportedFloatCosts()[floatIndex];
						else
							required = component.getShape().supportedFloatCosts()[floatIndex];
						
						inputSlot.setRequired(required);
						if (required == null) {
							this.isValid = !inputSlot.getHasStack();
						} else if (inputSlot.getHasStack()) {
							this.isValid = OreDictionary.itemMatches(required, inputSlot.getStack(), true);
						} else {
							this.isValid = false;
						}
					}
					else
						inputSlot.setRequired(null);
					
				}
				
				// Check for change on bool if no change on float
				if (isValid && hasBool && !hasChange) {
					boolean cur = SpellRune.getPieceParam(inventory.getMainSlot()).flip;
					boolean targ = boolIndex;
					hasChange = targ != cur;
				}
				
				if (isValid && !hasBool && !hasFloat)
					isValid = false;
				
				if (isValid && !hasChange)
					isValid = false;
				
			}
		}
		
		public void setMain(ItemStack item) {
			this.inventory.setInventorySlotContents(0, item);
			isValid = false;
			validate();
		}
		
		public void setInput(ItemStack item) {
			this.inventory.setInventorySlotContents(1, item);
			isValid = false;
			validate();
		}

	}
	
	@SideOnly(Side.CLIENT)
	public static class ModificationGui extends GuiContainer {

		private ModificationTableContainer container;
		private int localModIndex = 0;
		
		private SpellCastSummary summary;
		private ItemStack pageShadow;
		private ItemStack[] shadows;
		
		private static int buttonID = -1;
		private GuiButton submitButton;
		
		public ModificationGui(ModificationTableContainer container) {
			super(container);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
			pageShadow = new ItemStack(SpellTomePage.instance());
			shadows = new ItemStack[50];
			shadows[0] = new ItemStack(SpellTome.instance());
			int i = 1;
			for (SpellShape shape : SpellShape.getAllShapes()) {
				if (i >= 50)
					break;
				if (shape.supportsBoolean() || shape.supportedFloats() != null) {
					shadows[i] = SpellRune.getRune(shape);
					i++;
				}
			}
			for (SpellTrigger trigger: SpellTrigger.getAllTriggers()) {
				if (i >= 50)
					break;
				if (trigger.supportsBoolean() || trigger.supportedFloats() != null) {
					shadows[i] = SpellRune.getRune(trigger);
					i++;
				}
			}
		}
		
		@Override
		public void initGui() {
			super.initGui();
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			submitButton = new SubmitButton(buttonID++,
					horizontalMargin + SUBMIT_HOFFSET,
					verticalMargin + SUBMIT_VOFFSET, container);
			this.refreshButtons();
			this.localModIndex = container.modIndex;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			if (localModIndex != container.modIndex) {
				this.refreshButtons();
				this.localModIndex = container.modIndex;
			}
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			if (container.runeMode) {
				// Draw rune sliders or toggles
				int x, y;
				int len = mc.fontRendererObj.getStringWidth("Rune Modification");
				mc.fontRendererObj.drawStringWithShadow("Rune Modification",
						horizontalMargin + (PANEL_HOFFSET) + (PANEL_WIDTH / 2) - (len / 2),
						verticalMargin + PANEL_VOFFSET + 5, 0xFFFFFFFF);
				y = verticalMargin + PANEL_VOFFSET + 20;
				x = horizontalMargin + PANEL_HOFFSET + 5;
				if (container.hasBool) {
					mc.fontRendererObj.drawStringWithShadow("Bool:", x, y, 0xFFa0a0a0);
					
					
					
					y += 20;
				}
				
				if (container.hasFloat) {
					mc.fontRendererObj.drawStringWithShadow("Float:", x, y, 0xFFa0a0a0);
					
					y += 20;
				}
			} else {
				// Draw tome info
				int x, y;
				y = PANEL_VOFFSET + 10;
				ItemStack tome = container.inventory.getMainSlot();
				if (tome != null) {
					String name = tome.getDisplayName();
					if (name == null || name.length() == 0)
						name = "Spell Tome";
					int len = mc.fontRendererObj.getStringWidth(name);
					x = horizontalMargin + PANEL_HOFFSET + (PANEL_WIDTH / 2) - (len / 2);
					mc.fontRendererObj.drawStringWithShadow(name, x, y, 0xFFFFFFFF);
					y += 20;
					
					x = horizontalMargin + PANEL_HOFFSET + 5;
					int valX = horizontalMargin + PANEL_HOFFSET + 75;
					int topY = y;
					
					if (summary == null || !container.isValid) {
						summary = new SpellCastSummary(0, 0);
						SpellTome.applyEnhancements(tome, summary, container.player);
					}
					
					// Efficiency
					mc.fontRendererObj.drawStringWithShadow("Efficiency: ", x, y, 0xFFA0A0A0);
					mc.fontRendererObj.drawString(String.format("%+03.0f%%", (summary.getEfficiency() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRendererObj.FONT_HEIGHT;
					
					// LMC
					mc.fontRendererObj.drawStringWithShadow("Mana Cost: ", x, y, 0xFFA0A0A0);
					mc.fontRendererObj.drawString(String.format("%+03.0f%%", (summary.getCostRate() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRendererObj.FONT_HEIGHT;
					
					// LRC
					mc.fontRendererObj.drawStringWithShadow("Reagent Cost: ", x, y, 0xFFA0A0A0);
					mc.fontRendererObj.drawString(String.format("%+03.0f%%", (summary.getReagentCost() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRendererObj.FONT_HEIGHT;
					
					// XP
					mc.fontRendererObj.drawStringWithShadow("Bonus XP: ", x, y, 0xFFA0A0A0);
					mc.fontRendererObj.drawString(String.format("%+03.0f%%", (summary.getXpRate() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRendererObj.FONT_HEIGHT;
					
					y = topY;
					x += 100;
					valX += 100;
					
					// Level
					mc.fontRendererObj.drawStringWithShadow("Level: ", x, y, 0xFFA0A0A0);
					mc.fontRendererObj.drawString("" + SpellTome.getLevel(tome), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRendererObj.FONT_HEIGHT;
					
					// Modifications
					mc.fontRendererObj.drawStringWithShadow("Modifications: ", x, y, 0xFFA0A0A0);
					mc.fontRendererObj.drawString("" + SpellTome.getModifications(tome), valX, y, 0xFFFFFFFF);
							
				}
			}
			
			if (!container.inputSlot.getHasStack()) {
				ItemStack shadow = container.inputSlot.required;
				if (shadow == null && !container.runeMode) {
					shadow = pageShadow;
				}
				if (shadow != null) {
					GlStateManager.pushMatrix();
					GlStateManager.translate(0, 0, -100);
					mc.getRenderItem().renderItemIntoGUI(shadow,
						horizontalMargin + container.inputSlot.xDisplayPosition,
						verticalMargin + container.inputSlot.yDisplayPosition);
					GlStateManager.popMatrix();
				}
			}
			if (!container.isValid) {
				int color = 0x55FFFFFF;
				if ((container.inputSlot.required != null && container.inputSlot.getHasStack())
						|| (container.inputSlot.required == null && container.inputSlot.getHasStack()))
					color = 0x90FF5050;
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 1);
				Gui.drawRect(
						horizontalMargin + container.inputSlot.xDisplayPosition,
						verticalMargin + container.inputSlot.yDisplayPosition,
						horizontalMargin + container.inputSlot.xDisplayPosition + 16,
						verticalMargin + container.inputSlot.yDisplayPosition + 16,
						color);
				GlStateManager.popMatrix();
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			if (container.isValid) {
				
				int submitX = horizontalMargin + SUBMIT_HOFFSET;
				int submitY = verticalMargin + SUBMIT_VOFFSET;
				
				if (mouseX >= submitX && mouseY >= submitY
						&& mouseX <= submitX + SUBMIT_WIDTH
						&& mouseY <= submitY + SUBMIT_HEIGHT) {
					Gui.drawRect(SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x20FFFFFF);
				}
				
//				if (!container.spellValid) {
//					
//					if (mouseX > horizontalMargin + STATUS_DISP_HOFFSET && mouseX <= horizontalMargin + STATUS_DISP_HOFFSET + STATUS_WIDTH
//						 && mouseY > verticalMargin + STATUS_DISP_VOFFSET && mouseY <= verticalMargin + STATUS_DISP_VOFFSET + STATUS_HEIGHT) {
//						GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
//						
//						this.drawHoveringText(container.spellErrorStrings,
//								mouseX - horizontalMargin, mouseY - verticalMargin);
//					}
//				}
//				
//				if (mouseX > horizontalMargin + NAME_HOFFSET && mouseX <= horizontalMargin + NAME_HOFFSET + NAME_WIDTH
//						 && mouseY > verticalMargin + NAME_VOFFSET && mouseY <= verticalMargin + NAME_VOFFSET + NAME_HEIGHT) {
//					Gui.drawRect(NAME_HOFFSET, NAME_VOFFSET, NAME_HOFFSET + NAME_WIDTH, NAME_VOFFSET + NAME_HEIGHT, 0x40000000);
//				}
//				
//				if (mouseX >= horizontalMargin + SUBMIT_HOFFSET && mouseX <= horizontalMargin + SUBMIT_HOFFSET + SUBMIT_WIDTH && 
//						mouseY >= verticalMargin + SUBMIT_VOFFSET && mouseY <= verticalMargin + SUBMIT_VOFFSET + SUBMIT_HEIGHT) {
//					Gui.drawRect(SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x40000000);
//					this.drawHoveringText(container.reagentStrings,
//							mouseX - horizontalMargin, mouseY - verticalMargin);
//				}
			}
			
			if (container.inventory.getMainSlot() == null) {
				ItemStack display;
				if ((Minecraft.getSystemTime() / 1000) % 2 == 0) {
					display = new ItemStack(SpellTome.instance());
				} else {
					display = SpellRune.getRune(AoEShape.instance());
				}
				
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, -100);
				mc.getRenderItem().renderItemIntoGUI(display,
						SLOT_MAIN_HOFFSET,
						SLOT_MAIN_VOFFSET);
				GlStateManager.popMatrix();
				
				int color = 0x55FFFFFF;
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 1);
				Gui.drawRect(
						SLOT_MAIN_HOFFSET,
						SLOT_MAIN_VOFFSET,
						SLOT_MAIN_HOFFSET + 16,
						SLOT_MAIN_VOFFSET + 16,
						color);
				GlStateManager.popMatrix();
			}
			
		}
			
		@Override
		public void actionPerformed(GuiButton button) {
			if (button == this.submitButton && container.isValid) {
				if (container.runeMode) {
					SpellComponentWrapper component = SpellRune.toComponentWrapper(container.inventory.getMainSlot());
					float[] vals;
					if (component.isTrigger()) {
						vals = component.getTrigger().supportedFloats();
					} else {
						vals = component.getShape().supportedFloats();
					}
					NetworkHandler.getSyncChannel().sendToServer(
							new ModifyMessage(container.pos, container.boolIndex, vals[container.floatIndex]));
					container.inventory.modify(container.boolIndex, vals[container.floatIndex]);
				} else {
					NetworkHandler.getSyncChannel().sendToServer(
							new ModifyMessage(container.pos, false, 0));
					container.inventory.modify(false, 0);
				}
			} else if (button instanceof ToggleButton) {
				container.boolIndex = ((ToggleButton) button).val;
				container.validate();
			} else if (button instanceof FloatButton) {
				SpellComponentWrapper component = SpellRune.toComponentWrapper(container.inventory.getMainSlot());
				float[] vals;
				if (component.isTrigger()) {
					vals = component.getTrigger().supportedFloats();
				} else {
					vals = component.getShape().supportedFloats();
				}
				if (vals == null)
					container.floatIndex = 0;
				else
					container.floatIndex = Math.min(vals.length - 1, ((FloatButton) button).val);
				
				container.validate();
			}
		}
		
		protected void refreshButtons() {
			this.buttonList.clear();
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			int y = verticalMargin + PANEL_VOFFSET + 20;
			int x = horizontalMargin + PANEL_HOFFSET + 50;
			
			if (container.inventory.getMainSlot() != null) {
				if (container.hasBool) {
					this.addButton(new ToggleButton(buttonID++, x, y, false, container));
					this.addButton(new ToggleButton(buttonID++, x + 15, y, true, container));
					
					y += 20;
				}
				if (container.hasFloat) {
					SpellComponentWrapper component = SpellRune.toComponentWrapper(container.inventory.getMainSlot());
					float[] vals;
					
					if (component != null) {
						if (component.isTrigger()) {
							vals = component.getTrigger().supportedFloats();
						} else {
							vals = component.getShape().supportedFloats();
						}
						
						for (int i = 0; i < vals.length; i++) {
							FloatButton button = new FloatButton(buttonID++, x, y, i, vals[i], container);
							
							this.addButton(button);
							x += 25;
						}
					}
				}
			}
			
			this.addButton(submitButton);
		}
		
		private static class ToggleButton extends GuiButton {

			private boolean val;
			private ModificationTableContainer container;
			
			public ToggleButton(int buttonId, int x, int y, boolean val, ModificationTableContainer container) {
				super(buttonId, x, y, "");
				this.val = val;
				this.width = BUTTON_WIDTH;
				this.height = BUTTON_HEIGHT;
				this.container = container;
			}
			
			@Override
			public void drawButton(Minecraft mc, int parX, int parY) {
				if (visible) {
					
					float tint = 1f;
					mc.getTextureManager().bindTexture(TEXT);
					if (parX >= this.xPosition && parY >= this.yPosition
							&& parX <= this.xPosition + this.width
							&& parY <= this.yPosition + this.height) {
						tint = .8f;
					}
					
					int x = 0;
					if (!val)
						x += 2 * BUTTON_WIDTH;
					if (val != container.boolIndex)
						x += BUTTON_WIDTH;
					
					GlStateManager.color(tint, tint, tint);
					this.drawTexturedModalRect(this.xPosition, this.yPosition,
							BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET,
							this.width, this.height);
				}
			}
		}
		
		private static class FloatButton extends GuiButton {
			private int val;
			private float actualVal;
			private ModificationTableContainer container;
			
			public FloatButton(int buttonId, int x, int y, int val, float actual, ModificationTableContainer container) {
				super(buttonId, x, y, "");
				this.val = val;
				this.actualVal = actual;
				this.width = LARGE_BUTTON_WIDTH;
				this.height = LARGE_BUTTON_HEIGHT;
				this.container = container;
			}
			
			@Override
			public void drawButton(Minecraft mc, int parX, int parY) {
				if (visible) {
					
					float tint = 1f;
					mc.getTextureManager().bindTexture(TEXT);
					if (parX >= this.xPosition && parY >= this.yPosition
							&& parX <= this.xPosition + this.width
							&& parY <= this.yPosition + this.height) {
						tint = .8f;
					}
					
					int x = 0;
					if (container.floatIndex != this.val)
						x += LARGE_BUTTON_WIDTH;
					
					int len;
					String text = String.format("%.1f", actualVal);
					len = mc.fontRendererObj.getStringWidth(text);
					
					GlStateManager.color(tint, tint, tint);
					this.drawTexturedModalRect(this.xPosition, this.yPosition,
							BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET + BUTTON_HEIGHT,
							this.width, this.height);
					
					mc.fontRendererObj.drawString(text,
							xPosition + (LARGE_BUTTON_WIDTH / 2) - (len / 2),
							yPosition + 1,
							0xFF000000);
				}
			}
		}
		
		private static class SubmitButton extends GuiButton {
			
			private ModificationTableContainer container;
			
			public SubmitButton(int id, int x, int y, ModificationTableContainer container) {
				super(id, x, y, "");
				this.width = LARGE_BUTTON_WIDTH;
				this.height = LARGE_BUTTON_HEIGHT;
				this.container = container;
			}
			
			@Override
			public void drawButton(Minecraft mc, int parX, int parY) {
				if (visible) {
					
					float tint = 1f;
					if (parX >= this.xPosition && parY >= this.yPosition
							&& parX <= this.xPosition + this.width
							&& parY <= this.yPosition + this.height) {
						tint = .8f;
					}
					
					mc.getTextureManager().bindTexture(TEXT);
					GlStateManager.color(tint, tint, tint);
					int y = 0;
					if (container.isValid)
						y += SUBMIT_HEIGHT;
					this.drawTexturedModalRect(this.xPosition, this.yPosition,
							SUBMIT_TEXT_HOFFSET, SUBMIT_TEXT_VOFFSET + y,
							this.width, this.height);
				}
			}
			
		}
	}
	
	private static class InputSlot extends Slot {
		
		private ItemStack required;
		private ModificationTableContainer container;

		public InputSlot(ModificationTableContainer container, IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
			this.container = container;
		}
		
		@Override
		public boolean isItemValid(@Nullable ItemStack stack) {
			if (stack == null)
				return true;
			
			//if (!container.inventory.getWorld().isRemote)
				return true; // Just accept whatever on the server
			
//			if (container.runeMode && container.floatIndex == 0)
//				return false;
//			
//			if (required == null)
//				return stack.getItem() instanceof SpellTomePage;
//			
//			return OreDictionary.itemMatches(required, stack, true);
		}
		
		@Override
		public void putStack(@Nullable ItemStack stack) {
			super.putStack(stack);
			
			container.validate();
		}
		
		@Override
		public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
			container.validate();
			super.onPickupFromSlot(playerIn, stack);
		}
		
		@Override
		public int getSlotStackLimit() {
			return 1;
		}
		
		public void setRequired(ItemStack required) {
			this.required = required;
		}
		
	}
}