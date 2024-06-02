package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EAlteration;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class AlterationMasteryRequirement implements IRequirement{

	private EAlteration alteration;
	
	public AlterationMasteryRequirement(EAlteration alteration) {
		this.alteration = alteration;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		Boolean bool = attr.getAlterations().get(alteration);
		return (bool != null && bool);
	}

	@Override
	public boolean isValid() {
		return alteration != null;
	}

	@Override
	public List<ITextComponent> getDescription(PlayerEntity player) {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.alteration", 
				new StringTextComponent(alteration.getName()).mergeStyle(TextFormatting.AQUA)));
	}
}
