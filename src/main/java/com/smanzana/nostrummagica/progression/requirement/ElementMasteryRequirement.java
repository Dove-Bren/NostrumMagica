package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class ElementMasteryRequirement implements IRequirement{

	private final EMagicElement element;
	private final EElementalMastery level;
	 
	public ElementMasteryRequirement(EMagicElement element, EElementalMastery level) {
		this.element = element;
		this.level = level;
	}
	
	public ElementMasteryRequirement(EMagicElement element) {
		this(element, EElementalMastery.NOVICE);
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		final EElementalMastery currentMastery = attr.getElementalMastery(this.element);
		return currentMastery.isGreaterOrEqual(level);
	}

	@Override
	public boolean isValid() {
		return element != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.element",
					level.getName(),
					element.getDisplayName().copy().withStyle(ChatFormatting.DARK_RED)
				));
	}
}
