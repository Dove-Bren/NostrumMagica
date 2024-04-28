package com.smanzana.nostrummagica.client.gui.container;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTomePage;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.ModifyMessage;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPartParam;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;
import com.smanzana.nostrummagica.tiles.ModificationTableEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
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
	
	public static class ModificationTableContainer extends Container {
		
		public static final String ID = "modification_table";
		
		// Kept just to report to server which TE is doing crafting
		protected BlockPos pos;
		protected PlayerEntity player;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected ModificationTableEntity inventory;
		protected boolean isValid; // has input needed to combine
		protected InputSlot inputSlot; // Input slot. Changes what it can accept
		protected int floatIndex = 0;
		protected boolean boolIndex = false;
		protected int modIndex = 0;
		
		protected boolean runeMode;
		protected boolean scrollMode;
		protected boolean hasBool;
		protected boolean hasFloat;
		protected SpellComponentWrapper component;
		
		public ModificationTableContainer(int windowId, PlayerEntity player, IInventory playerInv, ModificationTableEntity tableInventory, BlockPos pos) {
			super(NostrumContainers.ModificationTable, windowId);
			this.inventory = tableInventory;
			this.player = player;
			this.pos = pos;
			this.runeMode = false;
			this.scrollMode = false;
			
			this.addSlot(new Slot(inventory, 0, SLOT_MAIN_HOFFSET, SLOT_MAIN_VOFFSET) {
				
				@Override
				public boolean isItemValid(@Nonnull ItemStack stack) {
					return inventory.isItemValidForSlot(this.slotNumber, stack);
				}
				
				@Override
				public void putStack(@Nonnull ItemStack stack) {
					super.putStack(stack);
					floatIndex = 0;
					
					if (!stack.isEmpty() && stack.getItem() instanceof SpellRune) {
						SpellPartParam params = SpellRune.getPieceParam(stack);
						SpellComponentWrapper comp = SpellRune.toComponentWrapper(stack);
						
						if (comp.isTrigger()) {
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
						} else if (comp.isShape()) {
							if (comp.getShape().supportedFloats() != null) {
								float[] vals = comp.getShape().supportedFloats();
								int i = 0;
								for (float val : vals) {
									if (val == params.level) {
										floatIndex = i;
										break;
									}
									i++;
										
								}
							}
							
							if (comp.getShape().supportsBoolean()) {
								boolIndex = params.flip;
							}
						}
					} else if (!stack.isEmpty() && stack.getItem() instanceof SpellScroll) {
						// Shouldn't be null since we disallow null in the slot... but let's just be safe. This is UI code.
						Spell spell = SpellScroll.getSpell(stack);
						if (spell != null) {
							floatIndex = spell.getIconIndex();							
						}
					}
					
					validate();
				}
				
				@Override
				public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
					validate();
					floatIndex = 0;
					return super.onTake(playerIn, stack);
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
		
		public static final ModificationTableContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			return new ModificationTableContainer(windowId, playerInv.player, playerInv, ContainerUtil.GetPackedTE(buf), buf.readBlockPos());
		}
		
		public static final IPackedContainerProvider Make(ModificationTableEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new ModificationTableContainer(windowId, player, playerInv, table, table.getPos());
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
				buffer.writeBlockPos(table.getPos());
			});
		}
		
		@Override
		public @Nonnull ItemStack transferStackInSlot(PlayerEntity playerIn, int fromSlot) {
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				
				if (slot.inventory == this.inventory) {
					// Trying to take our items
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						slot.putStack(ItemStack.EMPTY);
						slot.onTake(playerIn, cur);
					}
				} else {
					// Trying to add an item
					Slot mainSlot = this.getSlot(0);
					if (!mainSlot.getHasStack()) {
						if (mainSlot.isItemValid(cur))
							mainSlot.putStack(cur.split(1));
					} else if (!inputSlot.getHasStack()) {
						if (inputSlot.isItemValid(cur))
							inputSlot.putStack(cur.split(1));
					}
				}
				
				if (cur.isEmpty() || cur.getCount() <= 0) {
					slot.putStack(ItemStack.EMPTY);
				}
			}
			
			return ItemStack.EMPTY;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		public void validate() {
			modIndex++;
			if (this.inventory.getMainSlot().isEmpty()) {
				this.isValid = false;
				this.runeMode = false;
				this.scrollMode = false;
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellTome) {
				this.runeMode = false;
				this.scrollMode = false;
				ItemStack inputItem = inputSlot.getStack();
				isValid = (SpellTome.getModifications(inventory.getMainSlot()) > 0);
				if (!isValid || inputItem.isEmpty() || !(inputItem.getItem() instanceof SpellTomePage)) {
					this.isValid = false;
				}
				
				
				inputSlot.setRequired(ItemStack.EMPTY);
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellRune) {
				this.runeMode = true;
				this.scrollMode = false;
				SpellComponentWrapper component = SpellRune.toComponentWrapper(inventory.getMainSlot());
				this.component = component;
				hasBool = false;
				hasFloat = false;
				boolean hasChange = false;
				
				if (component.isTrigger()) {
					hasBool = component.getTrigger().supportsBoolean();
					hasFloat = component.getTrigger().supportedFloats() != null;
				} else if (component.isShape()) {
					hasBool = component.getShape().supportsBoolean();
					hasFloat = component.getShape().supportedFloats() != null;
				} else {
					this.isValid = false;
					this.runeMode = false;
					return;
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
						ItemStack required = ItemStack.EMPTY;
						if (component.isTrigger())
							required = component.getTrigger().supportedFloatCosts().get(floatIndex);
						else
							required = component.getShape().supportedFloatCosts().get(floatIndex);
						
						inputSlot.setRequired(required);
						if (required.isEmpty()) {
							this.isValid = !inputSlot.getHasStack();
						} else if (inputSlot.getHasStack()) {
							this.isValid = ItemStacks.stacksMatch(required, inputSlot.getStack());
						} else {
							this.isValid = false;
						}
					}
					else
						inputSlot.setRequired(ItemStack.EMPTY);
					
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
				
				return;
			}
			
			if (inventory.getMainSlot().getItem() instanceof SpellScroll) {
				boolean hasChange = false;
				ItemStack inputItem = inputSlot.getStack();
				ItemStack required = new ItemStack(Items.RED_DYE);
				Ingredient realRequired = Ingredient.fromTag(Tags.Items.DYES);
				
				
				inputSlot.setRequired(required);
				isValid = (SpellScroll.getSpell(inventory.getMainSlot()) != null);
				
				if (isValid) { // and therefore, spell is not null
					int cur = SpellScroll.getSpell(inventory.getMainSlot()).getIconIndex();
					hasChange = cur != (int) this.floatIndex;
				}
				
				if (!isValid || !hasChange || inputItem.isEmpty() || !(realRequired.test(inputItem))) {
					this.isValid = false;
				}
				
				hasBool = false;
				hasFloat = true;
				
				runeMode = false;
				scrollMode = true;
				
				return;
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
	
	@OnlyIn(Dist.CLIENT)
	public static class ModificationGui extends AutoGuiContainer<ModificationTableContainer> {

		private ModificationTableContainer container;
		private int localModIndex = 0;
		
		private SpellCastSummary summary;
		private @Nonnull ItemStack pageShadow;
		private NonNullList<ItemStack> shadows;
		
		private Button submitButton;
		
		public ModificationGui(ModificationTableContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
			pageShadow = new ItemStack(NostrumItems.spellTomePage);
			shadows = NonNullList.create();
			shadows.add(new ItemStack(NostrumItems.spellTomeNovice)); // hasto be index 0
			shadows.add(new ItemStack(NostrumItems.spellScroll)); // has to be index 1
			for (SpellShape shape : SpellShape.getAllShapes()) {
				if (shape.supportsBoolean() || shape.supportedFloats() != null) {
					shadows.add(SpellRune.getRune(shape));
				}
			}
			for (SpellTrigger trigger: SpellTrigger.getAllTriggers()) {
				if (trigger.supportsBoolean() || trigger.supportedFloats() != null) {
					shadows.add(SpellRune.getRune(trigger));
				}
			}
		}
		
		@Override
		public void init() {
			super.init();
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			submitButton = new SubmitButton(
					horizontalMargin + SUBMIT_HOFFSET,
					verticalMargin + SUBMIT_VOFFSET, this);
			this.refreshButtons();
			this.localModIndex = container.modIndex;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			if (localModIndex != container.modIndex) {
				this.refreshButtons();
				this.localModIndex = container.modIndex;
			}
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			if (container.runeMode) {
				// Draw rune sliders or toggles
				int x, y;
				int len = mc.fontRenderer.getStringWidth("Rune Modification");
				mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Rune Modification",
						horizontalMargin + (PANEL_HOFFSET) + (PANEL_WIDTH / 2) - (len / 2),
						verticalMargin + PANEL_VOFFSET + 5, 0xFFFFFFFF);
				y = verticalMargin + PANEL_VOFFSET + 20;
				x = horizontalMargin + PANEL_HOFFSET + 5;
				if (container.hasBool) {
					String boolTitle;
					if (container.component.isTrigger())
						boolTitle = container.component.getTrigger().supportedBooleanName();
					else
						boolTitle = container.component.getShape().supportedBooleanName();
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, boolTitle, x, y, 0xFFa0a0a0);
					
					y += 25;
				}
				
				if (container.hasFloat && container.runeMode) {
					String floatTitle;
					if (container.component.isTrigger())
						floatTitle = container.component.getTrigger().supportedFloatName();
					else
						floatTitle = container.component.getShape().supportedFloatName();
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, floatTitle, x, y, 0xFFa0a0a0);
					
					y += 25;
				}
			} else if (container.scrollMode) {
				
			} else {
				// Draw tome info
				int x, y;
				y = verticalMargin + PANEL_VOFFSET + 10;
				ItemStack tome = container.inventory.getMainSlot();
				if (!tome.isEmpty()) {
					ITextComponent nameComp = tome.getDisplayName();
					String name = nameComp == null ? null : nameComp.getString();
					if (name == null || name.length() == 0)
						name = "Spell Tome";
					int len = mc.fontRenderer.getStringWidth(name);
					x = horizontalMargin + PANEL_HOFFSET + (PANEL_WIDTH / 2) - (len / 2);
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, name, x, y, 0xFFFFFFFF);
					y += 20;
					
					x = horizontalMargin + PANEL_HOFFSET + 5;
					int valX = horizontalMargin + PANEL_HOFFSET + 75;
					int topY = y;
					
					if (summary == null || !container.isValid) {
						summary = new SpellCastSummary(0, 0);
						SpellTome.applyEnhancements(tome, summary, container.player);
					}
					
					// Efficiency
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Efficiency: ", x, y, 0xFFA0A0A0);
					mc.fontRenderer.drawString(matrixStackIn, String.format("%+03.0f%%", (summary.getEfficiency() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRenderer.FONT_HEIGHT;
					
					// LMC
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Mana Cost: ", x, y, 0xFFA0A0A0);
					mc.fontRenderer.drawString(matrixStackIn, String.format("%+03.0f%%", (summary.getCostRate() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRenderer.FONT_HEIGHT;
					
					// LRC
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Reagent Cost: ", x, y, 0xFFA0A0A0);
					mc.fontRenderer.drawString(matrixStackIn, String.format("%+03.0f%%", (summary.getReagentCost() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRenderer.FONT_HEIGHT;
					
					// XP
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Bonus XP: ", x, y, 0xFFA0A0A0);
					mc.fontRenderer.drawString(matrixStackIn, String.format("%+03.0f%%", (summary.getXpRate() - 1f) * 100), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRenderer.FONT_HEIGHT;
					
					y = topY;
					x += 100;
					valX += 100;
					
					// Level
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Level: ", x, y, 0xFFA0A0A0);
					mc.fontRenderer.drawString(matrixStackIn, "" + SpellTome.getLevel(tome), valX, y, 0xFFFFFFFF);
					y += 2 + mc.fontRenderer.FONT_HEIGHT;
					
					// Modifications
					mc.fontRenderer.drawStringWithShadow(matrixStackIn, "Modifications: ", x, y, 0xFFA0A0A0);
					mc.fontRenderer.drawString(matrixStackIn, "" + SpellTome.getModifications(tome), valX, y, 0xFFFFFFFF);
							
				}
			}
			
			if (!container.inputSlot.getHasStack()) {
				ItemStack shadow = container.inputSlot.required;
				if (shadow.isEmpty() && !container.runeMode) {
					shadow = pageShadow;
				}
				if (!shadow.isEmpty()) {
					matrixStackIn.push();
					matrixStackIn.translate(0, 0, -100);
					mc.getItemRenderer().renderItemIntoGUI(shadow,
						horizontalMargin + container.inputSlot.xPos,
						verticalMargin + container.inputSlot.yPos);
					matrixStackIn.pop();
				}
			}
			
			if (container.inventory.getMainSlot().isEmpty()) {
				ItemStack display;
//				if ((System.currentTimeMillis() / 1000) % 2 == 0) {
//					display = new ItemStack(SpellTome.instance());
//				} else {
//					display = SpellRune.getRune(AoEShape.instance());
//				}
				final int idx = Math.abs(((int) System.currentTimeMillis() / 1000) % shadows.size());
				display = shadows.get(idx);
				
				matrixStackIn.push();
				matrixStackIn.translate(0, 0, -100);
				mc.getItemRenderer().renderItemIntoGUI(display,
						horizontalMargin + SLOT_MAIN_HOFFSET,
						verticalMargin + SLOT_MAIN_VOFFSET);
				matrixStackIn.pop();
				
				int color = 0x55FFFFFF;
				matrixStackIn.push();
				matrixStackIn.translate(0, 0, 1);
				RenderFuncs.drawRect(matrixStackIn, 
						horizontalMargin + SLOT_MAIN_HOFFSET,
						verticalMargin + SLOT_MAIN_VOFFSET,
						horizontalMargin + SLOT_MAIN_HOFFSET + 16,
						verticalMargin + SLOT_MAIN_VOFFSET + 16,
						color);
				matrixStackIn.pop();
			}
			
			if (!container.isValid) {
				int color = 0x55FFFFFF;
				if ((!container.inputSlot.required.isEmpty() && container.inputSlot.getHasStack())
						|| (container.inputSlot.required.isEmpty() && container.inputSlot.getHasStack()))
					color = 0x90FF5050;
				matrixStackIn.push();
				matrixStackIn.translate(0, 0, 1);
				RenderFuncs.drawRect(matrixStackIn, 
						horizontalMargin + container.inputSlot.xPos,
						verticalMargin + container.inputSlot.yPos,
						horizontalMargin + container.inputSlot.xPos + 16,
						verticalMargin + container.inputSlot.yPos + 16,
						color);
				matrixStackIn.pop();
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			if (container.isValid) {
				
				int submitX = horizontalMargin + SUBMIT_HOFFSET;
				int submitY = verticalMargin + SUBMIT_VOFFSET;
				
				if (mouseX >= submitX && mouseY >= submitY
						&& mouseX <= submitX + SUBMIT_WIDTH
						&& mouseY <= submitY + SUBMIT_HEIGHT) {
					RenderFuncs.drawRect(matrixStackIn, SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x20FFFFFF);
				}
				
//				if (!container.spellValid) {
//					
//					if (mouseX > horizontalMargin + STATUS_DISP_HOFFSET && mouseX <= horizontalMargin + STATUS_DISP_HOFFSET + STATUS_WIDTH
//						 && mouseY > verticalMargin + STATUS_DISP_VOFFSET && mouseY <= verticalMargin + STATUS_DISP_VOFFSET + STATUS_HEIGHT) {
//						GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
//						
//						this.drawHoveringText(container.spellErrorStrings,
//								mouseX - horizontalMargin, mouseY - verticalMargin);
//					}
//				}
//				
//				if (mouseX > horizontalMargin + NAME_HOFFSET && mouseX <= horizontalMargin + NAME_HOFFSET + NAME_WIDTH
//						 && mouseY > verticalMargin + NAME_VOFFSET && mouseY <= verticalMargin + NAME_VOFFSET + NAME_HEIGHT) {
//					RenderFuncs.drawRect(NAME_HOFFSET, NAME_VOFFSET, NAME_HOFFSET + NAME_WIDTH, NAME_VOFFSET + NAME_HEIGHT, 0x40000000);
//				}
//				
//				if (mouseX >= horizontalMargin + SUBMIT_HOFFSET && mouseX <= horizontalMargin + SUBMIT_HOFFSET + SUBMIT_WIDTH && 
//						mouseY >= verticalMargin + SUBMIT_VOFFSET && mouseY <= verticalMargin + SUBMIT_VOFFSET + SUBMIT_HEIGHT) {
//					RenderFuncs.drawRect(SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x40000000);
//					this.drawHoveringText(container.reagentStrings,
//							mouseX - horizontalMargin, mouseY - verticalMargin);
//				}
			}
			
		}
		
		protected void onSubmitButton() {
			if (container.isValid) {
				if (container.runeMode) {
					SpellComponentWrapper component = SpellRune.toComponentWrapper(container.inventory.getMainSlot());
					float[] vals;
					if (component.isTrigger()) {
						vals = component.getTrigger().supportedFloats();
					} else {
						vals = component.getShape().supportedFloats();
					}
					float fVal = (vals == null ? 0 : vals[container.floatIndex]);
					NetworkHandler.sendToServer(
							new ModifyMessage(container.pos, container.boolIndex, fVal));
					container.inventory.modify(container.boolIndex, fVal);
				} else if (container.scrollMode) {
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
			if (container.runeMode) {
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
			} else if (container.scrollMode) {
				container.floatIndex =((FloatButton) button).val;
			}
			
			container.validate();
		}
			
		protected void refreshButtons() {
			this.buttons.clear();
			this.children.clear();
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			int y = verticalMargin + PANEL_VOFFSET + 33;
			int x = horizontalMargin + PANEL_HOFFSET + 50;
			
			if (!container.inventory.getMainSlot().isEmpty()) {
				if (container.hasBool) {
					this.addButton(new ToggleButton(x, y, false, this));
					this.addButton(new ToggleButton(x + 15, y, true, this));
					
					y += 25;
				}
				if (container.hasFloat) {
					if (container.runeMode) {
						SpellComponentWrapper component = SpellRune.toComponentWrapper(container.inventory.getMainSlot());
						float[] vals;
						
						if (component != null) {
							if (component.isTrigger()) {
								vals = component.getTrigger().supportedFloats();
							} else {
								vals = component.getShape().supportedFloats();
							}
							
							for (int i = 0; i < vals.length; i++) {
								FloatButton button = new FloatButton(x, y, i, vals[i], this);
								
								this.addButton(button);
								x += 25;
							}
						}
					} else if (container.scrollMode) {
						final int margin = 2;
						
						// more condensed
						x = horizontalMargin + PANEL_HOFFSET + margin;
						y = verticalMargin + PANEL_VOFFSET + margin;
						
						final int numHorizontal = (PANEL_WIDTH - (margin * 2)) / 16;
						
						for (int i = 0; i < SpellIcon.numIcons; i++) {
							FloatButton button = new FloatButton(x + ((i % numHorizontal) * 16), y + ((i / numHorizontal) * 16), i, i, this);
							
							this.addButton(button);
						}
					}
				}
			}
			
			this.addButton(submitButton);
		}
		
		private static class ToggleButton extends Button {

			private boolean val;
			private ModificationGui gui;
			
			public ToggleButton(int x, int y, boolean val, ModificationGui gui) {
				super(x, y, 200, 20, StringTextComponent.EMPTY, (b) -> {
					gui.onToggleButton((ToggleButton)b);
				});
				this.val = val;
				this.width = BUTTON_WIDTH;
				this.height = BUTTON_HEIGHT;
				this.gui = gui;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int parX, int parY, float partialTicks) {
				if (visible) {
					
					float tint = 1f;
					Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
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
					
					RenderSystem.color4f(tint, tint, tint, 1f);
					this.blit(matrixStackIn, this.x, this.y,
							BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET,
							this.width, this.height);
					RenderSystem.color4f(1f, 1f, 1f, 1f);
				}
			}
		}
		
		private static class FloatButton extends Button {
			private int val;
			private float actualVal;
			private ModificationGui gui;
			
			public FloatButton(int x, int y, int val, float actual, ModificationGui gui) {
				super(x, y, 200, 20, StringTextComponent.EMPTY, (b) -> {
					gui.onFloatButton((FloatButton) b);
				});
				this.val = val;
				this.actualVal = actual;
				this.width = gui.container.runeMode ? LARGE_BUTTON_WIDTH : 16;
				this.height = gui.container.runeMode ? LARGE_BUTTON_HEIGHT : 16;
				this.gui = gui;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int parX, int parY, float partialTicks) {
				if (visible) {
					
					// In rune mode, float buttons are buttons that display the number
					final Minecraft mc = Minecraft.getInstance();
					if (gui.container.runeMode) {
						float tint = 1f;
						mc.getTextureManager().bindTexture(TEXT);
						if (parX >= this.x && parY >= this.y
								&& parX <= this.x + this.width
								&& parY <= this.y + this.height) {
							tint = .8f;
						}
						
						int x = 0;
						if (gui.container.floatIndex != this.val)
							x += LARGE_BUTTON_WIDTH;
						
						int len;
						String text = String.format("%.1f", actualVal);
						len = mc.fontRenderer.getStringWidth(text);
						
						RenderSystem.color3f(tint, tint, tint);
						this.blit(matrixStackIn, this.x, this.y,
								BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET + BUTTON_HEIGHT,
								this.width, this.height);
						RenderSystem.color4f(1f, 1f, 1f, 1f);
						
						mc.fontRenderer.drawString(matrixStackIn, text,
								this.x + (LARGE_BUTTON_WIDTH / 2) - (len / 2),
								this.y + 1,
								0xFF000000);
					} else if (gui.container.scrollMode) {
						// In scroll mode, we show the icon they can select
						float tint = 1f;
						mc.getTextureManager().bindTexture(TEXT);
						if (parX >= this.x && parY >= this.y
								&& parX <= this.x + this.width
								&& parY <= this.y + this.height) {
							tint = .8f;
						}
						
						int x = 0;
						if (gui.container.floatIndex != this.val)
							x += LARGE_BUTTON_WIDTH;
						
						RenderSystem.color3f(tint, tint, tint);
						blit(matrixStackIn, this.x, this.y, BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET + BUTTON_HEIGHT,
								LARGE_BUTTON_WIDTH, LARGE_BUTTON_HEIGHT, this.width, this.height, 256, 256);
//						this.drawTexturedModalRect(this.x, this.y,
//								BUTTON_TEXT_HOFFSET + x, BUTTON_TEXT_VOFFSET + BUTTON_HEIGHT,
//								this.width, this.height);
						RenderSystem.color4f(1f, 1f, 1f, 1f);
						
						SpellIcon.get(this.val).render(mc, matrixStackIn, this.x + 2, this.y + 2, this.width - 4, this.height - 4, tint, tint, tint, 1f);
						
					}
				}
			}
		}
		
		private static class SubmitButton extends Button {
			
			private ModificationGui gui;
			
			public SubmitButton(int x, int y, ModificationGui gui) {
				super(x, y, 200, 20, StringTextComponent.EMPTY, (b) -> {
					gui.onSubmitButton();
				});
				this.width = LARGE_BUTTON_WIDTH;
				this.height = LARGE_BUTTON_HEIGHT;
				this.gui = gui;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int parX, int parY, float partialTicks) {
				if (visible) {
					
					float tint = 1f;
					if (parX >= this.x && parY >= this.y
							&& parX <= this.x + this.width
							&& parY <= this.y + this.height) {
						tint = .8f;
					}
					
					Minecraft.getInstance().getTextureManager().bindTexture(TEXT);
					RenderSystem.color3f(tint, tint, tint);
					int y = 0;
					if (gui.container.isValid)
						y += SUBMIT_HEIGHT;
					this.blit(matrixStackIn, this.x, this.y,
							SUBMIT_TEXT_HOFFSET, SUBMIT_TEXT_VOFFSET + y,
							this.width, this.height);
					RenderSystem.color4f(1f, 1f, 1f, 1f);
				}
			}
			
		}
	}
	
	private static class InputSlot extends Slot {
		
		private ItemStack required = ItemStack.EMPTY;
		private ModificationTableContainer container;

		public InputSlot(ModificationTableContainer container, IInventory inventoryIn, int index, int x, int y) {
			super(inventoryIn, index, x, y);
			this.container = container;
		}
		
		@Override
		public boolean isItemValid(@Nonnull ItemStack stack) {
			if (stack.isEmpty())
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
		
		public void setRequired(@Nonnull ItemStack required) {
			this.required = required;
		}
		
	}
}