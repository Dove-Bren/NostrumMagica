package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.quests.NostrumQuest;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class QuestRequirement implements IRequirement{

	private String questKey;
	
	public QuestRequirement(String key) {
		this.questKey = key;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getCompletedQuests().contains(questKey);
	}

	@Override
	public boolean isValid() {
		return NostrumQuest.lookup(questKey) != null;
	}

	@Override
	public List<ITextComponent> getDescription(PlayerEntity player) {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.quest", 
				new TranslationTextComponent("quest." + questKey + ".name").mergeStyle(TextFormatting.BLUE)));
	}
}
