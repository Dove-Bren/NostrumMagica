package com.smanzana.nostrummagica.entity;

import com.smanzana.nostrummagica.pet.PetInfo;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;

public interface IEntityPet extends IEntityTameable {

	public PetInfo getPetSummary();
	
	default public void onAttackCommand(EntityLivingBase target) { if (this instanceof EntityLiving) ((EntityLiving) this).setAttackTarget(target); };
	
	default public void onStopCommand() { if (this instanceof EntityLiving) ((EntityLiving) this).setAttackTarget(null); };
	
}
