package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.IRerollablePet;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;

import net.minecraft.entity.player.EntityPlayer;

public interface ITameDragon extends IEntityPet, IRerollablePet {

	@Override
	public PetGUI.PetContainer<? extends ITameDragon> getGUIContainer(EntityPlayer player);
	
	@Override
	public PetGUI.PetGUIStatAdapter<? extends ITameDragon> getGUIAdapter();
	
	//public UUID getUniqueID();
	
	// Reroll dragon stats!
	public void rerollStats();
	
	//public float getHealth();
	
	//public float getMaxHealth();
	
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
	
	public boolean isSoulBound();
}
