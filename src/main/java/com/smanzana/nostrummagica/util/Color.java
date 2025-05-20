package com.smanzana.nostrummagica.util;

import net.minecraft.util.Mth;

public class Color {

	public final float red;
	public final float green;
	public final float blue;
	public final float alpha;
	
	public Color(float red, float green, float blue, float alpha) {
		this.red = Mth.clamp(red, 0, 1);
		this.green = Mth.clamp(green, 0, 1);
		this.blue = Mth.clamp(blue, 0, 1);
		this.alpha = Mth.clamp(alpha, 0, 1);
	}
	
	public Color(float[] ARGB) {
		this(ARGB[0], ARGB[1], ARGB[2], ARGB[3]);
	}
	
	public Color(int ARGB) {
		this(ColorUtil.ARGBToColor(ARGB));
	}
	
	public int toARGB() {
		return ColorUtil.colorToARGB(red, green, blue, alpha);
	}
	
	public Color multiply(float red, float green, float blue, float alpha) {
		return new Color(this.red * red, this.green * green, this.blue * blue, this.alpha * alpha);
	}
	
	public Color scale(float scale) {
		return multiply(scale, scale, scale, scale);
	}
	
	public Color scaleAlpha(float alphaScale) {
		return multiply(1f, 1f, 1f, alphaScale);
	}
	
}
