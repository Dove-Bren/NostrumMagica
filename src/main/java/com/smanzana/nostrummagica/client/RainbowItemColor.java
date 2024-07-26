package com.smanzana.nostrummagica.client;

import java.awt.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.item.ItemStack;

public class RainbowItemColor implements IItemColor {
	
	private final int tintIndex;
	
	public RainbowItemColor(int tintIndex) {
		this.tintIndex = tintIndex;
	}

	@Override
	public int getColor(ItemStack stack, int index) {
		if (index == this.tintIndex) {
			final Minecraft mc = Minecraft.getInstance();
			final float period = 20 * 60;
			final float time = mc.player.ticksExisted + mc.getRenderPartialTicks();
			final float prog = ((time % period) / period);
			return 0xFF000000 | Color.HSBtoRGB(prog, .8f, 1f);
		}
		return 0xFFFFFFFF;
	}

}
