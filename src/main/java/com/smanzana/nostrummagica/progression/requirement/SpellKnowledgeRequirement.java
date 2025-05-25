package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.SpellEffects;
import com.smanzana.nostrummagica.spell.component.SpellAction;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;

public class SpellKnowledgeRequirement implements IRequirement{

	private final EMagicElement element;
	private final @Nullable EAlteration alteration;
	
	public SpellKnowledgeRequirement(EMagicElement element, @Nullable EAlteration alteration) {
		this.element = element;
		this.alteration = alteration;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.hasKnowledge(element, alteration);
	}

	@Override
	public boolean isValid() {
		return element != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		SpellAction action = SpellEffects.solveAction(alteration, element, 1);
		return Lists.newArrayList(new TranslatableComponent("info.requirement.spellknowledge", 
				action.getName().withStyle(ChatFormatting.DARK_PURPLE)));
	}
}
