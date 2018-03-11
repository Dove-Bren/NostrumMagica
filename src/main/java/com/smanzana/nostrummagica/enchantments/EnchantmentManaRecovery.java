package com.smanzana.nostrummagica.enchantments;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnumEnchantmentType;
import net.minecraft.inventory.EntityEquipmentSlot;

public class EnchantmentManaRecovery extends Enchantment {

	public static final String ID = "nostrum_enchantment_mana_recovery";
	private static EnchantmentManaRecovery instance = null;
	
	public static EnchantmentManaRecovery instance() {
		if (instance == null)
			instance = new EnchantmentManaRecovery();
		
		return instance;
	}
	
	protected EnchantmentManaRecovery() {
		super(Rarity.RARE, EnumEnchantmentType.ARMOR, 
				new EntityEquipmentSlot[] {
						EntityEquipmentSlot.FEET,
						EntityEquipmentSlot.LEGS,
						EntityEquipmentSlot.CHEST,
						EntityEquipmentSlot.HEAD});
		this.setName("mana_recovery");
	}

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    public int getMinEnchantability(int enchantmentLevel)
    {
        return enchantmentLevel * 15;
    }

    /**
     * Returns the maximum value of enchantability nedded on the enchantment level passed.
     */
    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 50;
    }

    public boolean isTreasureEnchantment()
    {
        return true;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    public int getMaxLevel()
    {
        return 3;
    }
	
}
