package com.smanzana.nostrummagica.client.gui.book;

import java.util.List;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;

public class LinedTextPage implements IBookPage {
	
	public static final int LINE_HEIGHT_EXTRA = 3;
	
	private List<String> text;
	
	public LinedTextPage(List<String> text) {
		this.text = text;
	}
	
	public LinedTextPage(String ... text) {
		this.text = Lists.newArrayList(text);
	}

	@Override
	public void draw(BookScreen parent, PoseStack matrixStackIn, Font fonter, int xoffset, int yoffset, int width, int height) {
		for (String line : text) {
			fonter.draw(matrixStackIn, line, xoffset, yoffset, 0x000000);
			yoffset += fonter.lineHeight + LINE_HEIGHT_EXTRA;
		}
	}

	@Override
	public void overlay(BookScreen parent, PoseStack matrixStackIn, Font fonter, int mouseX, int mouseY, int trueX, int trueY) {
		; //nothing to do
	}
	
}
