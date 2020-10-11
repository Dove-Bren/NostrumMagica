package com.smanzana.nostrummagica.entity.dragon;

import java.util.UUID;

import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.PetInfo;
import com.smanzana.nostrummagica.entity.PetInfo.PetAction;
import com.smanzana.nostrummagica.entity.PetInfo.SecondaryFlavor;

import net.minecraft.entity.player.EntityPlayer;

public interface ITameDragon extends IEntityPet {

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

	public void addMana(int mana);
	
	public boolean sharesMana(EntityPlayer player);
	
	public PetAction getPetAction();
	
	public default PetInfo getPetSummary() {
		return PetInfo.claim(getHealth(), getMaxHealth(), getXP(), getMaxXP(), SecondaryFlavor.PROGRESS, getPetAction());
	}
	
}
