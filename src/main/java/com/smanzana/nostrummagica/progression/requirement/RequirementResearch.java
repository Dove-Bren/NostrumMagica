package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class RequirementResearch implements IRequirement{

	private String researchKey;
	
	public RequirementResearch(String key) {
		this.researchKey = key;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getCompletedResearches().contains(researchKey);
	}

	@Override
	public boolean isValid() {
		return NostrumResearch.lookup(researchKey) != null;
	}

	@Override
	public List<ITextComponent> getDescription() {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.research", 
				new TranslationTextComponent("research." + researchKey + ".name").mergeStyle(TextFormatting.DARK_AQUA)));
	}
}
