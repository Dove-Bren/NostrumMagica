package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

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
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		final EElementalMastery currentMastery = attr.getElementalMastery(this.element);
		return currentMastery.isGreaterOrEqual(level);
	}

	@Override
	public boolean isValid() {
		return element != null;
	}

	@Override
	public List<ITextComponent> getDescription(PlayerEntity player) {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.element",
					level.getName(),
					new StringTextComponent(element.getName()).withStyle(TextFormatting.DARK_RED)
				));
	}
}
