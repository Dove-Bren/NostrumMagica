package com.smanzana.nostrummagica.capabilities;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.DamageSource;

public interface IManaArmor {

	// Armor present or not
	public boolean hasArmor();
	public void setHasArmor(boolean hasArmor, int manaCost);
	
	// Get info about the cost of the armor
	public int getManaCost();
	
	// Runtime armor functions
	/**
	 * Check whether this armor can handle the provided attack
	 * @param hurtEntity
	 * @param source
	 * @param amount
	 * @return
	 */
	public boolean canHandle(Entity hurtEntity, DamageSource source, float amount);
	
	/**
	 * Handle the provided attack, returning the new amount of damage it should do.
	 * Amounts <= 0f will treat the attack as completely absorbed.
	 * This function should do any deductions from the armor as necessary.
	 * @param hurtEntity
	 * @param source
	 * @param originalAmount
	 * @return
	 */
	public float handle(Entity hurtEntity, DamageSource source, float originalAmount);
	
	// Serialization/Deserialization. Do not call.
	public void deserialize(
			boolean hasAmor,
			int manaCost
			);
	
	// Copy fields out of an existing armor capability
	public void copy(IManaArmor cap);
	public void provideEntity(LivingEntity entity);
}
