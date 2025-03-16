package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.skill.Skill;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class SkillRequirement implements IRequirement{

	private final Skill skill;
	
	public SkillRequirement(Skill skill) {
		this.skill = skill;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.hasSkill(skill);
	}

	@Override
	public boolean isValid() {
		return skill != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.spellknowledge", 
				((BaseComponent) skill.getName()).withStyle(ChatFormatting.DARK_PURPLE)));
	}
}
