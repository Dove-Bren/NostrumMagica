package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

public class ResearchRequirement implements IRequirement{

	private ResourceLocation researchKey;
	
	public ResearchRequirement(ResourceLocation key) {
		this.researchKey = key;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getCompletedResearches().contains(researchKey);
	}

	@Override
	public boolean isValid() {
		return NostrumResearch.lookup(researchKey) != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.research", 
				new TranslatableComponent("research." + researchKey + ".name").withStyle(ChatFormatting.DARK_AQUA)));
	}
}
