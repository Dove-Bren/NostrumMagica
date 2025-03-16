package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Nice wrapper to rendering magic tier icons
@OnlyIn(Dist.CLIENT)
public class MagicTierIcon {

	private static Map<EMagicTier, MagicTierIcon> cache = new EnumMap<>(EMagicTier.class);
	
	public static MagicTierIcon get(EMagicTier tier) {
		MagicTierIcon icon = cache.get(tier);
		if (icon == null) {
			icon = new MagicTierIcon(tier);
			cache.put(tier, icon);
		}
		
		return icon;
	}
	
	private static int uWidth = 32;
	
	private int width;
	private int height;
	private ResourceLocation model;
	
	private MagicTierIcon(EMagicTier tier) {
		width = uWidth;
		height = uWidth;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/gui/tier_" + tier.name().toLowerCase() + ".png");
	}
	
	public void draw(GuiComponent parent, PoseStack matrixStackIn, Font fonter, int xOffset, int yOffset, int width, int height) {
		draw(parent, matrixStackIn, fonter, xOffset, yOffset, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(GuiComponent parent, PoseStack matrixStackIn, Font fonter, int xOffset, int yOffset, int width, int height,
			float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();

		{
			Minecraft.getInstance().getTextureManager().bind(this.getModelLocation());
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, xOffset, yOffset, 0, 0, this.width, this.height, width, height, this.width, this.height,
					red, green, blue, alpha);
			RenderSystem.disableBlend();
		}
		
		matrixStackIn.popPose();
	}
	
	public void draw(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int width, int height, boolean outline) {
		VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(getModelLocation(), outline));
		draw(matrixStackIn, buffer, packedLightIn, width, height);
	}
	
	public void draw(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, int width, int height, boolean outline, float red, float green, float blue, float alpha) {
		VertexConsumer buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(getModelLocation(), outline));
		draw(matrixStackIn, buffer, packedLightIn, width, height, red, green, blue, alpha);
	}
	
	public void draw(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int width, int height) {
		draw(matrixStackIn, buffer, packedLightIn, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(PoseStack matrixStackIn, VertexConsumer buffer, int packedLightIn, int width, int height, float red, float green, float blue, float alpha) {
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, buffer, 0, 0, 0, 0, this.width, this.height, width, height, this.width, this.height,
				red, green, blue, alpha);
		
	}
	
	public ResourceLocation getModelLocation() {
		return model;
	}
	
}
