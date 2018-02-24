package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeMark implements IRitualOutcome {

	public OutcomeMark() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Set player's mark location to their current location
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.setMarkLocation(player.dimension, player.getPosition());
	}
}
