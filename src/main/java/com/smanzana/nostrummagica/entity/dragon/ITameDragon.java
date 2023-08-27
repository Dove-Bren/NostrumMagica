package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.entity.IEntityPet;
import com.smanzana.nostrummagica.entity.IMagicEntity;
import com.smanzana.nostrummagica.entity.IRerollablePet;
import com.smanzana.nostrummagica.pet.PetInfo.PetAction;

import net.minecraft.entity.player.PlayerEntity;

public interface ITameDragon extends IEntityPet, IRerollablePet, IMagicEntity {

	@Override
	public PetGUI.PetContainer<? extends ITameDragon> getGUIContainer(PlayerEntity player);
	
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
	
	public float getBond();
	
	public boolean sharesMana(PlayerEntity player);
	
	public PetAction getPetAction();
	
	public boolean isSoulBound();
}
