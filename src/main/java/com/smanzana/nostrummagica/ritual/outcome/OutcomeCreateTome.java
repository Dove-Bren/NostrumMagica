package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.equipment.SpellTome;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.PedestalBlockEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class OutcomeCreateTome implements IRitualOutcome {

	public OutcomeCreateTome() {
		;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Take plates and pages from the altars.
		ItemStack tome = SpellTome.Create(layout.getCenterItem(world, center), layout.getExtraItems(world, center));
		if (tome.isEmpty())
			return;
		
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		PedestalBlockEntity altar = (PedestalBlockEntity) world.getBlockEntity(center);
		if (recipe.getTier() == 0) {
			// give to player
			
			player.getInventory().add(tome);
		} else {
			altar.setItem(tome);
		}
	}
	
	@Override
	public String getName() {
		return "create_tome";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.create_tome.desc");
	}
}
