package com.smanzana.nostrummagica.capabilities;

public interface INostrumMana {

	/**
	 * Return the amount of mana/max mana this provider has
	 * @return
	 */
	public int getMana();
	public int getMaxMana();
	
	/**
	 * Attempt to add or take (negative values) mana from this provider. 
	 * @param amount Amount to try to take
	 */
	public void addMana(int amount);
	
}
