package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.FontRenderer;

public class PlainTextPage implements IBookPage {
	
	private String text; // REMOVE the whole book thing?
	
	public PlainTextPage(String text) {
		this.text = text;
	}

	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		RenderFuncs.drawSplitString(matrixStackIn, fonter, text, xoffset, yoffset, width, 0x000000);
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		; //nothing to do
	}
	
}
