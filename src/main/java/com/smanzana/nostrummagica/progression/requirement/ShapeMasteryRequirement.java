package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;

public class ShapeMasteryRequirement implements IRequirement{

	private final SpellShape shape;
	
	public ShapeMasteryRequirement(SpellShape shape) {
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
	public List<ITextComponent> getDescription(PlayerEntity player) {
		return Lists.newArrayList(new TranslationTextComponent("info.requirement.shape", 
				shape.getDisplayName().plainCopy().withStyle(TextFormatting.DARK_GREEN)));
	}
}
