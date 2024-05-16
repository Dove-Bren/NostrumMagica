package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.entity.IMagicEntity;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.client.petgui.PetGUIStatAdapter;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.entity.IRerollablePet;
import com.smanzana.petcommand.api.pet.PetInfo.PetAction;

import net.minecraft.entity.player.PlayerEntity;

public interface ITameDragon extends IEntityPet, IRerollablePet, IMagicEntity {

	@Override
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(PlayerEntity player);
	
	@Override
	public PetGUIStatAdapter<? extends ITameDragon> getGUIAdapter();
	
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
