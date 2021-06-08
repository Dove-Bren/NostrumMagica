package com.smanzana.nostrummagica.utils;

import net.minecraft.entity.passive.EntitySheep;
import net.minecraft.item.EnumDyeColor;

public class ColorUtil {
	public static final int colorToARGB(float red, float green, float blue, float alpha) {
		return (int)Math.min(255, alpha * 255) << 24
				| (int)Math.min(255, red * 255) << 16
				| (int)Math.min(255, green * 255) << 8
				| (int)Math.min(255, blue * 255) << 0
				;
	}
	
	public static final int colorToARGB(float red, float green, float blue) {
		return colorToARGB(red, green, blue, 1f);
	}
	
	public static final int colorToARGB(float[] colors) {
		if (colors.length == 3) {
			return colorToARGB(colors[0], colors[1], colors[2]);
		} else {
			return colorToARGB(colors[0], colors[1], colors[2], colors[3]);
		}
	}
	
	public static final int dyeToARGB(EnumDyeColor color) {
		return colorToARGB(EntitySheep.getDyeRgb(color));
	}
	
	public static final float[] ARGBToColor(int ARGB) {
		return new float[] {
			(float) ((ARGB >> 16) & 0xFF) / 255f, // red
			(float) ((ARGB >> 8) & 0xFF) / 255f, // green
			(float) ((ARGB >> 0) & 0xFF) / 255f, // blue
			(float) ((ARGB >> 24) & 0xFF) / 255f // alpha
		};
	}
}
