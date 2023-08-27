package com.smanzana.nostrummagica.client.gui.book;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.smanzana.nostrummagica.utils.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;

public class ImagePage implements IBookPage {
	
	private ResourceLocation image;
	
	private int width;
	
	private int height;
	
	private int uoffset;
	
	private int voffset;
	
	private int textWidth;
	
	private int textHeight;
	
	private int widthCache;
	
	private int heightCache;
	
	private List<String> tooltip;
	
	public ImagePage(ResourceLocation image, int width, int height) {
		this(image, width, height, 0, 0);
	}
	
	public ImagePage(ResourceLocation image, int width, int height, List<String> tooltip) {
		this(image, width, height, 0, 0);
		this.tooltip = tooltip;
	}
	
	public ImagePage(ResourceLocation image, int width, int height, int uoffset, int voffset) {
		this(image, width, height, uoffset, voffset, -1, -1, null);
	}
	
	public ImagePage(ResourceLocation image, int width, int height, int uoffset, int voffset, int textWidth, int textHeight, List<String> tooltip) {
		this.image = image;
		this.width = width;
		this.height = height;
		this.uoffset = uoffset;
		this.voffset = voffset;
		this.textWidth = textWidth;
		this.textHeight = textHeight;
		this.tooltip = tooltip;
	}

	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		widthCache = width;
		heightCache = height;
		
		Minecraft.getInstance().getTextureManager().bindTexture(image);
		
		int centerx = xoffset + (width / 2);
		int centery = yoffset + (height / 2);
		int x = centerx - (this.width / 2);
		int y = centery - (this.height / 2);
		
		GlStateManager.color4f(1.0f, 1.0f, 1.0f, 1.0f);
		GlStateManager.enableBlend();
		if (textWidth == -1 || textHeight == -1)
			RenderFuncs.drawModalRectWithCustomSizedTexture(x, y, uoffset, voffset, this.width, this.height, 256, 256);
		else
			RenderFuncs.drawModalRectWithCustomSizedTexture(x, y, uoffset, voffset, this.width, this.height, textWidth, textHeight);
		GlStateManager.disableBlend();
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		if (tooltip != null) {
			int centerx = widthCache / 2;
			int centery = heightCache / 2;
			int x = centerx - (this.width / 2);
			int y = centery - (this.height / 2);
			
			if (mouseX > x && mouseX < x + this.width)
			if (mouseY > y && mouseY < y + this.height)
				parent.renderTooltip(tooltip, trueX, trueY);
		}
	}
	
}
