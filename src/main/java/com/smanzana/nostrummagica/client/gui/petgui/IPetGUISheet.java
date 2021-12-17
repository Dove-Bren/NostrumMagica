package com.smanzana.nostrummagica.client.gui.petgui;

import com.smanzana.nostrummagica.entity.IEntityPet;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A sheet on the pet GUI.
 * That is, one of the tabbed pages when interacting with a tamed pet.
 * @author Skyler
 *
 */
public interface IPetGUISheet<T extends IEntityPet> {
	
	// Called when the sheet is first shown. Set up anything that's needed.
	public void showSheet(T pet, EntityPlayer player, PetGUI.PetContainer<T> container, int width, int height, int offsetX, int offsetY);
	
	// Called when the sheet will no longer be shown.
	public void hideSheet(T pet, EntityPlayer player, PetGUI.PetContainer<T> container);
	
	// Draw the sheet
	@SideOnly(Side.CLIENT)
	public void draw(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY);
	
	@SideOnly(Side.CLIENT)
	public void overlay(Minecraft mc, float partialTicks, int width, int height, int mouseX, int mouseY);
	
	// Handle a mouse click.
	// mouseX and mouseY are relative to the sheet, not global.
	@SideOnly(Side.CLIENT)
	public void mouseClicked(int mouseX, int mouseY, int mouseButton);
	
	// A client sheet has sent a control message to its server counterpart. Update!
	public void handleMessage(NBTTagCompound data);
	
	// Return the (translated!) label for the button
	@SideOnly(Side.CLIENT)
	public String getButtonText();
	
	public boolean shouldShow(T pet, PetGUI.PetContainer<T> container);
	
}
