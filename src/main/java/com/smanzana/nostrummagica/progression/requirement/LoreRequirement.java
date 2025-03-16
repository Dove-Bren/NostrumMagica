package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class LoreRequirement implements IRequirement{

	private ILoreTagged lore;
	
	public LoreRequirement(String key) {
		this(LoreRegistry.instance().lookup(key));
	}
	
	public LoreRequirement(ILoreTagged tagged) {
		this.lore = tagged;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.hasLore(lore);
	}

	@Override
	public boolean isValid() {
		return lore != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.lore", 
				new TextComponent(lore.getLoreDisplayName()).withStyle(ChatFormatting.DARK_BLUE)));
	}
}
