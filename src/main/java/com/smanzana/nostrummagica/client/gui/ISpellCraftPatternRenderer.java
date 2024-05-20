package com.smanzana.nostrummagica.client.gui;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ISpellCraftPatternRenderer {

	@OnlyIn(Dist.CLIENT)
	public abstract void drawPatternIcon(MatrixStack matrixStackIn, SpellCraftPattern pattern, SpellCraftContext context, int width, int height, float red, float green, float blue, float alpha);
	
	
	
	
	static final Map<ResourceLocation, ISpellCraftPatternRenderer> Renderers = new HashMap<>();
	
	public static void RegisterRenderer(SpellCraftPattern pattern, ISpellCraftPatternRenderer renderer) {
		Renderers.put(pattern.getRegistryName(), renderer);
	}
	
	public static ISpellCraftPatternRenderer GetRenderer(SpellCraftPattern pattern) {
		return Renderers.getOrDefault(pattern.getRegistryName(), SpellCraftPatternAutoRenderer.INSTANCE);
	}
}
