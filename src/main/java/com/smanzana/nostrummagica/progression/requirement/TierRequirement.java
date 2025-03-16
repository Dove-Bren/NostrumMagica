package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class TierRequirement implements IRequirement{

	private final EMagicTier tier;
	
	public TierRequirement(EMagicTier tier) {
		this.tier = tier;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getTier().isGreaterOrEqual(this.tier);
	}

	@Override
	public boolean isValid() {
		return true;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.tier", 
				tier.getName().withStyle(ChatFormatting.DARK_PURPLE)));
	}
}
