package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;
import java.util.stream.Collectors;

import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;

public class OutcomeModifyCenterItemGeneric implements IRitualOutcome {
	
	public static interface ItemModification {
		public void modify(World world, PlayerEntity player, ItemStack item, List<ItemStack> otherItems, BlockPos center, RitualRecipe recipe);
	}

	private ItemModification modification;
	private List<ITextComponent> description;
	
	public OutcomeModifyCenterItemGeneric(ItemModification modification, List<String> description) {
		this(modification, description.stream().map(s -> new StringTextComponent(s)).collect(Collectors.toList()), false);
	}
	
	public OutcomeModifyCenterItemGeneric(ItemModification modification, List<ITextComponent> description, boolean dummy) {
		this.modification = modification;
		this.description = description;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
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
	public List<ITextComponent> getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return "modify_item_gen";
	}
}
