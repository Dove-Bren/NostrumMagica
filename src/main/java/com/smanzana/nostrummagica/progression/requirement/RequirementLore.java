package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class RequirementLore implements IRequirement{

	private ILoreTagged lore;
	
	public RequirementLore(String key) {
		this(LoreRegistry.instance().lookup(key));
	}
	
	public RequirementLore(ILoreTagged tagged) {
		this.lore = tagged;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.hasLore(lore);
	}

	@Override
	public boolean isValid() {
		return lore != null;
	}

	@Override
	public List<ITextComponent> getDescription() {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.lore", 
				new StringTextComponent(lore.getLoreDisplayName()).mergeStyle(TextFormatting.DARK_BLUE)));
	}
}
