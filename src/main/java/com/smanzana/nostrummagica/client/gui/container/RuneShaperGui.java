package com.smanzana.nostrummagica.client.gui.container;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.SpellComponentIcon;
import com.smanzana.nostrummagica.client.gui.container.SimpleInventoryWidget.SimpleInventoryContainerlet;
import com.smanzana.nostrummagica.items.SpellRune;
import com.smanzana.nostrummagica.items.SpellRune.AlterationSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.ElementSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.PackedShapeSpellRune;
import com.smanzana.nostrummagica.items.SpellRune.ShapeSpellRune;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.SpellPartProperties;
import com.smanzana.nostrummagica.spells.components.SpellAction;
import com.smanzana.nostrummagica.spells.components.SpellAction.SpellActionProperties;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.shapes.SingleShape;
import com.smanzana.nostrummagica.spells.components.triggers.ProximityTrigger;
import com.smanzana.nostrummagica.tiles.RuneShaperEntity;
import com.smanzana.nostrummagica.utils.ContainerUtil;
import com.smanzana.nostrummagica.utils.ContainerUtil.IPackedContainerProvider;
import com.smanzana.nostrummagica.utils.ContainerUtil.NoisySlot;
import com.smanzana.nostrummagica.utils.Inventories;
import com.smanzana.nostrummagica.utils.ItemStacks;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RuneShaperGui {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/rune_shaper.png");
	
	private static final int GUI_WIDTH = 202;
	private static final int GUI_HEIGHT = 221;
	private static final int PLAYER_INV_HOFFSET = 23;
	private static final int PLAYER_INV_VOFFSET = 140;
	
	private static final int SLOT_INPUT_HOFFSET = 45;
	private static final int SLOT_INPUT_VOFFSET = 17;
	private static final int SLOT_ELEM1_HOFFSET = 120;
	private static final int SLOT_ELEM1_VOFFSET = 7;
	private static final int SLOT_ELEM2_HOFFSET = 138;
	private static final int SLOT_ELEM2_VOFFSET = 7;
	private static final int SLOT_ELEM3_HOFFSET = 120;
	private static final int SLOT_ELEM3_VOFFSET = 25;
	private static final int SLOT_ELEM4_HOFFSET = 138;
	private static final int SLOT_ELEM4_VOFFSET = 25;
	private static final int SLOT_ALTER_HOFFSET = 169;
	private static final int SLOT_ALTER_VOFFSET = 15;
	private static final int SLOT_OUTPUT_HOFFSET = 95;
	private static final int SLOT_OUTPUT_VOFFSET = 107;
	
	//23, 136
	
	private static final int PANEL_HOFFSET = 3;
	private static final int PANEL_VOFFSET = 55;
	private static final int PANEL_WIDTH = 196;
	//private static final int PANEL_HEIGHT = 82;
	
	
	private static final int RUNE_BLANK_HOFFSET = 202;
	private static final int RUNE_BLANK_VOFFSET = 96;
	private static final int RUNE_BLANK_TEXT_WIDTH = 32;
	private static final int RUNE_BLANK_TEXT_HEIGHT = 32;
	
	public static class RuneShaperContainer extends Container {
		
		public static final String ID = "rune_shaper";
		
		// Actual container variables as well as a couple for keeping track
		// of crafting state
		protected RuneShaperEntity shaper;
		protected final CraftResultInventory resultInv;
		protected Slot outputSlot;
		protected SimpleInventoryContainerlet extraInv;
		
		public RuneShaperContainer(int windowId, PlayerEntity player, IInventory playerInv, RuneShaperEntity tableInventory, BlockPos pos, @Nullable IInventory extraInventory) {
			super(NostrumContainers.RuneShaper, windowId);
			this.shaper = tableInventory;
			this.resultInv = new CraftResultInventory();
			
			outputSlot = this.addSlot(new Slot(resultInv, 0, SLOT_OUTPUT_HOFFSET, SLOT_OUTPUT_VOFFSET) {
				@Override
				public boolean isItemValid(@Nonnull ItemStack stack) {
					return false; // Can't place into this slot
				}
				
				@Override
				public ItemStack onTake(PlayerEntity thePlayer, ItemStack stack) {
					return RuneShaperContainer.this.takeCraft(thePlayer, stack);
				}
			});
			
			// Shape slot
			this.addSlot(new NoisySlot(shaper, 0, SLOT_INPUT_HOFFSET, SLOT_INPUT_VOFFSET, this::inputsChanged));
			
			// Elem slots
			this.addSlot(new NoisySlot(shaper, 1, SLOT_ELEM1_HOFFSET, SLOT_ELEM1_VOFFSET, this::inputsChanged));
			this.addSlot(new NoisySlot(shaper, 2, SLOT_ELEM2_HOFFSET, SLOT_ELEM2_VOFFSET, this::inputsChanged));
			this.addSlot(new NoisySlot(shaper, 3, SLOT_ELEM3_HOFFSET, SLOT_ELEM3_VOFFSET, this::inputsChanged));
			this.addSlot(new NoisySlot(shaper, 4, SLOT_ELEM4_HOFFSET, SLOT_ELEM4_VOFFSET, this::inputsChanged));
			
			// Alteration slot
			this.addSlot(new NoisySlot(shaper, 5, SLOT_ALTER_HOFFSET, SLOT_ALTER_VOFFSET, this::inputsChanged));
			
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
			
			if (extraInventory != null) {
				final int height = 88;
				this.extraInv = new SimpleInventoryContainerlet(this::addSlot, extraInventory, HideableSlot::new, GUI_WIDTH, GUI_HEIGHT	- height, 100, height, new StringTextComponent("Chest"));
			}
			
			refreshOutput();
		}
		
		public static final RuneShaperContainer FromNetwork(int windowId, PlayerInventory playerInv, PacketBuffer buf) {
			RuneShaperEntity te = ContainerUtil.GetPackedTE(buf);
			return new RuneShaperContainer(windowId, playerInv.player, playerInv, te, buf.readBlockPos(), te.getExtraInventory());
		}
		
		public static final IPackedContainerProvider Make(RuneShaperEntity table) {
			return ContainerUtil.MakeProvider(ID, (windowId, playerInv, player) -> {
				return new RuneShaperContainer(windowId, player, playerInv, table, table.getPos(), table.getExtraInventory());
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
				
				if (slot.inventory == this.shaper) {
					// Trying to take our items
					if (playerIn.inventory.addItemStackToInventory(cur)) {
						slot.putStack(ItemStack.EMPTY);
						slot.onTake(playerIn, cur);
					}
				} else if (slot.inventory == this.resultInv) {
					// Trying to shift-click result slot
					
					// Remember each stack we're taking from for convenience
					List<Integer> takeSlots = new ArrayList<>(8);
					
					int craftCount = shaper.getStackInSlot(0).getCount(); // Start at shape count, and min everything
					takeSlots.add(0);
					
					for (int i = 1; i <= 4; i++) {
						ItemStack elemStack = shaper.getStackInSlot(i);
						if (!elemStack.isEmpty()) {
							craftCount = Math.min(craftCount, elemStack.getCount());
							takeSlots.add(i);
						}
					}
					
					{
						ItemStack alterationStack = shaper.getStackInSlot(5);
						if (!alterationStack.isEmpty()) {
							craftCount = Math.min(craftCount, alterationStack.getCount());
							takeSlots.add(5);
						}
					}
					
					ItemStack result = getOutput();
					result.setCount(craftCount);
					for (Integer idx : takeSlots) {
						shaper.decrStackSize(idx, craftCount);
					}
					
					result = Inventories.addItem(playerIn.inventory, result);
					if (!result.isEmpty()) {
						playerIn.dropItem(result, false);
					}
					this.refreshOutput();
					
				} else {
					// Trying to add an item
					if (SpellRune.isShape(cur) && !SpellRune.isPackedShape(cur)) {
						// Shape piece, so only care about first slot
						ItemStack existingStack = shaper.getStackInSlot(0);
						
						if (existingStack.isEmpty() || ItemStacks.stacksMatch(existingStack, cur)) {
							int room = cur.getMaxStackSize();
							if (!existingStack.isEmpty()) {
								room -= existingStack.getCount();
							}
							
							if (room > 0) {
								final ItemStack taken;
								final ItemStack remaining;
								if (existingStack.isEmpty()) {
									shaper.setInventorySlotContents(0, cur);
									taken = cur;
									remaining = ItemStack.EMPTY;
								} else {
									final int amt = room >= cur.getCount() ? cur.getCount() : room;
									existingStack.setCount(existingStack.getCount() + amt);
									shaper.setInventorySlotContents(0, existingStack); // generate change event
									taken = cur.split(amt);
									remaining = cur;
								}
								
								slot.putStack(remaining);
								slot.onTake(playerIn, taken);
								this.refreshOutput();
							}
						}
					} else if (SpellRune.isElement(cur)) {
						// Have 4 slots to try. Was going to try and spread elements out, but it's too much work.
						// Just allow dragging and let players do it the way they already know how.
						for (int i = 1; i <= 4 && !cur.isEmpty(); i++) {
							ItemStack existingStack = shaper.getStackInSlot(i);
							
							if (existingStack.isEmpty() || ItemStacks.stacksMatch(existingStack, cur)) {
								int room = cur.getMaxStackSize();
								if (!existingStack.isEmpty()) {
									room -= existingStack.getCount();
								}
								
								if (room > 0) {
									final ItemStack taken;
									if (existingStack.isEmpty()) {
										shaper.setInventorySlotContents(i, cur);
										taken = cur;
										cur = ItemStack.EMPTY;
									} else {
										final int amt = room >= cur.getCount() ? cur.getCount() : room;
										existingStack.setCount(existingStack.getCount() + amt);
										shaper.setInventorySlotContents(i, existingStack); // generate change event
										taken = cur.split(amt);
									}
									
									slot.putStack(cur);
									slot.onTake(playerIn, taken);
									this.refreshOutput();
								}
							}
						}
						
						
//						// Have 4 slots to try, and want to opt to fill them first and spread
//						// out across them...
//						
//						// First see if it matches the existing element, if any, and find how many stacks there are
//						int sum = 0;
//						EMagicElement existingElement = null;
//						for (int i = 1; i <= 4; i++) {
//							ItemStack elemStack = shaper.getStackInSlot(i);
//							if (elemStack.isEmpty() || !SpellRune.isElement(elemStack)) {
//								continue;
//							}
//							
//							ElementSpellRune rune = (ElementSpellRune) elemStack.getItem();
//							EMagicElement slotElem = rune.getElement();
//							
//							if (existingElement != null && existingElement != slotElem) {
//								return ItemStack.EMPTY;
//							}
//							
//							sum += elemStack.getCount();
//						}
//						
//						// Something tricky: if one stack has 50 and the other's have 1 and we are adding 3,
//						// we shouldn't pull down the 50 and instead add 1 to each of the other slots...
//						
//						// At this point, elements line up and stuff. Find out how many each stack should have
//						// when you include this stack
//						sum += cur.getCount();
//						
//						final int cntEach = 
					} else if (SpellRune.isAlteration(cur)) {
						// Like shape, but with alteration slot
						ItemStack existingStack = shaper.getStackInSlot(5);
						
						if (existingStack.isEmpty() || ItemStacks.stacksMatch(existingStack, cur)) {
							int room = cur.getMaxStackSize();
							if (!existingStack.isEmpty()) {
								room -= existingStack.getCount();
							}
							
							if (room > 0) {
								final ItemStack taken;
								final ItemStack remaining;
								if (existingStack.isEmpty()) {
									shaper.setInventorySlotContents(5, cur);
									taken = cur;
									remaining = ItemStack.EMPTY;
								} else {
									final int amt = room >= cur.getCount() ? cur.getCount() : room;
									existingStack.setCount(existingStack.getCount() + amt);
									shaper.setInventorySlotContents(5, existingStack); // generate change event
									taken = cur.split(amt);
									remaining = cur;
								}
								
								slot.putStack(remaining);
								slot.onTake(playerIn, taken);
								this.refreshOutput();
							}
						}
					}
					
					
//					Slot mainSlot = this.getSlot(0);
//					if (!mainSlot.getHasStack()) {
//						if (mainSlot.isItemValid(cur))
//							mainSlot.putStack(cur.split(1));
//					} else if (!inputSlot.getHasStack()) {
//						if (inputSlot.isItemValid(cur))
//							inputSlot.putStack(cur.split(1));
//					}
				}
				
				if (cur.isEmpty() || cur.getCount() <= 0) {
					slot.putStack(ItemStack.EMPTY);
				}
			}
			
			return ItemStack.EMPTY;
		}
		
		@Override
		public boolean canDragIntoSlot(Slot slotIn) {
			return slotIn.inventory != this.resultInv; // not in output slot
		}
		
		@Override
		public boolean canInteractWith(PlayerEntity playerIn) {
			return true;
		}
		
		protected @Nonnull ItemStack getOutput() {
			// Shape and some elems are required
			@Nullable SpellShape shape = null;
			@Nullable SpellPartProperties params = null;
			@Nullable EAlteration alteration = null;
			@Nullable EMagicElement element = null;
			int elementCount = 0;
			
			for (int i = 0; i < shaper.getSizeInventory(); i++) {
				ItemStack stack = shaper.getStackInSlot(i);
				if (stack.isEmpty())
					continue;
				
				if (!(stack.getItem() instanceof SpellRune))
					return ItemStack.EMPTY;
				
				if (SpellRune.isTrigger(stack)) {
					return ItemStack.EMPTY;
				}
				
				if (SpellRune.isPackedShape(stack)) {
					return ItemStack.EMPTY;
				}
				
				if (SpellRune.isShape(stack)) {
					if (shape != null) {
						return ItemStack.EMPTY; // Already found a shape
					}
					
					ShapeSpellRune shapeRune = (ShapeSpellRune) stack.getItem();
					shape = shapeRune.getShape();
					params = SpellRune.getPieceParam(stack);
					continue;
				}
				
				if (SpellRune.isAlteration(stack)) {
					if (alteration != null) {
						return ItemStack.EMPTY; // Only one alteration allowed
					}
					
					AlterationSpellRune altRune = (AlterationSpellRune) stack.getItem();
					alteration = altRune.getAlteration();
					continue;
				}
				
				if (SpellRune.isElement(stack)) {
					EMagicElement runeElem = ((ElementSpellRune) stack.getItem()).getElement();
					if (element != null && element != runeElem) {
						// Already have an element, and it's different than this rune's
						return ItemStack.EMPTY;
					}
					
					if (elementCount + 1 > 4) {
						// Have too many elements
						return ItemStack.EMPTY;
					}
					
					element = runeElem;
					elementCount += 1;
					continue;
				}
				
				NostrumMagica.logger.warn("Found unknown rune type while doing rune combine recipe");
				return ItemStack.EMPTY; // What is this?
			}
			
			// If we've got this far, we didn't find any dupes but we still may not have all required pieces. Check
			if (shape != null
					&& element != null // implies count [1-4]
					// not checking alteration because it 's optional
					) {
				
				// 1 => 1
				// 2 => 2
				// 3 => 2
				// 4 => 3
				// log2(count) + 1
				// log2(count) = log(count) / log(2)
				int elemLevel = 1 + (int) (Math.log(elementCount) / Math.log(2));
				
				ItemStack output = SpellRune.getRune(shape, element, elemLevel, alteration);
				SpellRune.setPieceParam(output, params);
				return output;
			}
			
			return ItemStack.EMPTY;
		}
		
		public void refreshOutput() {
			resultInv.setInventorySlotContents(0, getOutput());
		}
		
		protected void inputsChanged(NoisySlot changedSlot) {
			refreshOutput();
		}
		
		protected @Nonnull ItemStack takeCraft(PlayerEntity player, ItemStack output) {
			// Could recalculate output if needed, but trust the slot is right and just deduct ingredients
			
			for (int i = 0; i < shaper.getSizeInventory(); i++) {
				shaper.decrStackSize(i, 1);
			}

			this.refreshOutput();
			return output;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	public static class RuneShaperGuiContainer extends AutoGuiContainer<RuneShaperContainer> {

		private final RuneShaperContainer container;
		
		protected @Nullable SimpleInventoryWidget extraInventoryWidget;
		
		private @Nullable SpellAction lastAction = null;
		private @Nullable SpellActionProperties lastProps = null;
		
		public RuneShaperGuiContainer(RuneShaperContainer container, PlayerInventory playerInv, ITextComponent name) {
			super(container, playerInv, name);
			this.container = container;
			this.xSize = GUI_WIDTH;
			this.ySize = GUI_HEIGHT;
		}
		
		@Override
		public void init() {
			super.init();
			
			if (container.extraInv != null) {
				this.extraInventoryWidget = new SimpleInventoryWidget(this, container.extraInv);
				this.extraInventoryWidget.setColor(0xFF221F23);
				this.addButton(extraInventoryWidget);
			}
		}
		
		protected void drawAffectEntity(MatrixStack matrixStackIn, float[] color) {
			SpellComponentIcon.get(SingleShape.instance())
				.draw(this, matrixStackIn, this.font, 0, 0, 12, 12, color[0], color[1], color[2], color[3]);
		}
		
		protected void drawAffectBlock(MatrixStack matrixStackIn, float[] color) {
			SpellComponentIcon.get(ProximityTrigger.instance())
				.draw(this, matrixStackIn, this.font, 0, 0, 12, 12, color[0], color[1], color[2], color[3]);
		}
		
		@Override
		protected void drawGuiContainerBackgroundLayer(MatrixStack matrixStackIn, float partialTicks, int mouseX, int mouseY) {
			int horizontalMargin = (width - xSize) / 2;
			int verticalMargin = (height - ySize) / 2;
			
			mc.getTextureManager().bindTexture(TEXT);
			
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, horizontalMargin, verticalMargin,0, 0, GUI_WIDTH, GUI_HEIGHT, 256, 256);
			
			// Draw guiding shadows on empty slots
			{
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, horizontalMargin + SLOT_INPUT_HOFFSET + 1,
						verticalMargin + SLOT_INPUT_VOFFSET + 1, RUNE_BLANK_HOFFSET, RUNE_BLANK_VOFFSET, RUNE_BLANK_TEXT_WIDTH,
						RUNE_BLANK_TEXT_HEIGHT, 14, 14, 256, 256,
						1.0F,  1.0F, 1.0F, .4f);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, horizontalMargin + SLOT_ELEM1_HOFFSET + 1,
						verticalMargin + SLOT_ELEM1_VOFFSET + 1, RUNE_BLANK_HOFFSET, RUNE_BLANK_VOFFSET + RUNE_BLANK_TEXT_HEIGHT, RUNE_BLANK_TEXT_WIDTH,
						RUNE_BLANK_TEXT_HEIGHT, 14, 14, 256, 256,
						1.0F,  1.0F, 1.0F, .4f);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, horizontalMargin + SLOT_ELEM2_HOFFSET + 1,
						verticalMargin + SLOT_ELEM2_VOFFSET + 1, RUNE_BLANK_HOFFSET, RUNE_BLANK_VOFFSET + RUNE_BLANK_TEXT_HEIGHT, RUNE_BLANK_TEXT_WIDTH,
						RUNE_BLANK_TEXT_HEIGHT, 14, 14, 256, 256,
						1.0F,  1.0F, 1.0F, .4f);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, horizontalMargin + SLOT_ELEM3_HOFFSET + 1,
						verticalMargin + SLOT_ELEM3_VOFFSET + 1, RUNE_BLANK_HOFFSET, RUNE_BLANK_VOFFSET + RUNE_BLANK_TEXT_HEIGHT, RUNE_BLANK_TEXT_WIDTH,
						RUNE_BLANK_TEXT_HEIGHT, 14, 14, 256, 256,
						1.0F,  1.0F, 1.0F, .4f);
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, horizontalMargin + SLOT_ELEM4_HOFFSET + 1,
						verticalMargin + SLOT_ELEM4_VOFFSET + 1, RUNE_BLANK_HOFFSET, RUNE_BLANK_VOFFSET + RUNE_BLANK_TEXT_HEIGHT, RUNE_BLANK_TEXT_WIDTH,
						RUNE_BLANK_TEXT_HEIGHT, 14, 14, 256, 256,
						1.0F,  1.0F, 1.0F, .4f);
				
				RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, horizontalMargin + SLOT_ALTER_HOFFSET + 1,
						verticalMargin + SLOT_ALTER_VOFFSET + 1, RUNE_BLANK_HOFFSET, RUNE_BLANK_VOFFSET + RUNE_BLANK_TEXT_HEIGHT + RUNE_BLANK_TEXT_HEIGHT, RUNE_BLANK_TEXT_WIDTH,
						RUNE_BLANK_TEXT_HEIGHT, 14, 14, 256, 256,
						1.0F,  1.0F, 1.0F, .4f);
			}
			
			// Draw info about elements
			{
				
			}
			
			// Draw info about current rune output
			if (container.outputSlot.getHasStack()) {
				ItemStack output = container.outputSlot.getStack();
				PackedShapeSpellRune rune = (PackedShapeSpellRune) output.getItem();
				
				final EMagicElement element = rune.getNestedElement(output);
				final EAlteration alteration = rune.getNestedAlteration(output);
				final int elementLevel = rune.getNestedElementCount(output);
				
				INostrumMagic attr = NostrumMagica.getMagicWrapper(NostrumMagica.instance.proxy.getPlayer());
				final boolean known = attr != null && attr.hasKnowledge(element, alteration);
				final String name;
				final String desc;
				
				if (known) {
					final String suffix = elementLevel <= 1 ? ""
							: elementLevel <= 2 ? " II"
							: " III";
					
					lastAction = getAction(element, alteration);
					name = I18n.format("effect." + lastAction.getName() + ".name", (Object[]) null) + suffix;
					desc = I18n.format("effect." + lastAction.getName() + ".desc", (Object[]) null);
					
					lastProps = lastAction.getProperties();
					
				} else {
					name = "Unknown Effect";
					desc = "You haven't seen this effect before. Make a spell with it to find out what it does!";
					lastProps = null;
					lastAction = null;
				}
				
				int len = mc.fontRenderer.getStringWidth(name);
				mc.fontRenderer.drawStringWithShadow(matrixStackIn, name,
						horizontalMargin + (PANEL_HOFFSET) + (PANEL_WIDTH / 2) - (len / 2),
						verticalMargin + PANEL_VOFFSET + 5, 0xFFFFFFFF);
				
				RenderFuncs.drawSplitString(matrixStackIn, mc.fontRenderer, desc,
						horizontalMargin + (PANEL_HOFFSET) + 10,
						verticalMargin + PANEL_VOFFSET + 5 + 15,
						PANEL_WIDTH - 20,
						0xFFA0A0A0);
				
				if (lastProps != null) {
					matrixStackIn.push();
					matrixStackIn.translate(horizontalMargin + PANEL_WIDTH - (30), verticalMargin + PANEL_VOFFSET - 3, 0); // duped in foreground
					
					float color[] = {1f, 1f, 1f, 1f};
					if (!lastProps.affectsEntity) {
						color = new float[] {.3f, .3f, .3f, .4f};
					}
					drawAffectEntity(matrixStackIn, color);
					
					matrixStackIn.translate(12 + 4, 0, 0);
					if (lastProps.affectsBlock) {
						color = new float[] {1f, 1f, 1f, 1f};
					} else {
						color = new float[] {.3f, .3f, .3f, .4f};
					}
					drawAffectBlock(matrixStackIn, color);
					
					
					matrixStackIn.pop();
				}
			}
		}
		
		@Override
		protected void drawGuiContainerForegroundLayer(MatrixStack matrixStackIn, int mouseX, int mouseY) {
			final int horizontalMargin = (width - xSize) / 2;
			final int verticalMargin = (height - ySize) / 2;
			
			if (lastAction != null) {
				final int xAffectEntMin = horizontalMargin + PANEL_WIDTH - (30);
				final int xAffectEntMax = xAffectEntMin + 12;
				final int xAffectBlockMin = xAffectEntMax + 4;
				final int xAffectBlockMax = xAffectBlockMin + 12;
				final int yAffectMin = verticalMargin + PANEL_VOFFSET - 3;
				final int yAffectMax = yAffectMin + 12;
				
				if (lastProps.affectsEntity
						&& mouseX >= xAffectEntMin && mouseX <= xAffectEntMax
						&& mouseY >= yAffectMin && mouseY <= yAffectMax) {
					final ITextComponent s = new TranslationTextComponent("info.affects_entities");
					this.renderTooltip(matrixStackIn, s, mouseX - horizontalMargin, mouseY - verticalMargin);
				} else if (lastProps.affectsBlock
						&& mouseX >= xAffectBlockMin && mouseX <= xAffectBlockMax
						&& mouseY >= yAffectMin && mouseY <= yAffectMax) {
					final ITextComponent s = new TranslationTextComponent("info.affects_blocks");
					this.renderTooltip(matrixStackIn, s, mouseX - horizontalMargin, mouseY - verticalMargin);
				}
			}
		}
		
		private static final Map<EAlteration, Map<EMagicElement, SpellAction>> actionCache = new HashMap<>();
		
		protected static final SpellAction getAction(EMagicElement element, EAlteration alteration) {
			Map<EMagicElement, SpellAction> map = actionCache.get(alteration);
			if (map == null) {
				map = new EnumMap<>(EMagicElement.class);
				actionCache.put(alteration, map);
			}
			
			SpellAction action = map.get(element);
			if (action == null) {
				action = Spell.solveAction(null, alteration, element, 1);
				map.put(element, action);
			}
			
			return action;
		}
	}
}