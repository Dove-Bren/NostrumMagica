package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;
import com.smanzana.nostrummagica.pet.PetInfo;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;

public interface IEntityPet extends IEntityTameable {

	public PetInfo getPetSummary();
	
	default public void onAttackCommand(EntityLivingBase target) { if (this instanceof EntityLiving) ((EntityLiving) this).setAttackTarget(target); };
	
	default public void onStopCommand() { if (this instanceof EntityLiving) ((EntityLiving) this).setAttackTarget(null); };
	
	public PetGUI.PetContainer<? extends IEntityPet> getGUIContainer(EntityPlayer player);
	
	public PetGUI.PetGUIStatAdapter<? extends IEntityPet> getGUIAdapter();
	
}
