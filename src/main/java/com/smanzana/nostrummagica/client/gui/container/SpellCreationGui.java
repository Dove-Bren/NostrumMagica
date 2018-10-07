package com.smanzana.nostrummagica.client.gui.container;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import org.lwjgl.opengl.GL11;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.SpellTable.SpellTableEntity;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellCraftMessage;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

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
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class SpellCreationGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/spell_create.png");
	
	private static final int GUI_WIDTH = 202;
	private static final int GUI_HEIGHT = 218;
	private static final int PLAYER_INV_HOFFSET = 23;
	private static final int PLAYER_INV_VOFFSET = 138;
	private static final int SLOT_MAIN_HOFFSET = 23;
	private static final int SLOT_MAIN_VOFFSET = 14;
	
	//23, 136
	
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
	private static final int MESSAGE_VALID_VOFFSET = 219;
	private static final int MESSAGE_DISPLAY_VOFFSET = 38;
	
	private static final int STATUS_WIDTH = 10;
	private static final int STATUS_HEIGHT = 10;
	private static final int STATUS_HOFFSET = 10;
	private static final int STATUS_VOFFSET = 219;
	private static final int STATUS_DISP_HOFFSET = 4;
	private static final int STATUS_DISP_VOFFSET = 4;
	
	private static final int REAGENT_BAG_HOFFSET = 23;
	private static final int REAGENT_BAG_VOFFSET = 112;
	
	private static final int MANA_VOFFSET = 99;
	
	public static class SpellCreationContainer extends Container {
		
		// Kept just to report to server which TE is doing crafting
		protected BlockPos pos;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected SpellTableEntity inventory;
		protected boolean isValid; // has an acceptable scroll
		protected boolean spellValid; // grammer checks out
		protected List<String> spellErrorStrings; // Updated on validate(); what's wrong?
		protected List<String> reagentStrings; // Updated on validate; what reagents will be used. Only filled if successful
		protected StringBuffer name;
		protected int lastManaCost;
		
		public SpellCreationContainer(IInventory playerInv, SpellTableEntity tableInventory, BlockPos pos) {
			this.inventory = tableInventory;
			this.pos = pos;
			
			spellErrorStrings = new LinkedList<>();
			this.name = new StringBuffer(NAME_MAX + 1);
			
			this.addSlotToContainer(new Slot(inventory, 0, SLOT_MAIN_HOFFSET, SLOT_MAIN_VOFFSET) {
				@Override
				public boolean isItemValid(@Nullable ItemStack stack) {
					return (stack == null
							|| stack.getItem() instanceof BlankScroll);
				}
				
				@Override
				public void putStack(@Nullable ItemStack stack) {
					super.putStack(stack);
					
					validate();
				}
				
				@Override
				public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
					validate();
					
					return super.onTake(playerIn, stack);
				}
				
				@Override
				public int getSlotStackLimit() {
					return 1;
				}
			});
			
			RuneSlot prev = null, cur;
			for (int i = 0; i < Math.min(GRAMMAR_SLOT_MAXX * 2, inventory.getRuneSlotCount()); i++) {
				int x = ( (i % GRAMMAR_SLOT_MAXX) * GRAMMAR_SLOT_HDIST + GRAMMAR_SLOT_HOFFSET);
				int y = ( (i / GRAMMAR_SLOT_MAXX) * GRAMMAR_SLOT_VDIST + GRAMMAR_SLOT_VOFFSET);
				cur = new RuneSlot(this, prev, inventory, i + 1, x, y);
				if (prev != null)
					prev.setNext(cur);
				prev = cur;
				this.addSlotToContainer(prev);
			}
			
			// Create reagent bag slots
			for (int i = 0; i < inventory.getReagentSlotCount(); i++) {
				int x = (i * 18) + REAGENT_BAG_HOFFSET;
				int y = REAGENT_BAG_VOFFSET;
				this.addSlotToContainer(new Slot(inventory, i + inventory.getReagentSlotIndex(), x, y) {
					@Override
					public int getSlotStackLimit() {
						return 64;
					}
					
					@Override
					public void putStack(@Nullable ItemStack stack) {
						super.putStack(stack);
						
						validate();
					}
					
					@Override
					public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
						validate();
						
						return super.onTake(playerIn, stack);
					}
				});
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
			Slot slot = (Slot) this.inventorySlots.get(fromSlot);
			
			if (slot != null && slot.getHasStack()) {
				ItemStack cur = slot.getStack();
				
				if (slot.inventory == this.inventory) {
					// Trying to take from the table
					ItemStack dupe = cur.copy();
					if (playerIn.inventory.addItemStackToInventory(dupe)) {
						slot.putStack(null);
						slot.onTake(playerIn, dupe);
					}
				} else {
					// Trying to add an item
					if (cur.getItem() instanceof ReagentItem) {
						// Adding a reagent. Try to add to reagent spots
						
						// Try to add to existing
						for (int i = 0; i < inventory.getReagentSlotCount(); i++) {
							ItemStack stack = inventory.getStackInSlot(i + inventory.getReagentSlotIndex());
							if (stack == null || stack.getItem() != cur.getItem()
									|| stack.getMetadata() != cur.getMetadata())
								continue;
							Slot reagentSlot = this.getSlot(i + inventory.getReagentSlotIndex());
							
							int maxsize = Math.min(stack.getMaxStackSize(), reagentSlot.getSlotStackLimit());
							int room = maxsize - stack.getCount();
							if (room >= cur.getCount()) {
								stack.setCount(stack.getCount() + cur.getCount());
								cur.setCount(0);
							} else {
								cur.setCount(cur.getCount() - room);
								stack.setCount(maxsize);
							}
							
							if (cur.getCount() <= 0)
								break;
						}
						
						// If still ahve items, add to empty slots
						if (cur.getCount() > 0)
						for (int i = 0; i < inventory.getReagentSlotCount(); i++) {
							ItemStack stack = inventory.getStackInSlot(i + inventory.getReagentSlotIndex());
							if (stack != null)
								continue;
							Slot reagentSlot = this.getSlot(i + inventory.getReagentSlotIndex());
							
							int maxsize = reagentSlot.getSlotStackLimit();
							if (maxsize >= cur.getCount()) {
								reagentSlot.putStack(cur.copy());
								cur.setCount(0);
							} else {
								reagentSlot.putStack(cur.splitStack(maxsize));
							}
							
							if (cur.getCount() <= 0)
								break;
						}
					} else if (cur.getItem() instanceof BlankScroll) {
						ItemStack existing = inventory.getStackInSlot(inventory.getScrollSlotIndex());
						if (existing == null) {
							inventory.setInventorySlotContents(inventory.getScrollSlotIndex(),
									cur.splitStack(1));
							this.validate();
						}
					} else if (cur.getItem() instanceof SpellRune) {
						// Only allow adding if blank scroll is in place
						ItemStack scroll = inventory.getStackInSlot(inventory.getScrollSlotIndex());
						if (scroll == null || !(scroll.getItem() instanceof BlankScroll)) {
							// Do nothing
						} else if (null != inventory.getStackInSlot(inventory.getRuneSlotIndex() + inventory.getRuneSlotCount() - 1)) {
							// If something's in last slot, we're full
							// Table will naturally shift things down
						} else {
							// If this is anything but shape or trigger, do nothing
							SpellComponentWrapper wrapper = SpellRune.toComponentWrapper(cur);
							boolean add = false;
							if (wrapper.isTrigger()) {
								// Can always add triggers
								add = true;
							} else if (wrapper.isShape()) {
								// Must have a trigger in first slot already
								if (null != inventory.getStackInSlot(inventory.getRuneSlotIndex()))
									add = true;
							}
							
							if (add) {
								int index = inventory.getRuneSlotIndex();
								while (inventory.getStackInSlot(index) != null)
									index++;
								
								inventory.setInventorySlotContents(index, cur.copy());
								cur = null;
								this.validate();
							}
						}
					}
				}
				
				if (cur == null || cur.getCount() <= 0) {
					slot.putStack(null);
				}
			}
			
			return null;
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
			validate(name.toString());
		}
		
		public void validate(String name) {
			if (spellErrorStrings == null)
				spellErrorStrings = new LinkedList<>();
			if (reagentStrings == null)
				reagentStrings = new LinkedList<>();
			
			Spell spell = makeSpell(name);
			spellValid = (spell != null);
		}
		
		public Spell makeSpell(String name) {
			return makeSpell(name, false);
		}
		
		public Spell makeSpell(String name, boolean clear) {
			// Don't cache from validate... just in case...
			Spell spell = craftSpell(name, this.inventory, this.spellErrorStrings, this.reagentStrings, isValid, clear);
			
			if (spell == null)
				return null;
			
			if (clear)
				this.inventory.clearBoard();
			
			this.lastManaCost = spell.getManaCost();
			return spell;
		}
		
		public static Spell craftSpell(String name, SpellTableEntity inventory,
				List<String> spellErrorStrings, List<String> reagentStrings,
				boolean isValid, boolean deductReagents) {
			boolean fail = false;
			spellErrorStrings.clear();
			reagentStrings.clear();
			if (name.trim().isEmpty()) {
				spellErrorStrings.add("Must have a name");
				fail = true;
			}
			
			if (!isValid) {
				spellErrorStrings.add("Missing blank scroll");
				return null; // fatal
			}
			
			ItemStack stack;
			
			stack = inventory.getStackInSlot(1);
			if (stack == null || !SpellRune.isTrigger(stack)) {
				spellErrorStrings.add("Spell must begin with a trigger");
				return null;
			}
			
			boolean flag = false;
			for (int i = 2; i < inventory.getReagentSlotIndex(); i++) {
				stack = inventory.getStackInSlot(i);
				if (stack == null) {
					break;
				}
				if (!SpellRune.isSpellWorthy(stack)) {
					spellErrorStrings.add("Rune in slot " + (i) + " is not allowed.");
					if (SpellRune.isShape(stack)) {
						spellErrorStrings.add("  -> Shapes must have an element attached to them.");
					} else {
						spellErrorStrings.add("  -> Elements and Alterations must be combined with a Shape first.");
					}
					return null;
				}
				if (SpellRune.isShape(stack)) {
					flag = true;
					break;
				}
			}
			
			if (!flag) {
				spellErrorStrings.add("Must have at least one spell shape");
				return null;
			}
			
			Spell spell = new Spell(name);
			SpellPart part;
			for (int i = 1; i < inventory.getReagentSlotIndex(); i++) {
				stack = inventory.getStackInSlot(i);
				if (stack == null) {
					break;
				}
				
				part = SpellRune.getPart(stack);
				if (part == null) {
					spellErrorStrings.add("Unfinished spell part in slot " + (i + 1));
					if (SpellRune.isShape(stack))
						spellErrorStrings.add(" -> Spell parts must have an element");
					else
						spellErrorStrings.add(" -> This trigger has been corrupted");
					return null;
				} else {
					spell.addPart(part);
				}
			}
			
			Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
			for (ReagentType type : reagents.keySet()) {
				if (type == null)
					continue;
				Integer count = reagents.get(type);
				if (count == null)
					continue;
				
				int left = takeReagent(inventory, type, count, false);
				if (left != 0) {
					spellErrorStrings.add("Need " + left + " more " + type.prettyName());
					fail = true;
				} else {
					reagentStrings.add(count + " " + type.prettyName());
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
					
					int left = takeReagent(inventory, type, count, true);
					if (left != 0) {
						System.out.println("Couldn't take all " + type.name());
						spellErrorStrings.add("Need " + left + " more " + type.prettyName());
						return null;
					}
					
				}
			}
			
			return spell;
		}
		
		public void setScroll(ItemStack item) {
			this.inventory.setInventorySlotContents(0, item);
			isValid = false;
		}

	}
	
	// if take, actually removes. Otherwise, just checks
	// returns amount needed still. 0 means all that were needed are there
	private static int takeReagent(SpellTableEntity inventory, ReagentType type, int count, boolean take) {
		for (int i = inventory.getReagentSlotIndex(); i < inventory.getReagentSlotIndex() + inventory.getReagentSlotCount(); i++) {
			ItemStack stack = inventory.getStackInSlot(i);
			if (stack == null)
				continue;
			
			if (ReagentItem.findType(stack) == type) {
				if (stack.getCount() > count) {
					if (take)
						inventory.decrStackSize(i, count);
					count = 0;
				} else {
					count -= stack.getCount();
					if (take)
						inventory.setInventorySlotContents(i, null);
				}
				
				if (count == 0)
					break;
			}
		}
		
		return count;
	}
	
	private static final int NAME_MAX = 20;
	
	@SideOnly(Side.CLIENT)
	public static class SpellGui extends GuiContainer {

		private SpellCreationContainer container;
		private int nameSelectedPos; // -1 for no selection
		private int counter;
		
		public SpellGui(SpellCreationContainer container) {
			super(container);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
			this.nameSelectedPos = -1;
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
			if (container.isValid) {
				
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
				mc.fontRenderer.drawString(container.name.toString(), 
						horizontalMargin + NAME_HOFFSET + 2,
						verticalMargin + NAME_VOFFSET + 2, 
						0xFF000000);
				if (nameSelectedPos != -1 && ++counter > 30) {
					
					x = horizontalMargin + NAME_HOFFSET + 2;
					for (int i = 0; i < nameSelectedPos; i++) {
						x += mc.fontRenderer.getCharWidth(container.name.charAt(i));
					}
					
					Gui.drawRect(x, verticalMargin + NAME_VOFFSET + 1,
							x + 1, verticalMargin + NAME_VOFFSET + 3 + mc.fontRenderer.FONT_HEIGHT,
							0xFF000000);
					
					if (counter > 60)
						counter = 0;
				}
				
				if (container.spellValid) {
					String str = "Spell Cost: " + container.lastManaCost;
					x = this.width / 2;
					x -= mc.fontRenderer.getStringWidth(str) / 2;
					mc.fontRenderer.drawString(str, x, verticalMargin + MANA_VOFFSET, 0xFFD3D3D3);
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
				
				if (mouseX >= horizontalMargin + SUBMIT_HOFFSET && mouseX <= horizontalMargin + SUBMIT_HOFFSET + SUBMIT_WIDTH && 
						mouseY >= verticalMargin + SUBMIT_VOFFSET && mouseY <= verticalMargin + SUBMIT_VOFFSET + SUBMIT_HEIGHT) {
					Gui.drawRect(SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x40000000);
					this.drawHoveringText(container.reagentStrings,
							mouseX - horizontalMargin, mouseY - verticalMargin);
				}
			}
			
			if (!container.isValid) {
				GlStateManager.pushMatrix();
				GlStateManager.translate(0, 0, 500);
				mc.getTextureManager().bindTexture(TEXT);
				Gui.drawModalRectWithCustomSizedTexture((GUI_WIDTH - MESSAGE_WIDTH) / 2,
						MESSAGE_DISPLAY_VOFFSET,
						MESSAGE_VALID_HOFFSET, MESSAGE_VALID_VOFFSET,
						MESSAGE_WIDTH, MESSAGE_HEIGHT,
						256, 256);
				GlStateManager.popMatrix();
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
							nameSelectedPos = container.name.length();
						} else {
							int offset = mouseX - left;
							offset -= 5; // offset of drawn text
							int index = 0;
							while (index < container.name.length() && offset >= mc.fontRenderer.getCharWidth(container.name.charAt(index))) {
								offset -= mc.fontRenderer.getCharWidth(container.name.charAt(index));
								index++;
							}
							nameSelectedPos = Math.min(container.name.length(), index + 1);
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
								Spell spell = container.makeSpell(container.name.toString(), true);
								if (spell != null) {
									// All of this happens again and is synced back to client
									// But in the mean, might as well do it here for the
									// smoothest feel
									ItemStack scroll = new ItemStack(SpellScroll.instance(), 1);
									SpellScroll.setSpell(scroll, spell);
									container.setScroll(scroll);
									//NostrumMagicaSounds.AMBIENT_WOOSH.play(Minecraft.getMinecraft().thePlayer);
									
									NetworkHandler.getSyncChannel().sendToServer(new SpellCraftMessage(
											container.name.toString(),
											container.pos
											));
									container.name.delete(0, container.name.length() - 1);
								}
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
				this.mc.player.closeScreen();
			}

			if (nameSelectedPos != -1 && isValidKey(keyCode)) {
				if (keyCode == 203) {
					nameSelectedPos = Math.max(0, nameSelectedPos - 1);
				} else if (keyCode == 205) {
					nameSelectedPos = Math.min(container.name.length(), nameSelectedPos + 1);
				} else if (keyCode == 199) {
					nameSelectedPos = 0;
				} else if (keyCode == 207) {
					nameSelectedPos = container.name.length();
				} else if (nameSelectedPos == container.name.length()) {
					if (keyCode == 14) {
						if (container.name.length() != 0) {
							container.name.deleteCharAt(container.name.length() - 1);
							nameSelectedPos--;
						}
						
					} else if(container.name.length() < NAME_MAX) {
						// if we dont have too many
						container.name.append(typedChar);
						nameSelectedPos++;
					}
				} else {
					// Typed into the middle of the string
					if (keyCode == 14) {
						container.name.deleteCharAt(nameSelectedPos);
						nameSelectedPos--;
					} else if (container.name.length() < NAME_MAX) {
						// if backspace or we dont have too many
						container.name.insert(nameSelectedPos, typedChar);
						nameSelectedPos++;
					}
				}
				
				container.validate();
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
			
			boolean trigger = SpellRune.isTrigger(stack);
			if (!trigger && !SpellRune.isShape(stack))
				return false;
			
			return (prev != null || trigger);
		}
		
		@Override
		@SideOnly(Side.CLIENT)
		public boolean isEnabled() {
			return (prev == null ||
					prev.getHasStack());
		}
		
		@Override
		public void putStack(@Nullable ItemStack stack) {
			super.putStack(stack);
			
			container.validate();
		}
		
		@Override
		public ItemStack onTake(EntityPlayer playerIn, ItemStack stack) {
			// This is called AFTER things have been changed or swapped
			// Which means we just look to see if we have an item.
			// If not, take item from next
			if (!this.getHasStack() && next != null && next.getHasStack()) {
				System.out.println("grabbing stack");
				this.putStack(next.getStack().copy());
				next.putStack(null);
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
	
}