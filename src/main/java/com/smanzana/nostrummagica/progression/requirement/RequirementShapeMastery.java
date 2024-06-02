package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.components.shapes.SpellShape;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class RequirementShapeMastery implements IRequirement{

	private final SpellShape shape;
	
	public RequirementShapeMastery(SpellShape shape) {
		this.shape = shape;
	}

	@Override
	public boolean matches(PlayerEntity player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getShapes().contains(shape);
	}

	@Override
	public boolean isValid() {
		return shape != null;
	}

	@Override
	public List<ITextComponent> getDescription() {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.shape", 
				new StringTextComponent(shape.getDisplayName()).mergeStyle(TextFormatting.DARK_GREEN)));
	}
}
