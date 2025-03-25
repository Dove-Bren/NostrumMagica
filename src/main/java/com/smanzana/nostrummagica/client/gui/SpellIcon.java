package com.smanzana.nostrummagica.client.gui;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.systems.RenderSystem;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

// Wrapper that makes it easy to render the little icon the user has selected for a spell
@OnlyIn(Dist.CLIENT)
public class SpellIcon {

	public static final ResourceLocation TEX = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/spellicons.png");
	
	public static final int numIcons = 59;
	
	private static final int TEX_WIDTH = 256;
	private static final int TEX_ICON_WIDTH = 32;
	private static final int TEX_HCOUNT = TEX_WIDTH / TEX_ICON_WIDTH;
	
	private int u;
	private int v;
	
	public SpellIcon(int index) {
		index %= numIcons;
		u = TEX_ICON_WIDTH * (index % TEX_HCOUNT);
		v = TEX_ICON_WIDTH * (index / TEX_HCOUNT);
	}
	
	public void render(Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height) {
		render(mc, matrixStackIn, x, y, width, height, 1f, 1f, 1f, 1f);
	}
	
	public void render(Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, float red, float green, float blue, float alpha) {
		matrixStackIn.pushPose();

		RenderSystem.setShaderTexture(0, TEX);
		
		RenderSystem.enableBlend();
		RenderFuncs.drawScaledCustomSizeModalRectImmediate(matrixStackIn, x, y, u, v, TEX_ICON_WIDTH, TEX_ICON_WIDTH, width, height, TEX_WIDTH, TEX_WIDTH,
				red, green, blue, alpha);
		
		matrixStackIn.popPose();
	}
	
	public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int combinedLightIn, float red, float green, float blue, float alpha, int width, int height) {
		RenderFuncs.drawScaledCustomSizeModalRect(matrixStackIn, bufferIn.getBuffer(NostrumRenderTypes.GetIconType(TEX)), 0, 0, u, v, TEX_ICON_WIDTH, TEX_ICON_WIDTH, width, height, TEX_WIDTH, TEX_WIDTH,
				red, green, blue, alpha);
	}
	
	// Make it easy to pool these
	private static Map<Integer, SpellIcon> pool = new HashMap<>();
	
	public static SpellIcon get(int index) {
		SpellIcon icon = pool.get(index);
		
		if (icon == null) {
			icon = new SpellIcon(index);
			pool.put(index, icon);
		}
		
		return icon;
	}
}
