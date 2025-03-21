package com.smanzana.nostrummagica.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class ManaRecoveryEnchantment extends Enchantment {

	public static final String ID = "nostrum_enchantment_mana_recovery";
	private static ManaRecoveryEnchantment instance = null;
	
	public static ManaRecoveryEnchantment instance() {
		if (instance == null)
			instance = new ManaRecoveryEnchantment();
		
		return instance;
	}
	
	protected ManaRecoveryEnchantment() {
		super(Rarity.RARE, EnchantmentCategory.ARMOR, 
				new EquipmentSlot[] {
						EquipmentSlot.FEET,
						EquipmentSlot.LEGS,
						EquipmentSlot.CHEST,
						EquipmentSlot.HEAD});
		this.setRegistryName(ID);
	}

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    public int getMinCost(int enchantmentLevel)
    {
        return 10 + ((enchantmentLevel - 1) * 15); // 10, 25, 40
    }

    /**
     * Returns the maximum value of enchantability nedded on the enchantment level passed.
     */
    public int getMaxCost(int enchantmentLevel)
    {
        return this.getMinCost(enchantmentLevel) + 15;
    }

    public boolean isTreasureOnly()
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
