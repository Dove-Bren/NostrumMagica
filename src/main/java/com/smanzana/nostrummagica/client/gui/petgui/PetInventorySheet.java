package com.smanzana.nostrummagica.client.gui.petgui;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.nbt.CompoundNBT;

public abstract class PetInventorySheet<T extends IEntityPet> implements IPetGUISheet<T> {
	
	protected final T pet;
	protected final IInventory petInv;
	
	public PetInventorySheet(T pet, IInventory petInventory) {
		this.pet = pet;
		this.petInv = petInventory;
	}
	
	@Override
	public void showSheet(T pet, PlayerEntity player, PetContainer<T> container, int width, int height, int offsetX, int offsetY) {
		final int cellWidth = 18;
		final int invRow = 9;
		final int invWidth = cellWidth * invRow;
		final int leftOffset = (width - invWidth) / 2;
		final int dragonTopOffset = 10;
		final int playerInvSize = 27 + 9;
		
		for (int i = 0; i < petInv.getSizeInventory(); i++) {
			Slot slotIn = new Slot(petInv, i, leftOffset + offsetX + (cellWidth * (i % invRow)), dragonTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
		
		final int playerTopOffset = 100;
		IInventory playerInv = player.inventory;
		for (int i = 0; i < playerInvSize; i++) {
			Slot slotIn = new Slot(playerInv, (i + 9) % 36, leftOffset + offsetX + (cellWidth * (i % invRow)),
					(i < 27 ? 0 : 10) + playerTopOffset + offsetY + (cellWidth * (i / invRow)));
			container.addSheetSlot(slotIn);
		}
	}

	@Override
	public void hideSheet(T pet, PlayerEntity player, PetContainer<T> container) {
		container.clearSlots();
	}

	@Override
	public void draw(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		mc.getTextureManager().bindTexture(PetGUI.PetGUIContainer.TEXT);
		
		// Draw sheet
		matrixStackIn.push();
		{
			final int cellWidth = 18;
			final int invRow = 9;
			final int invWidth = cellWidth * invRow;
			final int leftOffset = (width - invWidth) / 2;
			final int dragonTopOffset = 10;
			final int playerInvSize = 27 + 9;
			
			for (int i = 0; i < petInv.getSizeInventory(); i++) {
				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, leftOffset - 1 + (cellWidth * (i % invRow)),
						dragonTopOffset - 1 + (cellWidth * (i / invRow)), PetGUI.GUI_TEX_CELL_HOFFSET,
						PetGUI.GUI_TEX_CELL_VOFFSET, cellWidth,
						cellWidth, 256, 256,
						1f, 1f, 1f, 1f);
			}
			
			final int playerTopOffset = 100;
			for (int i = 0; i < playerInvSize; i++) {
				RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, leftOffset - 1 + (cellWidth * (i % invRow)),
						(i < 27 ? 0 : 10) + playerTopOffset - 1 + (cellWidth * (i / invRow)), PetGUI.GUI_TEX_CELL_HOFFSET,
						PetGUI.GUI_TEX_CELL_VOFFSET, cellWidth,
						cellWidth, 256, 256,
						1f, 1f, 1f, 1f);
			}
			
		}
		matrixStackIn.pop();
	}

	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton) {
		return false;
	}

	@Override
	public void handleMessage(CompoundNBT data) {
		
	}

	@Override
	public String getButtonText() {
		return "Backpack";
	}

	@Override
	public abstract boolean shouldShow(T dragon, PetContainer<T> container);

	@Override
	public void overlay(MatrixStack matrixStackIn, Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		
	}

}
