package com.smanzana.nostrummagica.client.gui.dragongui;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon.DragonEquipmentInventory;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.dragon.ITameDragon;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class RedDragonInventorySheet implements IDragonGUISheet {
	
	private EntityTameDragonRed dragon;
	private IInventory dragonInv;
	private DragonEquipmentInventory dragonEquips;
	private IInventory playerInv;
	
	public RedDragonInventorySheet(EntityTameDragonRed dragon) {
		this.dragon = dragon;
	}
	
	@Override
	public void showSheet(ITameDragon dragon, EntityPlayer player, DragonContainer container, int width, int height, int offsetX, int offsetY) {
		final int cellWidth = 18;
		final int invRow = 9;
		final int invWidth = cellWidth * invRow;
		final int leftOffset = (width - invWidth) / 2;
		final int dragonTopOffset = 10;
		final int playerInvSize = 27 + 9;
		
		dragonInv = this.dragon.getInventory();
		for (int i = 0; i < dragonInv.getSizeInventory(); i++) {
			Slot slotIn = new Slot(dragonInv, i, leftOffset + offsetX + (cellWidth * (i % invRow)), dragonTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
		
		dragonEquips = this.dragon.getDragonEquipmentInventory();
		for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
			// NOT IMPLEMENTED TODO
			{
				if (slot == DragonEquipmentSlot.CREST || slot == DragonEquipmentSlot.WINGS) {
					continue;
				}
			}
			// NOT IMPLEMENTED TODO
			final int i = slot.ordinal();
			Slot slotIn = new Slot(dragonEquips, i, leftOffset + offsetX - (cellWidth + 4), dragonTopOffset + offsetY + (cellWidth * i * 2)) {
				@Override
				public boolean isItemValid(@Nonnull ItemStack stack) {
					return dragonEquips.isItemValidForSlot(this.getSlotIndex(), stack);
				}
			};
			container.addSheetSlot(slotIn);
		}

		int otherSlots = dragonInv.getSizeInventory();
		final int playerTopOffset = dragonTopOffset + (cellWidth * (otherSlots / 9)) + 20;
		playerInv = player.inventory;
		for (int i = 0; i < playerInvSize; i++) {
			Slot slotIn = new Slot(playerInv, (i + 9) % 36, leftOffset + offsetX + (cellWidth * (i % invRow)),
					(i < 27 ? 0 : 10) + playerTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
	}

	@Override
	public void hideSheet(ITameDragon dragon, EntityPlayer player, DragonContainer container) {
		container.clearSlots();
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
		
		mc.getTextureManager().bindTexture(TamedDragonGUI.DragonGUI.TEXT);
		
		// Draw sheet
		GlStateManager.pushMatrix();
		{
			final int cellWidth = 18;
			final int invRow = 9;
			final int invWidth = cellWidth * invRow;
			final int leftOffset = (width - invWidth) / 2;
			final int dragonTopOffset = 10;
			final int playerInvSize = 27 + 9;
			
			for (int i = 0; i < dragonInv.getSizeInventory(); i++) {
				GlStateManager.color(1f, 1f, 1f, 1f);
				Gui.drawModalRectWithCustomSizedTexture(leftOffset - 1 + (cellWidth * (i % invRow)), dragonTopOffset - 1 + (cellWidth * (i / invRow)),
						TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
						cellWidth, cellWidth,
						256, 256);
			}
			
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				// NOT IMPLEMENTED TODO
				{
					if (slot == DragonEquipmentSlot.CREST || slot == DragonEquipmentSlot.WINGS) {
						continue;
					}
				}
				// NOT IMPLEMENTED TODO
				
				final int i = slot.ordinal();
				GlStateManager.color(1f, 1f, 1f, 1f);
				Gui.drawModalRectWithCustomSizedTexture(leftOffset - 1 - (cellWidth + 4), dragonTopOffset - 1 + (cellWidth * (i * 2)),
						TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
						cellWidth, cellWidth,
						256, 256);
			}
			
			int otherSlots = dragonInv.getSizeInventory();
			final int playerTopOffset = dragonTopOffset + (cellWidth * (otherSlots / 9)) + 20;
			for (int i = 0; i < playerInvSize; i++) {
				GlStateManager.color(1f, 1f, 1f, 1f);
				Gui.drawModalRectWithCustomSizedTexture(leftOffset - 1 + (cellWidth * (i % invRow)), (i < 27 ? 0 : 10) + playerTopOffset - 1 + (cellWidth * (i / invRow)),
						TamedDragonGUI.GUI_TEX_CELL_HOFFSET, TamedDragonGUI.GUI_TEX_CELL_VOFFSET,
						cellWidth, cellWidth,
						256, 256);
			}
			
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
		return "Backpack";
	}

	@Override
	public boolean shouldShow(ITameDragon dragon, DragonContainer container) {
		return this.dragon.canUseInventory();
	}

	@Override
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
