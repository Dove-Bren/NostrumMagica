package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.tile.AltarTileEntity;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class OutcomeCreateTome implements IRitualOutcome {

	public OutcomeCreateTome() {
		;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Take plates and pages from the altars.
		ItemStack tome = SpellTome.Create(layout.getCenterItem(world, center), layout.getExtraItems(world, center));
		if (tome.isEmpty())
			return;
		
		// If there's an altar, we'll enchant the item there
		// Otherwise enchant the item the player has
		AltarTileEntity altar = (AltarTileEntity) world.getBlockEntity(center);
		if (recipe.getTier() == 0) {
			// give to player
			
			player.inventory.add(tome);
		} else {
			altar.setItem(tome);
		}
	}
	
	@Override
	public String getName() {
		return "create_tome";
	}

	@Override
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.create_tome.desc");
	}
}
