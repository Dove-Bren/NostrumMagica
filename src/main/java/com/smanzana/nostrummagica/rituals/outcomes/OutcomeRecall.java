package com.smanzana.nostrummagica.rituals.outcomes;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.rituals.RitualRecipe;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

public class OutcomeRecall implements IRitualOutcome {

	public OutcomeRecall() {
		;
	}
	
	@Override
	public void perform(World world, EntityPlayer player, ItemStack centerItem, ItemStack otherItems[], BlockPos center, RitualRecipe recipe) {
		// Return the player to their marked location, if they have one
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		BlockPos pos = attr.getMarkLocation();
		if (pos == null) {
			if (world.isRemote)
				player.addChatMessage(new TextComponentTranslation("info.recall.fail", new Object[0]));
			return;
		}
		
		if (player.dimension == attr.getMarkDimension()) {
			if (!world.isRemote)
				player.setPositionAndUpdate(pos.getX() + .5, pos.getY(), pos.getZ() + .5);
		} else {
			player.addChatMessage(new TextComponentTranslation("info.recall.baddimension", new Object[0]));
		}
	}
}
