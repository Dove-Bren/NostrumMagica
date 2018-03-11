package com.smanzana.nostrummagica.spelltome.enhancement;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.entity.EntityLivingBase;

/**
 * Lowers the amount of mana required to cast a spell
 * Amount lowered is simple 5% * level
 * @author Skyler
 */
public class EnhancementLowerManaCost extends SpellTomeEnhancement {

	public EnhancementLowerManaCost() {
		super("lower_mana_cost");
	}

	@Override
	public int getMaxLevel() {
		return 6;
	}

	@Override
	public int getWeight(int level) {
		return level / 4; // int div
	}

	@Override
	public void onCast(int level, SpellCastSummary summaryIn, EntityLivingBase source, INostrumMagic attributes) {
		float mod = level > 0 ? .05f * level : 0f;
		summaryIn.addCostRate(-mod);
	}

}
