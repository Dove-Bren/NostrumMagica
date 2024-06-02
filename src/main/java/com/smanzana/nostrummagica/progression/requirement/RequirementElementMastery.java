package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class RequirementElementMastery implements IRequirement{

	private EMagicElement element;
	
	public RequirementElementMastery(EMagicElement element) {
		this.element = element;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		Boolean known = attr.getKnownElements().get(element);
		return (known != null && known);
	}

	@Override
	public boolean isValid() {
		return element != null;
	}

	@Override
	public List<ITextComponent> getDescription() {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.element", 
				new StringTextComponent(element.getName()).mergeStyle(TextFormatting.DARK_RED)));
	}
}
