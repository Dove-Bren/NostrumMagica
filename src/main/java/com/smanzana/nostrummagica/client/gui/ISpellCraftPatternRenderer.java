package com.smanzana.nostrummagica.client.gui;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface ISpellCraftPatternRenderer {

	@OnlyIn(Dist.CLIENT)
	public abstract void drawPatternIconInGui(PoseStack matrixStackIn, SpellCraftPattern pattern, int width, int height, float red, float green, float blue, float alpha);
	
	@OnlyIn(Dist.CLIENT)
	public abstract void drawPatternIcon(PoseStack matrixStackIn, SpellCraftPattern pattern, MultiBufferSource bufferIn, int width, int height, float red, float green, float blue, float alpha);
	
	static final Map<ResourceLocation, ISpellCraftPatternRenderer> Renderers = new HashMap<>();
	
	public static void RegisterRenderer(SpellCraftPattern pattern, ISpellCraftPatternRenderer renderer) {
		Renderers.put(pattern.getRegistryName(), renderer);
	}
	
	public static ISpellCraftPatternRenderer GetRenderer(SpellCraftPattern pattern) {
		return Renderers.getOrDefault(pattern.getRegistryName(), SpellCraftPatternAutoRenderer.INSTANCE);
	}
}
