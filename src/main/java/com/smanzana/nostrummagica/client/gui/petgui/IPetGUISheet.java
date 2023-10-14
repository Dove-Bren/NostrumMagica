package com.smanzana.nostrummagica.client.gui.petgui;

import com.smanzana.nostrummagica.entity.IEntityPet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A sheet on the pet GUI.
 * That is, one of the tabbed pages when interacting with a tamed pet.
 * @author Skyler
 *
 */
public interface IPetGUISheet<T extends IEntityPet> {
	
	// Called when the sheet is first shown. Set up anything that's needed.
	public void showSheet(T pet, PlayerEntity player, PetGUI.PetContainer<T> container, int width, int height, int offsetX, int offsetY);
	
	// Called when the sheet will no longer be shown.
	public void hideSheet(T pet, PlayerEntity player, PetGUI.PetContainer<T> container);
	
	// Draw the sheet
	@OnlyIn(Dist.CLIENT)
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY);
	
	@OnlyIn(Dist.CLIENT)
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY);
	
	// Handle a mouse click.
	// mouseX and mouseY are relative to the sheet, not global.
	@OnlyIn(Dist.CLIENT)
	public boolean mouseClicked(double mouseX, double mouseY, int mouseButton);
	
	// A client sheet has sent a control message to its server counterpart. Update!
	public void handleMessage(CompoundNBT data);
	
	// Return the (translated!) label for the button
	@OnlyIn(Dist.CLIENT)
	public String getButtonText();
	
	public boolean shouldShow(T pet, PetGUI.PetContainer<T> container);
	
}
