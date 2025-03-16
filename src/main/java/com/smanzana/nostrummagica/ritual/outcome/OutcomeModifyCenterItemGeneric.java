package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;
import java.util.stream.Collectors;

import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.level.Level;

public class OutcomeModifyCenterItemGeneric implements IRitualOutcome {
	
	public static interface ItemModification {
		public void modify(Level world, Player player, ItemStack item, List<ItemStack> otherItems, BlockPos center, RitualRecipe recipe);
	}

	private ItemModification modification;
	private List<Component> description;
	
	public OutcomeModifyCenterItemGeneric(ItemModification modification, List<String> description) {
		this(modification, description.stream().map(s -> new TextComponent(s)).collect(Collectors.toList()), false);
	}
	
	public OutcomeModifyCenterItemGeneric(ItemModification modification, List<Component> description, boolean dummy) {
		this.modification = modification;
		this.description = description;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getBlockEntity(center);
		ItemStack centerItem = layout.getCenterItem(world, center);
		if (recipe.getTier() == 0 || centerItem.isEmpty()) {
			// enchant item on player
			ItemStack item = player.getMainHandItem();
			if (item.isEmpty())
				return;
			
			centerItem = item;
			altar = null;
		}
		
		modification.modify(world, player, centerItem, layout.getExtraItems(world, center), center, recipe);
		if (altar != null) {
			altar.setItem(centerItem);
		}
	}

	@Override
	public List<Component> getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return "modify_item_gen";
	}
}
