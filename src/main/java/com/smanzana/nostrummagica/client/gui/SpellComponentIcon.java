package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.util.ResourceLocation;
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
	
	public void draw(MatrixStack matrixStackIn, int xOffset, int yOffset, int width, int height) {
		draw(matrixStackIn, xOffset, yOffset, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void draw(MatrixStack matrixStackIn, int xOffset, int yOffset, int width, int height, float red, float green,
			float blue, float alpha) {
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
