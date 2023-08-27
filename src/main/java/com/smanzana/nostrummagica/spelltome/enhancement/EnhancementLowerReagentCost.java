package com.smanzana.nostrummagica.spelltome.enhancement;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.LivingEntity;

/**
 * Lowers the amount of reagents needed to cast a spell
 * Amount lowered is simple 5% * level
 * @author Skyler
 */
public class EnhancementLowerReagentCost extends SpellTomeEnhancement {

	public EnhancementLowerReagentCost() {
		super("lower_reagent_cost");
	}

	@Override
	public int getMaxLevel() {
		return 6;
	}

	@Override
	public int getWeight(int level) {
		return level / 3; // int div
	}

	@Override
	public void onCast(int level, SpellCastSummary summaryIn, LivingEntity source, INostrumMagic attributes) {
		float mod = level > 0 ? .05f * level : 0f;
		summaryIn.addReagentCost(-mod);
	}

}
