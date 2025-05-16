package com.smanzana.nostrummagica.enchantment;

import com.smanzana.nostrummagica.crafting.NostrumTags;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Wearable;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SpellChannelingEnchantment extends Enchantment {

	public static final String ID = "spell_channeling";
	private static SpellChannelingEnchantment instance = null;
	
	public static SpellChannelingEnchantment instance() {
		if (instance == null)
			instance = new SpellChannelingEnchantment();
		
		return instance;
	}
	
	protected SpellChannelingEnchantment() {
		super(Rarity.RARE, EnchantmentCategory.BREAKABLE, 
				new EquipmentSlot[] {
						EquipmentSlot.MAINHAND,
						EquipmentSlot.OFFHAND,
						});
		this.setRegistryName(ID);
	}
	
	@Override
	public boolean canEnchant(ItemStack stack) {
		return !stack.isEmpty()
				&& !(stack.getItem() instanceof Wearable)
				&& (!stack.isStackable() || stack.getCount() == 1)
				&& !stack.is(NostrumTags.Items.SpellChanneling) // forbid on things that already are to avoid costing enchanting points
				;
	}

    /**
     * Returns the minimal value of enchantability needed on the enchantment level passed.
     */
    @Override
    public int getMinCost(int enchantmentLevel)
    {
        return 15;
    }

    /**
     * Returns the maximum value of enchantability nedded on the enchantment level passed.
     */
    @Override
    public int getMaxCost(int enchantmentLevel)
    {
        return this.getMinCost(enchantmentLevel) + 50;
    }

    @Override
    public boolean isTreasureOnly()
    {
        return false;
    }
    
    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack) {
    	return canEnchant(stack) && super.canApplyAtEnchantingTable(stack); // still check super to only do enchant table rolls
    	// on damageable things instead of letting you try to enchant like a clock on the enchanting table.
    }

    /**
     * Returns the maximum level that the enchantment can have.
     */
    @Override
    public int getMaxLevel()
    {
        return 1;
    }
	
}
