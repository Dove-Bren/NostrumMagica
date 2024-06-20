package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.EMagicTier;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
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
	
	public void draw(AbstractGui parent, MatrixStack matrixStackIn, FontRenderer fonter, int xOffset, int yOffset, int width, int height) {
		draw(parent, matrixStackIn, fonter, xOffset, yOffset, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(AbstractGui parent, MatrixStack matrixStackIn, FontRenderer fonter, int xOffset, int yOffset, int width, int height,
			float red, float green, float blue, float alpha) {
		matrixStackIn.push();

		{
			Minecraft.getInstance().getTextureManager().bindTexture(this.getModelLocation());
			RenderSystem.enableBlend();
			RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, xOffset, yOffset, 0, 0, this.width, this.height, width, height, this.width, this.height,
					red, green, blue, alpha);
			RenderSystem.disableBlend();
		}
		
		matrixStackIn.pop();
	}
	
	public void draw(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, int width, int height, boolean outline) {
		IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(getModelLocation(), outline));
		draw(matrixStackIn, buffer, packedLightIn, width, height);
	}
	
	public void draw(MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, int width, int height, boolean outline, float red, float green, float blue, float alpha) {
		IVertexBuilder buffer = bufferIn.getBuffer(NostrumRenderTypes.GetBlendedEntity(getModelLocation(), outline));
		draw(matrixStackIn, buffer, packedLightIn, width, height, red, green, blue, alpha);
	}
	
	public void draw(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, int width, int height) {
		draw(matrixStackIn, buffer, packedLightIn, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(MatrixStack matrixStackIn, IVertexBuilder buffer, int packedLightIn, int width, int height, float red, float green, float blue, float alpha) {
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, buffer, 0, 0, 0, 0, this.width, this.height, width, height, this.width, this.height,
				red, green, blue, alpha);
		
	}
	
	public ResourceLocation getModelLocation() {
		return model;
	}
	
}
