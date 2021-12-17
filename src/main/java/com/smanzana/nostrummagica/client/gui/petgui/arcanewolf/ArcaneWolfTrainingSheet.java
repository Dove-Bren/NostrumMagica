package com.smanzana.nostrummagica.client.gui.petgui.arcanewolf;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.petgui.IPetGUISheet;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.ArcaneWolfElementalType;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf.WolfBondCapability;
import com.smanzana.nostrummagica.items.InfusedGemItem;
import com.smanzana.nostrummagica.items.MasteryOrb;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class ArcaneWolfTrainingSheet implements IPetGUISheet<EntityArcaneWolf> {
	
	protected static enum SlotMode {
		NONE, // No slots should be shown because all training is done or training is in progress
		PRIMARY, // No training at all has happened, so primary slots + master slot should show
		SECONDARY, // Primary training has happened and secondary can too. Show secondary slots.
	}
	
	protected static final ResourceLocation TEX_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/gui/container/arcane_wolf_training.png");
	private static int GUI_SLOT_ICON_HOFFSET = 0;
	private static int GUI_SLOT_ICON_VOFFSET = 0;
	private static int GUI_SLOT_ICON_WIDTH = 24;
	private static int GUI_SLOT_ICON_HEIGHT = 16;
	
	private static int GUI_CENTER_GEM_SOCKET_HOFFSET = 0;
	private static int GUI_CENTER_GEM_SOCKET_VOFFSET = GUI_SLOT_ICON_VOFFSET + GUI_SLOT_ICON_HEIGHT;
	private static int GUI_CENTER_GEM_SOCKET_WIDTH = 63;
	private static int GUI_CENTER_GEM_SOCKET_HEIGHT = 63;
	
	private static int GUI_CENTER_GEM_HOFFSET = GUI_CENTER_GEM_SOCKET_HOFFSET + GUI_CENTER_GEM_SOCKET_WIDTH;
	private static int GUI_CENTER_GEM_VOFFSET = GUI_CENTER_GEM_SOCKET_VOFFSET;
	private static int GUI_CENTER_GEM_WIDTH = GUI_CENTER_GEM_SOCKET_WIDTH;
	private static int GUI_CENTER_GEM_HEIGHT = GUI_CENTER_GEM_SOCKET_HEIGHT;
	
	private static int GUI_GEM_EXTRAS_1_HOFFSET = 0;
	private static int GUI_GEM_EXTRAS_1_VOFFSET = GUI_CENTER_GEM_VOFFSET + GUI_CENTER_GEM_SOCKET_HEIGHT;
	private static int GUI_GEM_EXTRAS_1_WIDTH = 109;
	private static int GUI_GEM_EXTRAS_1_HEIGHT = 42;
	
	private static int GUI_GEM_EXTRAS_2_HOFFSET = GUI_GEM_EXTRAS_1_HOFFSET + GUI_GEM_EXTRAS_1_WIDTH;
	private static int GUI_GEM_EXTRAS_2_VOFFSET = GUI_GEM_EXTRAS_1_VOFFSET;
	private static int GUI_GEM_EXTRAS_2_WIDTH = 131;
	private static int GUI_GEM_EXTRAS_2_HEIGHT = GUI_GEM_EXTRAS_1_HEIGHT;
	
	private static int GUI_GEM_EXTRAS_3_HOFFSET = 0;
	private static int GUI_GEM_EXTRAS_3_VOFFSET = GUI_GEM_EXTRAS_1_VOFFSET + GUI_GEM_EXTRAS_1_HEIGHT;
	private static int GUI_GEM_EXTRAS_3_WIDTH = 17;
	private static int GUI_GEM_EXTRAS_3_HEIGHT = 11;
	
	private static int GUI_GEM_SPARK_HOFFSET = 0;
	private static int GUI_GEM_SPARK_VOFFSET = GUI_GEM_EXTRAS_3_VOFFSET + GUI_GEM_EXTRAS_3_HEIGHT;
	private static int GUI_GEM_SPARK_WIDTH = 21;
	private static int GUI_GEM_SPARK_HEIGHT = 21;
	
	protected final EntityArcaneWolf pet;
	protected final InventoryBasic localInv;
	
	public ArcaneWolfTrainingSheet(EntityArcaneWolf pet) {
		this.pet = pet;
		localInv = new InventoryBasic("", false, 5);
	}
	
	protected SlotMode getSlotMode(EntityArcaneWolf wolf) {
		if (wolf.getTrainingElement() != null) {
			return SlotMode.NONE;
		}
		
		final ArcaneWolfElementalType type = wolf.getElementalType();
		if (type.getSecondary() != null) {
			// Can't train anymore
			return SlotMode.NONE;
		}
		
		if (type.getPrimary() == null) {
			return SlotMode.PRIMARY;
		}
		
		return SlotMode.SECONDARY;
	}
	
	protected @Nullable EMagicElement getExistingSlottedElement() {
		for (int i = 1; i < 5; i++) { // start at 1, skipping slot 0 which is mastery orb slot
			ItemStack inSlot = localInv.getStackInSlot(i);
			if (inSlot.isEmpty()) {
				continue;
			}
			if (!(inSlot.getItem() instanceof InfusedGemItem)) {
				continue;
			}
			EMagicElement elem = InfusedGemItem.instance().getTypeFromMeta(inSlot.getMetadata());
			if (elem != null && elem != EMagicElement.PHYSICAL) {
				return elem;
			}
		}
		return null;
	}
	
	protected boolean canSetInSlot(EntityArcaneWolf wolf, int index, @Nonnull ItemStack stack) {
		// Index 0 is the 'center' item if there is one.
		// 1-5 are four 'outer' slots.
		if (stack.isEmpty()) {
			return true;
		}
		
		final SlotMode mode = getSlotMode(wolf);
		if (mode == SlotMode.NONE) {
			return false;
		}
		
		if (mode == SlotMode.SECONDARY && index == 0) {
			return false;
		}
		
		if (index == 0) {
			// Only mastery orbs can go there
			return stack.getItem() instanceof MasteryOrb;
		}
		
		// Other slots must be attuned gems
		if (!(stack.getItem() instanceof InfusedGemItem)) {
			return false;
		}
		
		EMagicElement element = InfusedGemItem.instance().getTypeFromMeta(stack.getMetadata());
		if (element == null || element == EMagicElement.PHYSICAL) {
			return false;
		}
		
		// If any are present, only ones that match that element are allowed
		EMagicElement existing = getExistingSlottedElement();
		if (existing != null && existing != element) {
			return false;
		}
		
		// If wolf is already trained in one element, make sure new element is okay
		if (!wolf.canTrainElement(element)) {
			return false;
		}
		
		return true;
	}
	
	protected int getSlotX(EntityArcaneWolf wolf, int index, int width) {
		final SlotMode mode = getSlotMode(wolf);
		final int error = -1;
		final int cellWidth = GUI_SLOT_ICON_WIDTH;
		if (mode == SlotMode.NONE) {
			return error;
		}
		
		if (mode == SlotMode.SECONDARY && index == 0) {
			return error;
		}
		
		final int center = (width-cellWidth)/2;
		if (mode == SlotMode.PRIMARY) {
			final int offset = 32;
			final int[] offsets = {
					0,
					0,
					0,
					-offset,
					offset
			};
			return index < offsets.length ? offsets[index] + center : error;
//			if (index == 0 || index == 1 || index == 2) {
//				return center;
//			} else if (index == 3) {
//				return center - offset;
//			} else {
//				return center + offset;
//			}
		} else {
			final int offsetSmall = 32;
			final int offsetLarge = 64;
			final int[] offsets = {
					0,
					-offsetSmall,
					offsetSmall,
					-offsetLarge,
					offsetLarge
			};
			return index < offsets.length ? offsets[index] + center : error;
//			if (index == 1) {
//				return center - offsetSmall;
//			} else if (index == 2) {
//				return center + offsetSmall;
//			} else if (index == 3) {
//				return center - offsetLarge;
//			} else {
//				return center + offsetLarge;
//			}
		}
	}
	
	protected int getSlotY(EntityArcaneWolf wolf, int index, int height) {
		final SlotMode mode = getSlotMode(wolf);
		final int error = -1;
		final int cellHeight = GUI_SLOT_ICON_HEIGHT;
		if (mode == SlotMode.NONE) {
			return error;
		}
		
		if (mode == SlotMode.SECONDARY && index == 0) {
			return error;
		}
		
		final int center = (height-cellHeight)/2;
		if (mode == SlotMode.PRIMARY) {
			final int offset = 32;
			final int[] offsets = {
					8,
					-offset,
					offset,
					0,
					0
			};
			return index < offsets.length ? offsets[index] + center : error;
			
//			if (index == 0) {
//				return center;
//			} else if (index == 1) {
//				return center - offset;
//			} else if (index == 2) {
//				return center + offset;
//			} else {
//				return center;
//			}
		} else {
			final int offset = 32;
			final int[] offsets = {
					0,
					offset,
					offset,
					0,
					0
			};
			return index < offsets.length ? offsets[index] + center : error;
			
//			if (index == 1 || index == 2) {
//				return center + offset;
//			} else {
//				return center;
//			}
		}
	}
	
	protected void onSlotChange(EntityArcaneWolf wolf, EntityPlayer player, PetContainer<EntityArcaneWolf> container) {
		
		// See if all slots are filled and if we should start training
		final SlotMode mode = getSlotMode(wolf);
		if (mode == SlotMode.NONE) {
			return;
		}
		
		// Both modes need all four secondary slots to be full with the same element
		EMagicElement elem = null;
		for (int i = 1; i < 5; i++) {
			ItemStack stackInSlot = localInv.getStackInSlot(i);
			if (stackInSlot.isEmpty() || !(stackInSlot.getItem() instanceof InfusedGemItem)) {
				return;
			}
			
			EMagicElement slotElem = InfusedGemItem.instance().getTypeFromMeta(stackInSlot.getMetadata());
			if (elem == null) {
				elem = slotElem;
			} else if (elem != slotElem) {
				return;
			}
		}
		
		if (!wolf.canTrainElement(elem)) {
			return;
		}
		
		// Primary slot mode needs mastery orb, too
		if (mode == SlotMode.PRIMARY) {
			ItemStack center = localInv.getStackInSlot(0);
			if (center.isEmpty() || !(center.getItem() instanceof MasteryOrb)) {
				return;
			}
		}
		
		// Start training!
		wolf.startTraining(elem);
		localInv.clear();
		//container.clearSlots();
	}
	
	@Override
	public void showSheet(EntityArcaneWolf pet, EntityPlayer player, PetContainer<EntityArcaneWolf> container, int width, int height, int offsetX, int offsetY) {
		final int cellWidth = 18;
		final int invRow = 9;
		final int invWidth = cellWidth * invRow;
		final int leftOffset = (width - invWidth) / 2;
		//final int invTopOffset = 10;
		final int playerInvSize = 27 + 9;
		final int playerTopOffset = 100;
		final int upperHeight = (height-playerTopOffset);
		
		for (int i = 0; i < localInv.getSizeInventory(); i++) {
			final int x = getSlotX(pet, i, width);
			final int y = getSlotY(pet, i, upperHeight);
			if (x < 0 || y < 0) {
				continue;
			}
			final int index = i;
			Slot slotIn = new Slot(localInv, i, offsetX + x, offsetY + y) {
				
				@Override
				public void onSlotChanged() {
					ArcaneWolfTrainingSheet.this.onSlotChange(pet, player, container);
				}
				
				@Override
				public boolean isItemValid(ItemStack stack) {
					return canSetInSlot(pet, index, stack);
				}
				
				@Override
				public int getItemStackLimit(ItemStack stack) {
					return 1;
				}
				
				@Override
				@SideOnly(Side.CLIENT)
				public boolean isEnabled() {
					return getSlotMode(pet) != SlotMode.NONE;
				}
			};
			container.addSheetSlot(slotIn);
		}
		
		IInventory playerInv = player.inventory;
		for (int i = 0; i < playerInvSize; i++) {
			Slot slotIn = new Slot(playerInv, (i + 9) % 36, leftOffset + offsetX + (cellWidth * (i % invRow)),
					(i < 27 ? 0 : 10) + playerTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
	}

	@Override
	public void hideSheet(EntityArcaneWolf pet, EntityPlayer player, PetContainer<EntityArcaneWolf> container) {
		container.dropContainerInventory(localInv);
		container.clearSlots();
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
		
		// Draw sheet
		GlStateManager.pushMatrix();
		{
			final int cellWidth = 18;
			final int bowlWidth = GUI_SLOT_ICON_WIDTH;
			final int bowlHeight = GUI_SLOT_ICON_HEIGHT;
			final int bowlHOffset = ((cellWidth-2)-bowlWidth)/2;
			final int bowlVOffset = 8;
			final int invRow = 9;
			final int invWidth = cellWidth * invRow;
			final int leftOffset = (width - invWidth) / 2;
			final int playerInvSize = 27 + 9;
			final int playerTopOffset = 100;
			final int upperHeight = (height-playerTopOffset);
			
			// Draw glyphs first
			if (pet.getTrainingElement() != null || pet.getElementalType().getPrimary() != null)
			{
				mc.getTextureManager().bindTexture(TEX_LOC);
				final EMagicElement primary = pet.getElementalType().getPrimary();
				
				// Inner glyph for primary
				{
					// Draw background
					GlStateManager.color(1f, 1f, 1f, 1f);
					Gui.drawModalRectWithCustomSizedTexture((width-GUI_CENTER_GEM_SOCKET_WIDTH)/2, (upperHeight-GUI_CENTER_GEM_SOCKET_HEIGHT)/2,
							GUI_CENTER_GEM_SOCKET_HOFFSET, GUI_CENTER_GEM_SOCKET_VOFFSET,
							GUI_CENTER_GEM_SOCKET_WIDTH, GUI_CENTER_GEM_SOCKET_HEIGHT,
							256, 256);
					
					// Then draw inner
					EMagicElement inner;
					final float prog;
					final int level;
					if (primary == null) {
						inner = pet.getTrainingElement();
						level = pet.getTrainingLevel();
						prog = (((float) pet.getTrainingXP() / (float) pet.getMaxTrainingXP()) + (level-1)) / 3f;
					} else {
						inner = primary;
						prog = 1f;
						level = 3;
					}
					
					final int color = inner.getColor();
					final float brightness = .8f * (float) Math.sqrt(prog);
					
					float red;
					float green;
					float blue;
					
					{
						int r = (color >> 16) & 0xFF;
						int g = (color >> 8) & 0xFF;
						int b = (color >> 0) & 0xFF;
						int dr = 0xFF - r;
						int dg = 0xFF - g;
						int db = 0xFF - b;
						
						red = 1f - ((float) dr / 255f) * brightness;
						green = 1f - ((float) dg / 255f) * brightness;
						blue = 1f - ((float) db / 255f) * brightness;
					}
					
					GlStateManager.color(red, green, blue, 1f);
					
					Gui.drawModalRectWithCustomSizedTexture((width-GUI_CENTER_GEM_WIDTH)/2, (upperHeight-GUI_CENTER_GEM_HEIGHT)/2,
							GUI_CENTER_GEM_HOFFSET + ((level-1) * GUI_CENTER_GEM_WIDTH), GUI_CENTER_GEM_VOFFSET,
							GUI_CENTER_GEM_WIDTH, GUI_CENTER_GEM_HEIGHT,
							256, 256);
					
					// Inner spark that's always 'bright'
					GlStateManager.color(
							(float)((color >> 16) & 0xFF) / 255f,
							(float)((color >> 8) & 0xFF) / 255f,
							(float)((color >> 0) & 0xFF) / 255f,
							1f
							);
					Gui.drawModalRectWithCustomSizedTexture((width-GUI_GEM_SPARK_WIDTH)/2, (upperHeight-GUI_GEM_SPARK_HEIGHT)/2,
							GUI_GEM_SPARK_HOFFSET, GUI_GEM_SPARK_VOFFSET,
							GUI_GEM_SPARK_WIDTH, GUI_GEM_SPARK_HEIGHT,
							256, 256);
				}
				
				// Outer
				final EMagicElement secondary = pet.getElementalType().getSecondary();
				if (primary != null && (secondary != null || pet.getTrainingElement() != null)) {
					final EMagicElement outer;
					final float prog;
					final int level;
					if (secondary == null) {
						outer = pet.getTrainingElement();
						level = pet.getTrainingLevel();
						prog = (((float) pet.getTrainingXP() / (float) pet.getMaxTrainingXP()) + (level-1)) / 3f;
					} else {
						outer = secondary;
						prog = 1f;
						level = 3;
					}
					
					final int color = outer.getColor();
					final float brightness = .8f * (float) Math.sqrt(prog);
					
					float red;
					float green;
					float blue;
					
					{
						int r = (color >> 16) & 0xFF;
						int g = (color >> 8) & 0xFF;
						int b = (color >> 0) & 0xFF;
						int dr = 0xFF - r;
						int dg = 0xFF - g;
						int db = 0xFF - b;
						
						red = 1f - ((float) dr / 255f) * brightness;
						green = 1f - ((float) dg / 255f) * brightness;
						blue = 1f - ((float) db / 255f) * brightness;
					}
					
					GlStateManager.color(red, green, blue, 1f);
					mc.getTextureManager().bindTexture(TEX_LOC);

					// Level 1
					{
						Gui.drawModalRectWithCustomSizedTexture((width-GUI_GEM_EXTRAS_1_WIDTH)/2,
								(upperHeight-GUI_GEM_EXTRAS_1_HEIGHT)/2,
								GUI_GEM_EXTRAS_1_HOFFSET, GUI_GEM_EXTRAS_1_VOFFSET,
								GUI_GEM_EXTRAS_1_WIDTH, GUI_GEM_EXTRAS_1_HEIGHT,
								256, 256);
					}
					
					if (level >= 2) {
						Gui.drawModalRectWithCustomSizedTexture((width-GUI_GEM_EXTRAS_2_WIDTH)/2,
								(upperHeight-GUI_GEM_EXTRAS_2_HEIGHT)/2,
								GUI_GEM_EXTRAS_2_HOFFSET, GUI_GEM_EXTRAS_2_VOFFSET,
								GUI_GEM_EXTRAS_2_WIDTH, GUI_GEM_EXTRAS_2_HEIGHT,
								256, 256);
					}
					
					if (level >= 3) {
						Gui.drawModalRectWithCustomSizedTexture((width-GUI_GEM_EXTRAS_3_WIDTH)/2,
								(upperHeight-GUI_GEM_EXTRAS_3_HEIGHT)/2 + 35,
								GUI_GEM_EXTRAS_3_HOFFSET, GUI_GEM_EXTRAS_3_VOFFSET,
								GUI_GEM_EXTRAS_3_WIDTH, GUI_GEM_EXTRAS_3_HEIGHT,
								256, 256);
					}
				}
			}
			
			// Then draw inventories
			GlStateManager.color(1f, 1f, 1f, 1f);
			for (int i = 0; i < localInv.getSizeInventory(); i++) {
				final int x = getSlotX(pet, i, width);
				final int y = getSlotY(pet, i, upperHeight);
				if (x < 0 || y < 0) {
					continue;
				}
				
				if (i == 0) {
					// Draw center slot
					mc.getTextureManager().bindTexture(PetGUI.PetGUIContainer.TEXT);
					Gui.drawModalRectWithCustomSizedTexture(x - 1, y - 1,
							PetGUI.GUI_TEX_CELL_HOFFSET, PetGUI.GUI_TEX_CELL_VOFFSET,
							cellWidth, cellWidth,
							256, 256);
				} else {
					// Draw bowl
					mc.getTextureManager().bindTexture(TEX_LOC);
					Gui.drawModalRectWithCustomSizedTexture(x + bowlHOffset, y + bowlVOffset,
							GUI_SLOT_ICON_HOFFSET, GUI_SLOT_ICON_VOFFSET,
							bowlWidth, bowlHeight,
							256, 256);
				}
			}
			

			mc.getTextureManager().bindTexture(PetGUI.PetGUIContainer.TEXT);
			for (int i = 0; i < playerInvSize; i++) {
				GlStateManager.color(1f, 1f, 1f, 1f);
				Gui.drawModalRectWithCustomSizedTexture(leftOffset - 1 + (cellWidth * (i % invRow)), (i < 27 ? 0 : 10) + playerTopOffset - 1 + (cellWidth * (i / invRow)),
						PetGUI.GUI_TEX_CELL_HOFFSET, PetGUI.GUI_TEX_CELL_VOFFSET,
						cellWidth, cellWidth,
						256, 256);
			}
			mc.getTextureManager().bindTexture(TEX_LOC);
			
			GlStateManager.popMatrix();
		}
	}

	@Override
	public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		
	}

	@Override
	public void handleMessage(NBTTagCompound data) {
		
	}

	@Override
	public String getButtonText() {
		return "Training";
	}

	@Override
	public boolean shouldShow(EntityArcaneWolf wolf, PetContainer<EntityArcaneWolf> container) {
		return wolf.hasWolfCapability(WolfBondCapability.TRAINABLE);
	}

	@Override
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
