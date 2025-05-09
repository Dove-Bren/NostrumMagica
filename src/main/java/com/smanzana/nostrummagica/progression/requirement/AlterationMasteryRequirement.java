package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class AlterationMasteryRequirement implements IRequirement{

	private EAlteration alteration;
	
	public AlterationMasteryRequirement(EAlteration alteration) {
		this.alteration = alteration;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		Boolean bool = attr.getAlterations().get(alteration);
		return (bool != null && bool);
	}

	@Override
	public boolean isValid() {
		return alteration != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.alteration", 
				alteration.getDisplayName().copy().withStyle(ChatFormatting.AQUA)));
	}
}
