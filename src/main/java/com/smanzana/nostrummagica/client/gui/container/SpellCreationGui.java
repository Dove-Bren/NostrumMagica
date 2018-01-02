package com.smanzana.nostrummagica.client.gui.container;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellCreationGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/spell_create.png");
	
	private static final int GUI_WIDTH = 202;
	private static final int GUI_HEIGHT = 203;
	private static final int PLAYER_INV_HOFFSET = 23;
	private static final int PLAYER_INV_VOFFSET = 122;
	private static final int SLOT_MAIN_HOFFSET = 23;
	private static final int SLOT_MAIN_VOFFSET = 14;
	
	private static final int GRAMMAR_SLOT_HOFFSET = 16;
	private static final int GRAMMAR_SLOT_VOFFSET = 42;
	private static final int GRAMMAR_SLOT_HDIST = 22;
	private static final int GRAMMAR_SLOT_VDIST = 39;
	private static final int GRAMMAR_SLOT_MAXX = 8;
	
	private static final int NAME_HOFFSET  = 47;
	private static final int NAME_VOFFSET = 18;
	private static final int NAME_WIDTH = 116;
	private static final int NAME_HEIGHT = 12;
	private static final int SUBMIT_HOFFSET = 172;
	private static final int SUBMIT_VOFFSET = 16;
	private static final int SUBMIT_WIDTH = 18;
	private static final int SUBMIT_HEIGHT = 10;
	
	private static final int MESSAGE_WIDTH = 144;
	private static final int MESSAGE_HEIGHT = 37;
	private static final int MESSAGE_VALID_HOFFSET = 30;
	private static final int MESSAGE_VALID_VOFFSET = 203;
	private static final int MESSAGE_DISPLAY_VOFFSET = 38;
	
	private static final int STATUS_WIDTH = 10;
	private static final int STATUS_HEIGHT = 10;
	private static final int STATUS_HOFFSET = 10;
	private static final int STATUS_VOFFSET = 203;
	private static final int STATUS_DISP_HOFFSET = 4;
	private static final int STATUS_DISP_VOFFSET = 4;
	
	private static final int MANA_VOFFSET = 103;
	
	public static class SpellCreationContainer extends Container {
		
		
		protected IInventory inventory;
		protected boolean isValid; // has an acceptable scroll
		protected boolean spellValid; // grammer checks out
		protected List<String> spellErrorStrings; // Updated on validate(); what's wrong?
		protected int lastManaCost;
		
		public SpellCreationContainer(IInventory playerInv, IInventory tableInventory) {
			this.inventory = tableInventory;
			
			spellErrorStrings = new LinkedList<>();
			
			//this.mySlot = 0;
			
			this.addSlotToContainer(new Slot(inventory, 0, SLOT_MAIN_HOFFSET, SLOT_MAIN_VOFFSET) {
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					return (stack == null
							|| stack.getItem() instanceof BlankScroll);
				}
			});
			
			RuneSlot prev = null, cur;
			for (int i = 0; i < Math.min(GRAMMAR_SLOT_MAXX * 2, inventory.getSizeInventory() - 1); i++) {
				int x = ( (i % GRAMMAR_SLOT_MAXX) * GRAMMAR_SLOT_HDIST + GRAMMAR_SLOT_HOFFSET);
				int y = ( (i / GRAMMAR_SLOT_MAXX) * GRAMMAR_SLOT_VDIST + GRAMMAR_SLOT_VOFFSET);
				cur = new RuneSlot(this, prev, inventory, i + 1, x, y);
				if (prev != null)
					prev.setNext(cur);
				prev = cur;
				this.addSlotToContainer(prev);
			}
			
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
			
			isValid = false;
			ItemStack stack = this.inventory.getStackInSlot(0);
			if (stack != null && (stack.getItem() instanceof SpellTome || stack.getItem() instanceof BlankScroll))
				isValid = true;
			
			return ret;
		}
		
		public void validate() {
			if (spellErrorStrings == null)
				spellErrorStrings = new LinkedList<>();
			
			spellErrorStrings.clear();
			
			spellErrorStrings.add("First line");
			spellErrorStrings.add("Second string");
			// set spellErrorStrings appropriately
			
			// set lastManaCost too
		}
		
		public Spell makeSpell(String name) {
			// TODO
			// clear out inventory, too
			return null;
		}
		
		public void setScroll(ItemStack item) {
			this.inventory.setInventorySlotContents(0, item);
			isValid = false;
		}

	}
	
	@SideOnly(Side.CLIENT)
	public static class SpellGui extends GuiContainer {

		private static final int NAME_MAX = 20;
		
		private SpellCreationContainer container;
		private int nameSelectedPos; // -1 for no selection
		private StringBuffer name;
		private int counter;
		
		public SpellGui(SpellCreationContainer container) {
			super(container);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
			this.nameSelectedPos = -1;
			this.name = new StringBuffer(NAME_MAX + 1);
			counter = 0;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(float partialTicks, int mouseX, int mouseY) {
			
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
			mc.getTextureManager().bindTexture(TEXT);
			
			Gui.drawModalRectWithCustomSizedTexture(horizontalMargin, verticalMargin, 0,0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			int x = (width - MESSAGE_WIDTH) / 2;
			int y = verticalMargin + MESSAGE_DISPLAY_VOFFSET;
			if (!container.isValid) {
				Gui.drawModalRectWithCustomSizedTexture(x, y,
						MESSAGE_VALID_HOFFSET, MESSAGE_VALID_VOFFSET,
						MESSAGE_WIDTH, MESSAGE_HEIGHT,
						256, 256);
			} else {
				
				GL11.glColor4f(1.0f, 1.0f, 1.0f, 1.0f);
				x = horizontalMargin + STATUS_DISP_HOFFSET;
				y = verticalMargin + STATUS_DISP_VOFFSET;
				int u = STATUS_HOFFSET;
				int v = STATUS_VOFFSET;
				if (!container.spellValid) {
					u += STATUS_WIDTH;
				}
				
				Gui.drawModalRectWithCustomSizedTexture(x, y, u, v,
						STATUS_WIDTH, STATUS_HEIGHT,
						256, 256);
				
				GL11.glPushMatrix();
				mc.fontRendererObj.drawString(name.toString(), 
						horizontalMargin + NAME_HOFFSET + 2,
						verticalMargin + NAME_VOFFSET + 2, 
						0xFF000000);
				if (nameSelectedPos != -1 && ++counter > 30) {
					
					x = horizontalMargin + NAME_HOFFSET + 2;
					for (int i = 0; i < nameSelectedPos; i++) {
						x += mc.fontRendererObj.getCharWidth(name.charAt(i));
					}
					
					Gui.drawRect(x, verticalMargin + NAME_VOFFSET + 1,
							x + 1, verticalMargin + NAME_VOFFSET + 3 + mc.fontRendererObj.FONT_HEIGHT,
							0xFF000000);
					
					if (counter > 60)
						counter = 0;
				}
				
				if (container.spellValid) {
					String str = "Spell Cost: " + container.lastManaCost;
					x = this.width / 2;
					x -= mc.fontRendererObj.getStringWidth(str) / 2;
					mc.fontRendererObj.drawString(str, x, verticalMargin + MANA_VOFFSET, 0xFFD3D3D3);
				}
				
				GL11.glPopMatrix();
				
			}
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(int mouseX, int mouseY) {
			
			if (container.isValid) {
				int horizontalMargin = (width - xSize) / 2;
				int verticalMargin = (height - ySize) / 2;
				
				if (!container.spellValid) {
					
					if (mouseX > horizontalMargin + STATUS_DISP_HOFFSET && mouseX <= horizontalMargin + STATUS_DISP_HOFFSET + STATUS_WIDTH
						 && mouseY > verticalMargin + STATUS_DISP_VOFFSET && mouseY <= verticalMargin + STATUS_DISP_VOFFSET + STATUS_HEIGHT) {
						GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
						
						this.drawHoveringText(container.spellErrorStrings,
								mouseX - horizontalMargin, mouseY - verticalMargin);
					}
				}
				
				if (mouseX > horizontalMargin + NAME_HOFFSET && mouseX <= horizontalMargin + NAME_HOFFSET + NAME_WIDTH
						 && mouseY > verticalMargin + NAME_VOFFSET && mouseY <= verticalMargin + NAME_VOFFSET + NAME_HEIGHT) {
					Gui.drawRect(NAME_HOFFSET, NAME_VOFFSET, NAME_HOFFSET + NAME_WIDTH, NAME_VOFFSET + NAME_HEIGHT, 0x40000000);
				}
			}
			
		}
			
		@Override
		protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
			int guiLeft = (width - xSize) / 2;
			int guiTop = (height - ySize) / 2;
			
			if (container.isValid) {
				int left = guiLeft + NAME_HOFFSET;
				int top = guiTop + NAME_VOFFSET;
			
				if (mouseX >= left && mouseX <= left + NAME_WIDTH && 
					mouseY >= top && mouseY <= top + NAME_HEIGHT) {
						// clicked in name field
						if (nameSelectedPos == -1) {
							nameSelectedPos = name.length();
						} else {
							int offset = mouseX - left;
							offset -= 5; // offset of drawn text
							int index = 0;
							while (index < name.length() && offset >= mc.fontRendererObj.getCharWidth(name.charAt(index))) {
								offset -= mc.fontRendererObj.getCharWidth(name.charAt(index));
								index++;
							}
							nameSelectedPos = index + 1;
						}
						counter = 0;
						return;
				}
				// implicit else
				nameSelectedPos = -1;
				left = guiLeft + SUBMIT_HOFFSET;
				top = guiTop + SUBMIT_VOFFSET;
				
				if (mouseX >= left && mouseX <= left + SUBMIT_WIDTH && 
						mouseY >= top && mouseY <= top + SUBMIT_HEIGHT) {
							// clicked on submit button
							container.validate();
							if (container.spellValid) {
								// whoo make spell
								Spell spell = container.makeSpell(name.toString());
								ItemStack scroll = new ItemStack(SpellScroll.instance(), 1);
								SpellScroll.setSpell(scroll, spell);
								container.setScroll(scroll);
							} else {
								// Don't
							}
							return;
					}
			}
			
			nameSelectedPos = -1;
			super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		protected void keyTyped(char typedChar, int keyCode) throws IOException {
			if (keyCode == 1) {
				this.mc.thePlayer.closeScreen();
			}

			if (nameSelectedPos != -1 && isValidKey(keyCode)) {
				if (keyCode == 203) {
					nameSelectedPos = Math.max(0, nameSelectedPos--);
				} else if (keyCode == 205) {
					nameSelectedPos = Math.min(name.length(), ++nameSelectedPos);
				} else if (keyCode == 199) {
					nameSelectedPos = 0;
				} else if (keyCode == 207) {
					nameSelectedPos = name.length();
				} else if (nameSelectedPos == name.length()) {
					if (keyCode == 14) {
						if (name.length() != 0) {
							name.deleteCharAt(name.length() - 1);
							nameSelectedPos--;
						}
						
					} else if(name.length() < NAME_MAX) {
						// if we dont have too many
						name.append(typedChar);
						nameSelectedPos++;
					}
				} else {
					// Typed into the middle of the string
					if (keyCode == 14) {
						name.deleteCharAt(nameSelectedPos);
						nameSelectedPos--;
					} else if (name.length() < NAME_MAX) {
						// if backspace or we dont have too many
						name.insert(nameSelectedPos, typedChar);
						nameSelectedPos++;
					}
				}
					
			} else {
				super.keyTyped(typedChar, keyCode);
			}
		}
	}
	
	private static boolean isValidKey(int keyCode) {
		// utility function for me <3
		return keyCode == 14 // backspace
				|| (keyCode >= 2 && keyCode <= 11) // numbers
				|| (keyCode >= 16 && keyCode <= 27) // qwerty row
				|| (keyCode >= 30 && keyCode <= 39) // asdf row (+ colon)
				|| (keyCode >= 44 && keyCode <= 53) // zxcv row
				|| keyCode == 57 // space
				|| keyCode == 203 // left arrow
				|| keyCode == 205 // right arrow
				|| keyCode == 199 // home
				|| keyCode == 207; // end
			
	}
	
	private static class RuneSlot extends Slot {

		private RuneSlot prev;
		private RuneSlot next;
		private SpellCreationContainer container;
		
		public RuneSlot(SpellCreationContainer container, RuneSlot prev, IInventory inventoryIn, int index, int xPosition, int yPosition) {
			super(inventoryIn, index, xPosition, yPosition);
			this.prev = prev;
			this.container = container;
		}
		
		public void setNext(RuneSlot next) {
			this.next = next;
		}
		
		@Override
		public boolean isItemValid(@Nullable ItemStack stack) {
			// Can put the item in if:
			// it's empty
			// OR previous slot is not null (not the first trigger-only slot)
			// OR it's a trigger rune
			// all ANDed with does the previous slot have a rune?
			if (!container.isValid)
				return false;
			
			if (prev != null &&
					!prev.getHasStack())
				return false;
			
			if (stack == null)
				return true;
			
			if (!(stack.getItem() instanceof SpellRune))
				return false;
			
			return (prev != null || SpellRune.isTrigger(stack));
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean canBeHovered() {
			return (prev == null ||
					prev.getHasStack());
		}
		
		@Override
		public void putStack(@Nullable ItemStack stack) {
			super.putStack(stack);
			
			container.validate();
		}
		
		@Override
		public void onPickupFromSlot(EntityPlayer playerIn, ItemStack stack) {
			// This is called AFTER things have been changed or swapped
			// Which means we just look to see if we have an item.
			// If not, take item from next
			if (!this.getHasStack() && next != null && next.getHasStack()) {
				this.putStack(next.getStack().copy());
				next.putStack(null);
				next.onPickupFromSlot(playerIn, this.getStack());
			}
			
			super.onPickupFromSlot(playerIn, stack);
		}
		
		@Override
		public int getSlotStackLimit() {
			return 1;
		}
	}
	
}