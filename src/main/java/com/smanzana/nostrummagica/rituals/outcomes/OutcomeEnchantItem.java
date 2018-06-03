package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.items.SpellPlate;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
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
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
		if (recipe.getTier() == 0 || centerItem == null) {
			// enchant item on player
			ItemStack item = player.getHeldItemMainhand();
			if (item == null)
				return;
			
			if (!enchantment.canApply(item)) {
				return;
			}
			
			item.addEnchantment(enchantment, level);
		} else {
			if (!enchantment.canApply(centerItem)) {
				altar.setItem(centerItem);
				return;
			}
			
			centerItem.addEnchantment(enchantment, level);
			altar.setItem(centerItem);
		}
	}

	@Override
	public List<String> getDescription() {
		String name = I18n.format(enchantment.getName(), (Object[]) null);
		String level = SpellPlate.toRoman(this.level);
		return Lists.newArrayList(I18n.format("ritual.outcome.enchant.desc",
				new Object[] {name, level})
				.split("\\|"));
	}
}
