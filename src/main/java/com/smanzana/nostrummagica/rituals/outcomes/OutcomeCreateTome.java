package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.blocks.AltarBlock.AltarTileEntity;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeCreateTome implements IRitualOutcome {

	public OutcomeCreateTome() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Take plates and pages from the altars.
		ItemStack tome = SpellTome.createTome(centerItem, otherItems);
		if (tome == null)
			return;
		
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
		if (recipe.getTier() == 0) {
			// give to player
			
			player.inventory.addItemStackToInventory(tome);
		} else {
			altar.setItem(tome);
		}
	}

	@Override
	public List<String> getDescription() {
		return Lists.newArrayList(I18n.format("ritual.outcome.create_tome.desc",
				(Object[]) null)
				.split("\\|"));
	}
}
