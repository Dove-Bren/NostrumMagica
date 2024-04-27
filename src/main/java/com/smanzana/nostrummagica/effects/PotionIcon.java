package com.smanzana.nostrummagica.effects;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum PotionIcon {

	FROSTBITE(0, 0),
	MAGICRESIST(1, 0),
	MAGICSHIELD(2, 0),
	PHYSICALSHIELD(3, 0),
	ROOTED(4, 0),
	MAGICBOOST(5, 0),
	FAMILIAR(6, 0),
	ENCHANT(7, 0),
	LIGHTNINGMOVE(8, 0),
	LIGHTNINGATTACK(9, 0),
	NATURESBLESSING(10, 0),
	MANAREGEN(11, 0),
	;
	
	private static final ResourceLocation text = new ResourceLocation(
			NostrumMagica.MODID, "textures/gui/icons.png");
	private static final int TEXT_OFFSETU = 0;
	private static final int TEXT_OFFSETV = 128;
	private static final int TEXT_WIDTH = 256;
	private static final int TEXT_HEIGHT = 256;
	
	private int u;
	private int v;
	
	private PotionIcon(int u, int v) {
		this.u = u;
		this.v = v;
	}
	
	@OnlyIn(Dist.CLIENT)
	public void draw(Minecraft mc, int posX, int posY) {
		mc.getRenderManager().textureManager.bindTexture(text);
		
		RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, posX,
				posY, TEXT_OFFSETU + (u * 18),
				TEXT_OFFSETV + (v * 18), 18, 18, TEXT_WIDTH, TEXT_HEIGHT);
	}
	
}
