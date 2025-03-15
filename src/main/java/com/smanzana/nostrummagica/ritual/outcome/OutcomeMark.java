package com.smanzana.nostrummagica.ritual.outcome;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.ritual.IRitualLayout;
import com.smanzana.nostrummagica.ritual.RitualRecipe;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.TextUtils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

public class OutcomeMark implements IRitualOutcome {

	public OutcomeMark() {
		;
	}
	
	@Override
	public void perform(World world, PlayerEntity player, BlockPos center, IRitualLayout layout, RitualRecipe recipe) {
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
	public List<ITextComponent> getDescription() {
		return TextUtils.GetTranslatedList("ritual.outcome.mark.desc");
	}
}
