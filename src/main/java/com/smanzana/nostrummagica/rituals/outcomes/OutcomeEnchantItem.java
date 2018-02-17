package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeEnchantItem implements IRitualOutcome {

	private Enchantment enchantment;
	private int level;
	
	public OutcomeEnchantItem(Enchantment enchantment, int level) {
		this.enchantment = enchantment;
		this.level = level;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, BlockPos center, RitualRecipe recipe) {
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		if (recipe.getTier() == 0) {
			// enchant item on player
			ItemStack item = player.getHeldItemMainhand();
			if (item == null)
				return;
			
			if (!enchantment.canApply(item))
				return;
			
			item.addEnchantment(enchantment, level);
		} else {
			AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
			
			ItemStack item = altar.getItem();
			if (item == null)
				return;
			
			if (!enchantment.canApply(item))
				return;
			
			item.addEnchantment(enchantment, level);
			altar.setItem(item);
		}
	}

	
	
}
