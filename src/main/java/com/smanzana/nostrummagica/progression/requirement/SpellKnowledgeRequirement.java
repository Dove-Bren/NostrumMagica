package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellAction;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class SpellKnowledgeRequirement implements IRequirement{

	private final EMagicElement element;
	private final @Nullable EAlteration alteration;
	
	public SpellKnowledgeRequirement(EMagicElement element, @Nullable EAlteration alteration) {
		this.element = element;
		this.alteration = alteration;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.hasKnowledge(element, alteration);
	}

	@Override
	public boolean isValid() {
		return element != null;
	}

	@Override
	public List<ITextComponent> getDescription(PlayerEntity player) {
		SpellAction action = Spell.solveAction(alteration, element, 1);
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.spellknowledge", 
				action.getName().withStyle(TextFormatting.DARK_PURPLE)));
	}
}
