package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.progression.skill.Skill;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class SkillRequirement implements IRequirement{

	private final Skill skill;
	
	public SkillRequirement(Skill skill) {
		this.skill = skill;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.hasSkill(skill);
	}

	@Override
	public boolean isValid() {
		return skill != null;
	}

	@Override
	public List<ITextComponent> getDescription(PlayerEntity player) {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.spellknowledge", 
				((TextComponent) skill.getName()).withStyle(TextFormatting.DARK_PURPLE)));
	}
}
