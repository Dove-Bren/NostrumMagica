package com.smanzana.nostrummagica.client.gui;

import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
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
	
	public void render(Minecraft mc, int x, int y, int width, int height) {
		GL11.glPushMatrix();

		Minecraft.getInstance().getTextureManager().bindTexture(TEX);
		
		GlStateManager.enableBlend();
		GlStateManager.enableAlphaTest();
		RenderFuncs.drawScaledCustomSizeModalRect(x, y, u, v, TEX_ICON_WIDTH, TEX_ICON_WIDTH, width, height, TEX_WIDTH, TEX_WIDTH);
		
		GL11.glPopMatrix();
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
