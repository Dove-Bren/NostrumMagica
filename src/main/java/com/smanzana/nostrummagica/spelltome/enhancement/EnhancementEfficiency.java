package com.smanzana.nostrummagica.spelltome.enhancement;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.world.entity.LivingEntity;

/**
 * Increases the general effectiveness of spells
 * Amount is +5% per level
 * @author Skyler
 */
public class EnhancementEfficiency extends SpellTomeEnhancement {

	public EnhancementEfficiency() {
		super("eff_bonus");
	}

	@Override
	public int getMaxLevel() {
		return 3;
	}

	@Override
	public int getWeight(int level) {
		return level;
	}

	@Override
	public void onCast(int level, SpellCastSummary summaryIn, LivingEntity source, INostrumMagic attributes) {
		float mod = level > 0 ? .05f * level : 0f;
		summaryIn.addEfficiency(mod);;
	}

}
