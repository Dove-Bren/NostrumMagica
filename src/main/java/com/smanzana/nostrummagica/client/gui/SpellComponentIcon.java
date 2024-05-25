package com.smanzana.nostrummagica.client.gui;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Nice wrapper to rendering spell component icons
@OnlyIn(Dist.CLIENT)
public class SpellComponentIcon {

	private static Map<EMagicElement, SpellComponentIcon> elementCache = new EnumMap<>(EMagicElement.class);
	private static Map<EAlteration, SpellComponentIcon> alterationCache = new EnumMap<>(EAlteration.class);
	private static Map<String, SpellComponentIcon> shapeCache = new HashMap<>();
	private static Map<String, SpellComponentIcon> triggerCache = new HashMap<>(); 
	
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
	
	public static SpellComponentIcon get(SpellTrigger trigger) {
		String name = trigger.getTriggerKey();
		SpellComponentIcon icon = triggerCache.get(name);
		
		if (icon == null) {
			icon = new SpellComponentIcon(trigger);
			triggerCache.put(trigger.getTriggerKey(), icon);
		}
		
		return icon;
	}
	
	private static int uWidthElement = 32;
	private static int uWidthAlteration = 32;
	private static int uWidthTrigger = 32;
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
	
	public SpellComponentIcon(SpellTrigger trigger) {
		width = uWidthTrigger;
		height = uWidthTrigger;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/trigger_" + trigger.getTriggerKey().toLowerCase() + ".png");
	}
	
	public SpellComponentIcon(SpellShape shape) {
		width = uWidthShape;
		height = uWidthShape;
		
		model = new ResourceLocation(NostrumMagica.MODID,
				"textures/models/symbol/shape_" + shape.getShapeKey().toLowerCase() + ".png");
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
	
	public ResourceLocation getModelLocation() {
		return model;
	}
	
}
