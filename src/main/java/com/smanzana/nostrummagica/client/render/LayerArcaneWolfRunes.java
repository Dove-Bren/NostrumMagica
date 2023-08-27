package com.smanzana.nostrummagica.client.render;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.entity.RenderArcaneWolf;
import com.smanzana.nostrummagica.entity.EntityArcaneWolf;

import com.mojang.blaze3d.platform.GlStateManager;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.util.ResourceLocation;

public class LayerArcaneWolfRunes implements LayerRenderer<EntityArcaneWolf> {

	private static final ResourceLocation RUNE_LOC = new ResourceLocation(NostrumMagica.MODID, "textures/entity/arcane_wolf/overlay.png");
	private final RenderArcaneWolf wolfRenderer;
	
	public LayerArcaneWolfRunes(RenderArcaneWolf wolfRenderer) {
		this.wolfRenderer = wolfRenderer;
	}
	
	@Override
	public void doRenderLayer(EntityArcaneWolf wolf, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch, float scale) {
		if (!wolf.isInvisible()) {
			this.wolfRenderer.bindTexture(RUNE_LOC);
			final float brightPeriod = 40f;
			final float brightness = .8f + .2f * (float) Math.sin(Math.PI * 2 * ((partialTicks + wolf.ticksExisted) % brightPeriod) / brightPeriod);
			final int ARGB = wolf.getRuneColor();
			final float alpha = (float)((ARGB >> 24) & 0xFF) / 255f;
			final float red = (float)((ARGB >> 16) & 0xFF) / 255f;
			final float green = (float)((ARGB >> 8) & 0xFF) / 255f;
			final float blue = (float)((ARGB >> 0) & 0xFF) / 255f;
			GlStateManager.color4f(red * brightness, green * brightness, blue * brightness, alpha);
			wolfRenderer.getMainModel().render(wolf, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch, scale);
		}
	}
	
	@Override
	public boolean shouldCombineTextures() {
		return true;
	}
}
