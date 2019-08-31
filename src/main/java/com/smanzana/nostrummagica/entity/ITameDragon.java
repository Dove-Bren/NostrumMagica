package com.smanzana.nostrummagica.entity;

import java.util.UUID;

import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI;

import net.minecraft.entity.player.EntityPlayer;

public interface ITameDragon extends IEntityTameable {

	public TamedDragonGUI.DragonContainer getGUIContainer(EntityPlayer player);
	
	public UUID getUniqueID();
	
	// Reroll dragon stats!
	public void rollStats();
	
	public float getHealth();
	
	public float getMaxHealth();
	
	// Get the current amount of xp the dragon has.
	// Return -1 to indicate this dragon cannot level up.
	public int getXP();
	
	public int getMaxXP();
	
	public int getMana();
	
	public int getMaxMana();
	
	public float getBond();
	
}
