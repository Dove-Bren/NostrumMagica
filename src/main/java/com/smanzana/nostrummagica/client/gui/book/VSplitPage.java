package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;

public class VSplitPage implements IBookPage {
	
	private IBookPage left;
	
	private IBookPage right;
	
	private int widthCache;
	
	public VSplitPage(IBookPage left, IBookPage right) {
		this.left = left;
		this.right = right;
	}

	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		widthCache = width;
		int subwidth = width / 2;
		
		
		left.draw(parent, matrixStackIn, fonter, xoffset, yoffset, subwidth, height);
		
		xoffset += subwidth; //move offset to the right
		right.draw(parent, matrixStackIn, fonter, xoffset, yoffset, subwidth, height);
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		int subwidth = widthCache / 2;
		
		//find out of in left or right
		if (mouseX < subwidth) {
			left.overlay(parent, matrixStackIn, fonter, mouseX, mouseY, trueX, trueY);
		} else {
			right.overlay(parent, matrixStackIn, fonter, mouseX - subwidth, mouseY, trueX, trueY);
		}
	}
	
}
