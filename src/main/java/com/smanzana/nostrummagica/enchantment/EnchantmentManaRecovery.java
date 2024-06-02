package com.smanzana.nostrummagica.enchantment;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentType;
import net.minecraft.inventory.EquipmentSlotType;

public class EnchantmentManaRecovery extends Enchantment {

	public static final String ID = "nostrum_enchantment_mana_recovery";
	private static EnchantmentManaRecovery instance = null;
	
	public static EnchantmentManaRecovery instance() {
		if (instance == null)
			instance = new EnchantmentManaRecovery();
		
		return instance;
	}
	
	protected EnchantmentManaRecovery() {
		super(Rarity.RARE, EnchantmentType.ARMOR, 
				new EquipmentSlotType[] {
						EquipmentSlotType.FEET,
						EquipmentSlotType.LEGS,
						EquipmentSlotType.CHEST,
						EquipmentSlotType.HEAD});
		this.setRegistryName(ID);
	}

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    public int getMinEnchantability(int enchantmentLevel)
    {
        return 10 + ((enchantmentLevel - 1) * 15); // 10, 25, 40
    }

    /**
     * Returns the maximum value of enchantability nedded on the enchantment level passed.
     */
    public int getMaxEnchantability(int enchantmentLevel)
    {
        return this.getMinEnchantability(enchantmentLevel) + 15;
    }

    public boolean isTreasureEnchantment()
    {
        return false;
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    public int getMaxLevel()
    {
        return 3;
    }
	
}
