package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Nice wrapper to rendering spell component icons
@OnlyIn(Dist.CLIENT)
public class SpellComponentIcon {

	private static Map<EMagicElement, SpellComponentIcon> elementCache = new EnumMap<>(EMagicElement.class);
	private static Map<EAlteration, SpellComponentIcon> alterationCache = new EnumMap<>(EAlteration.class);
	private static Map<String, SpellComponentIcon> shapeCache = new HashMap<>();
	
	public static SpellComponentIcon get(EMagicElement element) {
		SpellComponentIcon icon = elementCache.get(element);
		if (icon == null) {
			icon = new SpellComponentIcon(element);
			elementCache.put(element, icon);
		}
		
		return icon;
	}
	
	public static SpellComponentIcon get(EAlteration alteration) {
		SpellComponentIcon icon = alterationCache.get(alteration);
		if (icon == null) {
			icon = new SpellComponentIcon(alteration);
			alterationCache.put(alteration, icon);
		}
		
		return icon;
	}
	
	public static SpellComponentIcon get(SpellShape shape) {
		String name = shape.getShapeKey();
		SpellComponentIcon icon = shapeCache.get(name);
		
		if (icon == null) {
			icon = new SpellComponentIcon(shape);
			shapeCache.put(shape.getShapeKey(), icon);
		}
		
		return icon;
	}
	
	private static int uWidthElement = 32;
	private static int uWidthAlteration = 32;
	private static int uWidthShape = 32;
	
	private int width;
	private int height;
	private ResourceLocation model;
	
	private SpellComponentIcon(EMagicElement element) {
		width = uWidthElement;
		height = 32;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/element_" + element.name().toLowerCase() + ".png");
	}
	
	private SpellComponentIcon(EAlteration alteration) {
		width = uWidthAlteration;
		height = 32;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/alteration_" + alteration.name().toLowerCase() + ".png");
	}
	
	public SpellComponentIcon(SpellShape shape) {
		width = uWidthShape;
		height = uWidthShape;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/shape_" + shape.getShapeKey().toLowerCase() + ".png");
	}
	
	public void draw(PoseStack matrixStackIn, int xOffset, int yOffset, int width, int height) {
		draw(matrixStackIn, xOffset, yOffset, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(PoseStack matrixStackIn, int xOffset, int yOffset, int width, int height, float red, float green,
			float blue, float alpha) {
		matrixStackIn.pushPose();

		{
			RenderSystem.setShaderTexture(0, this.getModelLocation());
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
