package com.smanzana.nostrummagica.spelltome.enhancement;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.world.entity.LivingEntity;

/**
 * Increases the amount of XP gained
 * Amount is +10% per level
 * @author Skyler
 */
public class EnhancementBonusXP extends SpellTomeEnhancement {

	public EnhancementBonusXP() {
		super("xp_bonus");
	}

	@Override
	public int getMaxLevel() {
		return 2;
	}

	@Override
	public int getWeight(int level) {
		return level + 1;
	}

	@Override
	public void onCast(int level, SpellCastSummary summaryIn, LivingEntity source, INostrumMagic attributes, float applyRate) {
		float mod = level > 0 ? .1f * level : 0f;
		summaryIn.addXPRate(mod * applyRate);
	}

}
