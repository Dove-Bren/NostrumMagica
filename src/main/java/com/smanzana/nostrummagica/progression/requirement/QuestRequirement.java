package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class QuestRequirement implements IRequirement{

	private String questKey;
	
	public QuestRequirement(String key) {
		this.questKey = key;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getCompletedQuests().contains(questKey);
	}

	@Override
	public boolean isValid() {
		return NostrumQuest.lookup(questKey) != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.quest", 
				new TranslatableComponent("quest." + questKey + ".name").withStyle(ChatFormatting.BLUE)));
	}
}
