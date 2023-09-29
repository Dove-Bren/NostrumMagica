package com.smanzana.nostrummagica.rituals.outcomes;

import java.util.List;

import com.smanzana.nostrummagica.rituals.RitualRecipe;
import com.smanzana.nostrummagica.tiles.AltarTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeModifyCenterItemGeneric implements IRitualOutcome {
	
	public static interface ItemModification {
		public void modify(World world, PlayerEntity player, ItemStack item, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe);
	}

	private ItemModification modification;
	private List<String> description;
	
	public OutcomeModifyCenterItemGeneric(ItemModification modification, List<String> description) {
		this.modification = modification;
		this.description = description;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, ItemStack centerItem, NonNullList<ItemStack> otherItems, BlockPos center, RitualRecipe recipe) {
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getTileEntity(center);
		if (recipe.getTier() == 0 || centerItem.isEmpty()) {
			// enchant item on player
			ItemStack item = player.getHeldItemMainhand();
			if (item.isEmpty())
				return;
			
			centerItem = item;
			altar = null;
		}
		
		modification.modify(world, player, centerItem, otherItems, center, recipe);
		if (altar != null) {
			altar.setItem(centerItem);
		}
	}

	@Override
	public List<String> getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return "modify_item_gen";
	}
}
