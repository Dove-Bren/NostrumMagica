package com.smanzana.nostrummagica.spelltome.enhancement;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spelltome.SpellCastSummary;

import net.minecraft.world.entity.LivingEntity;

/**
 * Increases the speed spells are charged.
 * Amount is +2.5% per level
 * @author Skyler
 */
public class EnhancementCastSpeed extends SpellTomeEnhancement {

	public EnhancementCastSpeed() {
		super("cast_speed");
	}

	@Override
	public int getMaxLevel() {
		return 4;
	}

	@Override
	public int getWeight(int level) {
		return level;
	}

	@Override
	public void onCast(int level, SpellCastSummary summaryIn, LivingEntity source, INostrumMagic attributes, float applyRate) {
		float mod = level > 0 ? .025f * level : 0f;
		summaryIn.addCastSpeedRate(-mod * applyRate);
	}

}
