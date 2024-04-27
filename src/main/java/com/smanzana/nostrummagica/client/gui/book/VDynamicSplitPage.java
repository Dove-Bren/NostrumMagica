package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.client.gui.FontRenderer;

public class VDynamicSplitPage implements IBookPage {
	
	private IBookPage left;
	
	private IBookPage right;
	
	private int leftWidth;
	
	public VDynamicSplitPage(IBookPage left, IBookPage right, int leftWidth) {
		this.left = left;
		this.right = right;
		this.leftWidth = leftWidth;
	}

	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		
		left.draw(parent, matrixStackIn, fonter, xoffset, yoffset, leftWidth, height);
		
		xoffset += leftWidth; //move offset to the right
		right.draw(parent, matrixStackIn, fonter, xoffset, yoffset, width - leftWidth, height);
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		//find out of in left or right
		if (mouseX < leftWidth) {
			left.overlay(parent, matrixStackIn, fonter, mouseX, mouseY, trueX, trueY);
		} else {
			right.overlay(parent, matrixStackIn, fonter, mouseX - leftWidth, mouseY, trueX, trueY);
		}
	}
	
}
