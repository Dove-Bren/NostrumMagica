package com.smanzana.nostrummagica.entity;

/**
 * An entity with its own store of mana
 * @author Skyler
 *
 */
public interface IMagicEntity {

	/**
	 * Gets the current amount of mana an entity has
	 * @return
	 */
	public int getMana();
	
	/**
	 * Tries to add the provided amount of mana to the entity.
	 * Returns any that could not be stored (too much mana).
	 * @param amount
	 * @return
	 */
	public int addMana(int amount);
	
	/**
	 * Attempts to take mana from the entity.
	 * If the entity doesn't have as much mana as is requested, this should
	 * return false and not deduct any mana.
	 * @param amount
	 * @return
	 */
	public boolean takeMana(int amount);
	
	/**
	 * Get this entity's maximum mana
	 * @return
	 */
	public int getMaxMana();
	
}
