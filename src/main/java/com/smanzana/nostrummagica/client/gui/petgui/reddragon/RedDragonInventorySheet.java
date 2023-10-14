package com.smanzana.nostrummagica.client.gui.petgui.reddragon;

import javax.annotation.Nonnull;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.client.gui.petgui.PetGUI.PetContainer;
import com.smanzana.nostrummagica.client.gui.petgui.PetInventorySheet;
import com.smanzana.nostrummagica.entity.dragon.EntityDragon.DragonEquipmentInventory;
import com.smanzana.nostrummagica.entity.dragon.EntityTameDragonRed;
import com.smanzana.nostrummagica.items.DragonArmor.DragonEquipmentSlot;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;

public class RedDragonInventorySheet extends PetInventorySheet<EntityTameDragonRed> {
	
	public RedDragonInventorySheet(EntityTameDragonRed dragon) {
		super(dragon, dragon.getInventory());
	}
	
	@Override
	public void showSheet(EntityTameDragonRed dragon, PlayerEntity player, PetContainer<EntityTameDragonRed> container, int width, int height, int offsetX, int offsetY) {
		super.showSheet(dragon, player, container, width, height, offsetX, offsetY);
		
		final int cellWidth = 18;
		final int invRow = 9;
		final int invWidth = cellWidth * invRow;
		final int leftOffset = (width - invWidth) / 2;
		final int dragonTopOffset = 10;
		
		DragonEquipmentInventory dragonEquips = this.pet.getDragonEquipmentInventory();
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
	}

	@Override
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY) {
		super.draw(mc, partialTicks, width, height, mouseX, mouseY);
		
		GlStateManager.color4f(1.0F,  1.0F, 1.0F, 1.0F);
		
		mc.getTextureManager().bindTexture(PetGUI.PetGUIContainer.TEXT);
		
		// Draw sheet
		GlStateManager.pushMatrix();
		{
			final int cellWidth = 18;
			final int invRow = 9;
			final int invWidth = cellWidth * invRow;
			final int leftOffset = (width - invWidth) / 2;
			final int dragonTopOffset = 10;
			
			for (DragonEquipmentSlot slot : DragonEquipmentSlot.values()) {
				// NOT IMPLEMENTED TODO
				{
					if (slot == DragonEquipmentSlot.CREST || slot == DragonEquipmentSlot.WINGS) {
						continue;
					}
				}
				// NOT IMPLEMENTED TODO
				
				final int i = slot.ordinal();
				GlStateManager.color4f(1f, 1f, 1f, 1f);
				RenderFuncs.drawModalRectWithCustomSizedTexture(leftOffset - 1 - (cellWidth + 4), dragonTopOffset - 1 + (cellWidth * (i * 2)),
						PetGUI.GUI_TEX_CELL_HOFFSET, PetGUI.GUI_TEX_CELL_VOFFSET,
						cellWidth, cellWidth,
						256, 256);
			}
			
			GlStateManager.popMatrix();
		}
	}

	@Override
	public boolean shouldShow(EntityTameDragonRed dragon, PetContainer<EntityTameDragonRed> container) {
		return this.pet.canUseInventory();
	}

}
