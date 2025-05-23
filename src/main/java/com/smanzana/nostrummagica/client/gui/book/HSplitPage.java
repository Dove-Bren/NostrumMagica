package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.Font;
import net.minecraft.resources.ResourceLocation;

public class HSplitPage implements IClickableBookPage {
	
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
	public void draw(BookScreen parent, PoseStack matrixStackIn, Font fonter, int xoffset, int yoffset, int width, int height) {
		heightCache = height;
		
		int divideSize = 10; //amount in middle as seperation.
		if (!drawSplit)
			divideSize = 0;
		int subheight = (height - divideSize) / 2;
		
		//draw dividing line on bottom (if applicable)
		if (drawSplit) {
			RenderSystem.setShaderTexture(0, divide);
			RenderFuncs.drawModalRectWithCustomSizedTextureImmediate(matrixStackIn, xoffset,
					yoffset + (subheight + (divideSize / 2)), 0, 0, width, divideSize, TEXT_WIDTH, TEXT_HEIGHT);
		}
		
		top.draw(parent, matrixStackIn, fonter, xoffset, yoffset, width, subheight);
		
		if (bottom != null) {
			yoffset += (subheight + divideSize); //offset a subheight + divide length down
			bottom.draw(parent, matrixStackIn, fonter, xoffset, yoffset, width, subheight);
		}
	}

	@Override
	public void overlay(BookScreen parent, PoseStack matrixStackIn, Font fonter, int mouseX, int mouseY, int trueX, int trueY) {
		int divideSize = 10; //amount in middle as seperation.
		int subheight = (heightCache - divideSize) / 2;
		
		//find out of in top of bottom
		if (mouseY < subheight) {
			top.overlay(parent, matrixStackIn, fonter, mouseX, mouseY, trueX, trueY);
		} else if (bottom != null && mouseY > subheight + divideSize) {
			bottom.overlay(parent, matrixStackIn, fonter, mouseX, mouseY - (subheight + divideSize), trueX, trueY);
		}
	}

	@Override
	public boolean onClick(BookScreen parent, double mouseX, double mouseY, int button) {
		int divideSize = 10; //amount in middle as seperation.
		if (!drawSplit)
			divideSize = 0;
		int subheight = (heightCache - divideSize) / 2;
		
		if (mouseY < subheight && top != null && top instanceof IClickableBookPage) {
			((IClickableBookPage) top).onClick(parent, mouseX, mouseY, button);
		} else if (mouseY > subheight + divideSize && bottom != null && bottom instanceof IClickableBookPage) {
			((IClickableBookPage) bottom).onClick(parent, mouseX, mouseY - (subheight + divideSize), button);
		}
		return false;
	}
	
}
