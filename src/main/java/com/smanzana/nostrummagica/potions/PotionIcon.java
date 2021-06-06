package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

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
	LIGHTNINGATTACK(9, 0);
	
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
	
	@SideOnly(Side.CLIENT)
	public void draw(Minecraft mc, int posX, int posY) {
		mc.renderEngine.bindTexture(text);
		
		Gui.drawModalRectWithCustomSizedTexture(posX, posY,
				TEXT_OFFSETU + (u * 18), TEXT_OFFSETV + (v * 18),
				18, 18, TEXT_WIDTH, TEXT_HEIGHT);
	}
	
}
