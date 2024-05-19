package com.smanzana.nostrummagica.client.gui;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.spellcraft.SpellCraftContext;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

/**
 * Renders an icon based on the registry name of the pattern.
 * Takes patterns with registry names "example:testpattern" and renders
 * "example:textures/pattern/testpattern.png".
 * Assumes texture is 32x32
 * @author Skyler
 *
 */
public class SpellCraftPatternAutoRenderer implements ISpellCraftPatternRenderer {
	
	public static final SpellCraftPatternAutoRenderer INSTANCE = new SpellCraftPatternAutoRenderer();
	
	private Map<ResourceLocation, ResourceLocation> iconMap;
	
	protected SpellCraftPatternAutoRenderer() {
		iconMap = new HashMap<>();
	}
	
	protected ResourceLocation getTexture(ResourceLocation key) {
		if (iconMap.containsKey(key)) {
			return iconMap.get(key);
		}
		
		ResourceLocation textureLoc = makeTextureLoc(key);
		iconMap.put(key, textureLoc);
		return textureLoc;
	}
	
	private static final ResourceLocation makeTextureLoc(ResourceLocation key) {
		return new ResourceLocation(key.getNamespace(), "textures/pattern/" + key.getPath() + ".png");
	}

	@Override
	public void drawPatternIcon(MatrixStack matrixStackIn, SpellCraftPattern pattern, SpellCraftContext context,
			int width, int height, float red, float green, float blue, float alpha) {
		ResourceLocation texture = getTexture(pattern.getRegistryName());
		
		Minecraft.getInstance().getTextureManager().bindTexture(texture);
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, 0, 0, 0, 0, 32, 32, width, height, 32, 32, red, green, blue, alpha);
	}

}
