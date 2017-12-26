package com.smanzana.nostrummagica.client.gui.book;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.util.ResourceLocation;

public class HSplitPage implements IBookPage {
	
	private static ResourceLocation divide = new ResourceLocation(NostrumMagica.MODID + ":textures/gui/divide.png");
	
	private static final int TEXT_WIDTH = 150;
	
	private static final int TEXT_HEIGHT = 10;
	
	private IBookPage top;
	
	private IBookPage bottom;
	
	private boolean drawSplit;
	
	private int heightCache;
	
	public HSplitPage(IBookPage top, IBookPage bottom) {
		this(top, bottom, false);
	}
	
	public HSplitPage(IBookPage top, IBookPage bottom, boolean drawSplit) {
		this.top = top;
		this.bottom = bottom;
		this.drawSplit = drawSplit;
	}

	@Override
	public void draw(BookScreen parent, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		heightCache = height;
		
		int divideSize = 10; //amount in middle as seperation.
		if (!drawSplit)
			divideSize = 0;
		int subheight = (height - divideSize) / 2;
		
		//draw dividing line on bottom (if applicable)
		if (drawSplit) {
			Minecraft.getMinecraft().getTextureManager().bindTexture(divide);
			Gui.drawModalRectWithCustomSizedTexture(xoffset, yoffset + (subheight + (divideSize / 2)),
					0, 0, width, divideSize, TEXT_WIDTH, TEXT_HEIGHT);
		}
		
		top.draw(parent, fonter, xoffset, yoffset, width, subheight);
		
		yoffset += (subheight + divideSize); //offset a subheight + divide length down
		bottom.draw(parent, fonter, xoffset, yoffset, width, subheight);
	}

	@Override
	public void overlay(BookScreen parent, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		int divideSize = 10; //amount in middle as seperation.
		int subheight = (heightCache - divideSize) / 2;
		
		//find out of in top of bottom
		if (mouseY < subheight) {
			top.overlay(parent, fonter, mouseX, mouseY, trueX, trueY);
		} else if (mouseY > subheight + divideSize) {
			bottom.overlay(parent, fonter, mouseX, mouseY - (subheight + divideSize), trueX, trueY);
		}
	}
	
}
