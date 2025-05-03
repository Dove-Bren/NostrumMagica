package com.smanzana.nostrummagica.entity.dragon;

import com.smanzana.nostrummagica.capabilities.INostrumMana;
import com.smanzana.petcommand.api.client.petgui.IPetGUISheet;
import com.smanzana.petcommand.api.entity.IEntityPet;
import com.smanzana.petcommand.api.entity.IRerollablePet;
import com.smanzana.petcommand.api.pet.EPetAction;

import net.minecraft.world.entity.player.Player;

public interface ITameDragon extends IEntityPet, IRerollablePet, INostrumMana {

	@Override
	public IPetGUISheet<? extends IEntityPet>[] getContainerSheets(Player player);
	
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
	
	public boolean sharesMana(Player player);
	
	public EPetAction getPetAction();
	
	public boolean isSoulBound();
}
