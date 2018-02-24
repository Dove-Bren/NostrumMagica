package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.items.PositionToken;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class OutcomeConstructGeotoken extends OutcomeSpawnItem {

	public OutcomeConstructGeotoken() {
		super(null);
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// set up stack and then call super to spawn it
		this.stack = PositionToken.constructFrom(centerItem);
		
		super.perform(world, player, centerItem, otherItems, center, recipe);
	}

	private static ItemStack RES = null;
	@Override
	public ItemStack getResult() {
		if (RES == null)
			RES = new ItemStack(PositionToken.instance());
		
		return RES;
	}
	
}
