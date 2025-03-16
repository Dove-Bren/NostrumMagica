package com.smanzana.nostrummagica.progression.requirement;

import java.util.List;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.TranslatableComponent;

public class ShapeMasteryRequirement implements IRequirement{

	private final SpellShape shape;
	
	public ShapeMasteryRequirement(SpellShape shape) {
		this.shape = shape;
	}

	@Override
	public boolean matches(Player player) {
		final INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		return attr.getShapes().contains(shape);
	}

	@Override
	public boolean isValid() {
		return shape != null;
	}

	@Override
	public List<Component> getDescription(Player player) {
		return Lists.newArrayList(new TranslatableComponent("info.requirement.shape", 
				shape.getDisplayName().plainCopy().withStyle(ChatFormatting.DARK_GREEN)));
	}
}
