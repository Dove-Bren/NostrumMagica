package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.SpellIcon;
import com.smanzana.nostrummagica.items.BlankScroll;
import com.smanzana.nostrummagica.items.NostrumItems;
import com.smanzana.nostrummagica.items.ReagentItem;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.SpellCraftMessage;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.SpellCrafting;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.tiles.SpellTableEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.widget.button.Button;
import net.minecraft.client.renderer.Rectangle2d;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ClickType;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MasterSpellCreationGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/spell_create_master.png");
	
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
	
	private static final int ICON_LBUTTON_HOFFSET = 174;
	private static final int ICON_LBUTTON_VOFFSET = 219; 
	private static final int ICON_BUTTON_LENGTH = 16;
	
	private static final int MANA_VOFFSET = 99;
	
	public static class SpellCreationContainer extends Container {
		
		public static final String ID = "spell_creation";
		
		// Kept just to report to server which TE is doing crafting
		protected final BlockPos pos;
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected final SpellTableEntity inventory;
		protected final PlayerEntity player;
		protected boolean isValid; // has an acceptable scroll
		protected boolean spellValid; // grammer checks out
		protected List<ITextComponent> spellErrorStrings; // Updated on validate(); what's wrong?
		protected List<ITextComponent> reagentStrings; // Updated on validate; what reagents will be used. Only filled if successful
		protected String name;
		protected int iconIndex; // -1 indicates none has been selected yet
		protected int lastManaCost;
		protected int lastWeight;
		
		public SpellCreationContainer(int windowId, PlayerEntity crafter, PlayerInventory playerInv, SpellTableEntity tableInventory) {
			super(NostrumContainers.SpellCreationMaster, windowId);
			this.inventory = tableInventory;
			this.player = crafter;
			this.pos = tableInventory.getPos();
			
			spellErrorStrings = new LinkedList<>();
			this.name = "";
			this.iconIndex = -1;
			
			this.addSlot(new Slot(inventory, 0, SLOT_MAIN_HOFFSET, SLOT_MAIN_VOFFSET) {
				@Override
				public boolean isItemValid(@Nonnull ItemStack stack) {
					return (stack.isEmpty()
							|| stack.getItem() instanceof BlankScroll);
				}
				
				@Override
				public void putStack(@Nonnull ItemStack stack) {
					super.putStack(stack);
					
					validate();
				}
				
				@Override
				public ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
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
				this.addSlot(prev);
			}
			
			// Create reagent bag slots
			for (int i = 0; i < inventory.getReagentSlotCount(); i++) {
				int x = (i * 18) + REAGENT_BAG_HOFFSET;
				int y = REAGENT_BAG_VOFFSET;
				this.addSlot(new Slot(inventory, i + inventory.getReagentSlotIndex(), x, y) {
					@Override
					public int getSlotStackLimit() {
						return 64;
					}
					
					@Override
					public void putStack(@Nonnull ItemStack stack) {
						super.putStack(stack);
						
						validate();
					}
					
					@Override
					public @Nonnull ItemStack onTake(PlayerEntity playerIn, ItemStack stack) {
						validate();
						
						return super.onTake(playerIn, stack);
					}
				});
			}
			
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
			
			// isValid means there's something that can accept a spell
			// in the tome slot. Is there?
			isValid = false;
			@Nonnull ItemStack stack = this.inventory.getStackInSlot(0);
			if (!stack.isEmpty() && stack.getItem() instanceof BlankScroll)
				isValid = true;
			
			validate();
			
		}
		
		public static final SpellCreationContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buffer) {
			return new SpellCreationContainer(windowId, playerInv.player, playerInv, ContainerUtil.GetPackedTE(buffer));
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.inventory; // It's NOT bag inventory
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		public static IPackedContainerProvider Make(SpellTableEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new SpellCreationContainer(windowId, player, playerInv, table);
			}, (buffer) -> {
				ContainerUtil.PackTE(buffer, table);
			});
		}
		
		@Override
		public ItemStack slotClick(int slotId, int dragType, ClickType clickTypeIn, PlayerEntity player) {
			ItemStack ret = super.slotClick(slotId, dragType, clickTypeIn, player);
			
			isValid = false;
			ItemStack stack = this.inventory.getStackInSlot(0);
			if (!stack.isEmpty() && (stack.getItem() instanceof SpellTome || stack.getItem() instanceof BlankScroll))
				isValid = true;
			
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
					if (cur.getItem() instanceof ReagentItem) {
						// Adding a reagent. Try to add to reagent spots
						
						// Try to add to existing
						for (int i = 0; i < inventory.getReagentSlotCount(); i++) {
							ItemStack stack = inventory.getStackInSlot(i + inventory.getReagentSlotIndex());
							if (stack.isEmpty() || stack.getItem() != cur.getItem())
								continue;
							Slot reagentSlot = this.getSlot(i + inventory.getReagentSlotIndex());
							
							int maxsize = Math.min(stack.getMaxStackSize(), reagentSlot.getSlotStackLimit());
							int room = maxsize - stack.getCount();
							if (room >= cur.getCount()) {
								stack.grow(cur.getCount());
								cur.setCount(0);
							} else {
								cur.shrink(room);
								stack.setCount(maxsize);
							}
							
							if (cur.getCount() <= 0)
								break;
						}
						
						// If still ahve items, add to empty slots
						if (!cur.isEmpty())
						for (int i = 0; i < inventory.getReagentSlotCount(); i++) {
							ItemStack stack = inventory.getStackInSlot(i + inventory.getReagentSlotIndex());
							if (!stack.isEmpty())
								continue;
							Slot reagentSlot = this.getSlot(i + inventory.getReagentSlotIndex());
							
							int maxsize = reagentSlot.getSlotStackLimit();
							if (maxsize >= cur.getCount()) {
								reagentSlot.putStack(cur.copy());
								cur.setCount(0);
							} else {
								reagentSlot.putStack(cur.split(maxsize));
							}
							
							if (cur.isEmpty())
								break;
						}
					} else if (cur.getItem() instanceof BlankScroll) {
						ItemStack existing = inventory.getStackInSlot(inventory.getScrollSlotIndex());
						if (existing.isEmpty()) {
							inventory.setInventorySlotContents(inventory.getScrollSlotIndex(),
									cur.split(1));
							this.validate();
						}
					} else if (cur.getItem() instanceof SpellRune) {
						// Only allow adding if blank scroll is in place
						ItemStack scroll = inventory.getStackInSlot(inventory.getScrollSlotIndex());
						if (scroll.isEmpty() || !(scroll.getItem() instanceof BlankScroll)) {
							// Do nothing
						} else if (!inventory.getStackInSlot(inventory.getRuneSlotIndex() + inventory.getRuneSlotCount() - 1).isEmpty()) {
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
								if (!inventory.getStackInSlot(inventory.getRuneSlotIndex()).isEmpty())
									add = true;
							}
							
							if (add) {
								int index = inventory.getRuneSlotIndex();
								while (!inventory.getStackInSlot(index).isEmpty())
									index++;
								
								inventory.setInventorySlotContents(index, cur.split(1));
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
		
		public void validate() {
			validate(name.toString(), this.iconIndex);
		}
		
		public void validate(String name, int iconIdx) {
			if (spellErrorStrings == null)
				spellErrorStrings = new LinkedList<>();
			if (reagentStrings == null)
				reagentStrings = new LinkedList<>();
			
			Spell spell = makeSpell(name, iconIdx);
			spellValid = (spell != null);
		}
		
		public Spell makeSpell(String name, int iconIdx) {
			return makeSpell(name, iconIdx, false);
		}
		
		public Spell makeSpell(String name, int iconIdx, boolean clear) {
			// Don't cache from validate... just in case...
			Spell spell = craftSpell(name, iconIdx, this.inventory, this.player, this.spellErrorStrings, this.reagentStrings, clear);
			
			if (spell == null)
				return null;
			
			if (clear)
				this.inventory.clearBoard();
			
			this.lastManaCost = spell.getManaCost();
			this.lastWeight = spell.getWeight();
			return spell;
		}
		
		public static Spell craftSpell(String name, int iconIdx, SpellTableEntity inventory, PlayerEntity crafter,
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
			if (!SpellCrafting.CheckForValidRunes(inventory, 1, inventory.getReagentSlotIndex()-1, rawSpellErrors)) {
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
			SpellCraftContext context = new SpellCraftContext(crafter, inventory.getWorld(), inventory.getPos());
			Spell spell = SpellCrafting.CreateSpellFromRunes(context, null, name, inventory, 1, inventory.getReagentSlotIndex()-1);
			
			// Do reagent check
			Map<ReagentType, Integer> reagents = spell.getRequiredReagents();
			for (ReagentType type : reagents.keySet()) {
				if (type == null)
					continue;
				Integer count = reagents.get(type);
				if (count == null)
					continue;
				
				int left = takeReagent(inventory, type, count, false);
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
					
					int left = takeReagent(inventory, type, count, true);
					if (left != 0) {
						System.out.println("Couldn't take all " + type.name());
						spellErrorStrings.add(new StringTextComponent("Need " + left + " more " + type.prettyName()));
						return null;
					}
					
				}
			}
			
			return spell;
		}
		
		public void setScroll(@Nonnull ItemStack item) {
			this.inventory.setInventorySlotContents(0, item);
			isValid = false;
		}

	}
	
	// if take, actually removes. Otherwise, just checks
	// returns amount needed still. 0 means all that were needed are there
	private static int takeReagent(SpellTableEntity inventory, ReagentType type, int count, boolean take) {
		for (int i = inventory.getReagentSlotIndex(); i < inventory.getReagentSlotIndex() + inventory.getReagentSlotCount(); i++) {
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
	
	private static final int NAME_MAX = 20;
	
	@OnlyIn(Dist.CLIENT)
	public static class SpellGui extends AutoGuiContainer<SpellCreationContainer> implements IJEIAwareGuiContainer {
		
		private static class SpellIconButton extends Button {
			
			private int value;
			private SpellGui gui;
			
			public SpellIconButton(int x, int y, int val, SpellGui gui) {
				super(x, y, ICON_BUTTON_LENGTH, ICON_BUTTON_LENGTH, StringTextComponent.EMPTY, (b) -> {
					gui.iconButtonClicked(b);
				});
				this.value = val;
				this.width = ICON_BUTTON_LENGTH;
				this.height = ICON_BUTTON_LENGTH;
				this.gui = gui;
			}
			
			@Override
			public void render(MatrixStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
				final Minecraft mc = Minecraft.getInstance();
				float tint = 1f;
				mc.getTextureManager().bindTexture(TEXT);
				if (mouseX >= this.x && mouseY >= this.y
						&& mouseX <= this.x + this.width
						&& mouseY <= this.y + this.height) {
					tint = .8f;
				}
				
				int x = 0;
				if (gui.container.iconIndex != this.value)
					x += 20;
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, this.x, this.y, ICON_LBUTTON_HOFFSET + x,
						ICON_LBUTTON_VOFFSET, 20, 20, this.width, this.height, 256, 256,
						tint, tint, tint, 1f);
				
				SpellIcon.get(this.value).render(mc, matrixStackIn, this.x + 2, this.y + 2, this.width - 4, this.height - 4,
						tint, tint, tint, 1f);
			}
			
		}

		private SpellCreationContainer container;
		private List<SpellIconButton> buttons;
		private TextFieldWidget nameField;
		private Rectangle2d iconArea;
		
		public SpellGui(SpellCreationContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
			final Minecraft mc = Minecraft.getInstance();
			this.nameField = new TextFieldWidget(mc.fontRenderer, 0, 0, NAME_WIDTH, NAME_HEIGHT, new StringTextComponent(container.name));
			this.nameField.setMaxStringLength(NAME_MAX);
			this.nameField.setResponder((s) -> {
				container.name = s;
				container.validate();
			});
			this.nameField.setValidator((s) -> {
				// do this better? If it ends up sucking. Otherwise this is probably fine
				return s.codePoints().allMatch(MasterSpellCreationGui::isValidChar);
			});
			this.buttons = new ArrayList<>(SpellIcon.numIcons);
			
			this.addButton(nameField);
		}
		
		@Override
		public void init() {
			buttons.clear();
			
			super.init();
			
			int extraMargin = 3;
			final int horizontalMargin = ((width - xSize) / 2);
			final int verticalMargin = (height - ySize) / 2;
			final int spaceWidth = horizontalMargin - (2 * extraMargin); // amount of space to draw in
			
			final int perRow = spaceWidth / ICON_BUTTON_LENGTH;
			extraMargin += (spaceWidth % ICON_BUTTON_LENGTH) / 2; // Center by adding remainder / 2
			
			iconArea = new Rectangle2d(extraMargin, verticalMargin, horizontalMargin - extraMargin, ((SpellIcon.numIcons / perRow) + 1) * ICON_BUTTON_LENGTH);
			for (int i = 0; i < SpellIcon.numIcons; i++) {
				SpellIconButton button = new SpellIconButton(
						extraMargin + (i % perRow) * ICON_BUTTON_LENGTH,
						verticalMargin + (i / perRow) * ICON_BUTTON_LENGTH,
						i,
						this);
						//int buttonId, int x, int y, int val, float actual, SpellCreationContainer container
				
				this.buttons.add(button);
				this.addButton(button);
			}
			
			this.addButton(nameField);
			this.nameField.x = horizontalMargin + NAME_HOFFSET;
			this.nameField.y = verticalMargin + NAME_VOFFSET;
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			int x = (width - MESSAGE_WIDTH) / 2;
			int y = verticalMargin + MESSAGE_DISPLAY_VOFFSET;
			if (container.isValid) {
				
				x = horizontalMargin + STATUS_DISP_HOFFSET;
				y = verticalMargin + STATUS_DISP_VOFFSET;
				int u = STATUS_HOFFSET;
				int v = STATUS_VOFFSET;
				if (!container.spellValid) {
					u += STATUS_WIDTH;
				}
				
				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, x, y, u,
						v, STATUS_WIDTH,
						STATUS_HEIGHT, 256, 256);
				
				if (container.spellValid) {
					String costStr = "Mana Cost: " + container.lastManaCost;
					String weightStr = "Weight: " + container.lastWeight;
					final int weightStrLen = mc.fontRenderer.getStringWidth(weightStr);
					final int margin = 10;
					
					mc.fontRenderer.drawString(matrixStackIn, costStr, horizontalMargin + margin, verticalMargin + MANA_VOFFSET, 0xFFD3D3D3);
					mc.fontRenderer.drawString(matrixStackIn, weightStr, horizontalMargin + xSize - (margin + weightStrLen), verticalMargin + MANA_VOFFSET, 0xFFD3D3D3);
				}
			}
			
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			
			if (container.isValid) {
				int horizontalMargin = (width - xSize) / 2;
				int verticalMargin = (height - ySize) / 2;
				
				if (!container.spellValid) {
					
					if (mouseX > horizontalMargin + STATUS_DISP_HOFFSET && mouseX <= horizontalMargin + STATUS_DISP_HOFFSET + STATUS_WIDTH
						 && mouseY > verticalMargin + STATUS_DISP_VOFFSET && mouseY <= verticalMargin + STATUS_DISP_VOFFSET + STATUS_HEIGHT) {
						this.func_243308_b(matrixStackIn, container.spellErrorStrings,
								mouseX - horizontalMargin, mouseY - verticalMargin);
					}
				}
				
				if (mouseX > horizontalMargin + NAME_HOFFSET && mouseX <= horizontalMargin + NAME_HOFFSET + NAME_WIDTH
						 && mouseY > verticalMargin + NAME_VOFFSET && mouseY <= verticalMargin + NAME_VOFFSET + NAME_HEIGHT) {
					RenderFuncs.drawRect(matrixStackIn, NAME_HOFFSET, NAME_VOFFSET, NAME_HOFFSET + NAME_WIDTH, NAME_VOFFSET + NAME_HEIGHT, 0x40000000);
				}
				
				if (mouseX >= horizontalMargin + SUBMIT_HOFFSET && mouseX <= horizontalMargin + SUBMIT_HOFFSET + SUBMIT_WIDTH && 
						mouseY >= verticalMargin + SUBMIT_VOFFSET && mouseY <= verticalMargin + SUBMIT_VOFFSET + SUBMIT_HEIGHT) {
					RenderFuncs.drawRect(matrixStackIn, SUBMIT_HOFFSET, SUBMIT_VOFFSET, SUBMIT_HOFFSET + SUBMIT_WIDTH, SUBMIT_VOFFSET + SUBMIT_HEIGHT, 0x40000000);
					this.func_243308_b(matrixStackIn, container.reagentStrings,
							mouseX - horizontalMargin, mouseY - verticalMargin);
				}
			}
			
			if (!container.isValid) {
				matrixStackIn.push();
				matrixStackIn.translate(0, 0, 500);
				mc.getTextureManager().bindTexture(TEXT);
				RenderSystem.enableBlend();
				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn,
						(GUI_WIDTH - MESSAGE_WIDTH) / 2,
						MESSAGE_DISPLAY_VOFFSET, MESSAGE_VALID_HOFFSET,
						MESSAGE_VALID_VOFFSET, MESSAGE_WIDTH,
						MESSAGE_HEIGHT, 256, 256);
				matrixStackIn.pop();
			}
			
		}
		
		protected void iconButtonClicked(Button buttonIn) {
			// Only type of button we have are icon buttons
			SpellIconButton button = (SpellIconButton) buttonIn;
			
			container.iconIndex = button.value;
			container.validate();
		}
			
		@Override
		public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
			int guiLeft = (width - xSize) / 2;
			int guiTop = (height - ySize) / 2;
			
			if (container.isValid) {
				int left = guiLeft + NAME_HOFFSET;
				int top = guiTop + NAME_VOFFSET;
			
				left = guiLeft + SUBMIT_HOFFSET;
				top = guiTop + SUBMIT_VOFFSET;
				
				if (mouseX >= left && mouseX <= left + SUBMIT_WIDTH && 
						mouseY >= top && mouseY <= top + SUBMIT_HEIGHT) {
							// clicked on submit button
							container.validate();
							if (container.spellValid) {
								// whoo make spell
								Spell spell = container.makeSpell(container.name.toString(), container.iconIndex, true);
								if (spell != null) {
									// All of this happens again and is synced back to client
									// But in the mean, might as well do it here for the
									// smoothest feel
									ItemStack scroll = new ItemStack(NostrumItems.spellScroll, 1);
									SpellScroll.setSpell(scroll, spell);
									container.setScroll(scroll);
									//NostrumMagicaSounds.AMBIENT_WOOSH.play(Minecraft.getInstance().thePlayer);
									
									NetworkHandler.sendToServer(new SpellCraftMessage(
											container.name.toString(),
											container.pos,
											container.iconIndex
											));
									container.name = "";
									this.nameField.setText("");
									container.iconIndex = -1;
								}
							} else {
								// Don't
							}
							return true;
					}
			}
			
			return super.mouseClicked(mouseX, mouseY, mouseButton);
		}
		
		@Override
		public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
			if (p_keyPressed_1_ == 256) {
				this.mc.player.closeScreen();
			}

			// Copied from AnvilScreen
			return !this.nameField.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) && !this.nameField.canWrite() ? super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_) : true;
		}
		
		@Override
		public List<Rectangle2d> getGuiExtraAreas() {
			return Lists.newArrayList(iconArea);
		}
	}
	
	private static boolean isValidChar(int codepoint) {
		return Character.isAlphabetic(codepoint) || Character.isDigit(codepoint) || Character.isSpaceChar(codepoint);
	}
	
	private static class RuneSlot extends Slot {

		private RuneSlot prev;
		private RuneSlot next;
		private SpellCreationContainer container;
		
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
			if (!container.isValid)
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
				System.out.println("grabbing stack");
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
	
}