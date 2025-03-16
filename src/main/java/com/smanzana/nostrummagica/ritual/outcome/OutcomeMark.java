package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;

public class OutcomeMark implements IRitualOutcome {

	public OutcomeMark() {
		;
	}
	
	@Override
	public void perform(Level world, Player player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
		// Set player's mark location to their current location
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null)
			return;
		
		attr.setMarkLocation(DimensionUtils.GetDimension(player), player.blockPosition());
	}
	
	@Override
	public String getName() {
		return "mark";
	}

	@Override
	public List<Component> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.mark.desc");
	}
}
