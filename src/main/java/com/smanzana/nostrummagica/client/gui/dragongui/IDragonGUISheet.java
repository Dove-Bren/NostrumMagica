package com.smanzana.nostrummagica.client.gui.dragongui;

import com.smanzana.nostrummagica.entity.dragon.ITameDragon;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

/**
 * A sheet on the dragon GUI.
 * That is, one of the tabbed pages when interacting with a tamed dragon.
 * @author Skyler
 *
 */
public interface IDragonGUISheet {
	
	// Called when the sheet is first shown. Set up anything that's needed.
	public void showSheet(ITameDragon dragon, EntityPlayer player, TamedDragonGUI.DragonContainer container, int width, int height, int offsetX, int offsetY);
	
	// Called when the sheet will no longer be shown.
	public void hideSheet(ITameDragon dragon, EntityPlayer player, TamedDragonGUI.DragonContainer container);
	
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
	
	public boolean shouldShow(ITameDragon dragon, TamedDragonGUI.DragonContainer container);
	
}
