package com.smanzana.nostrummagica.client.gui.dragongui;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI.DragonContainer;
import com.smanzana.nostrummagica.entity.EntityTameDragonRed;
import com.smanzana.nostrummagica.entity.ITameDragon;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;

public class RedDragonInventorySheet implements IDragonGUISheet {
	
	private static final ResourceLocation TEXT = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/container/tamed_dragon_gui.png");
	
	private EntityTameDragonRed dragon;
	private IInventory dragonInv;
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
		
		FontRenderer fonter = mc.fontRendererObj;
		GlStateManager.color(1.0F,  1.0F, 1.0F, 1.0F);
		
		mc.getTextureManager().bindTexture(TEXT);
		
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

}
