package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.gui.FontRenderer;

public class TableOfContentsPage implements IClickableBookPage {

	private boolean title;
	private String[] pages;
	private Integer[] indices;
	
	private int widthCache;
	private int xCache;
	private int yCache;
	private int fontHeightCache;
	
	public TableOfContentsPage(String[] pages, Integer[] indices, boolean title) {
		this.title = title;
		this.pages = pages;
		this.indices = indices;
	}
	
	@Override
	public void draw(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int xoffset, int yoffset, int width, int height) {
		widthCache = width;
		xCache = xoffset;
		fontHeightCache = fonter.lineHeight;
		
		if (title) {
			int x = xoffset + (width / 2);
			x -= fonter.width("Table Of Contents") / 2;
			fonter.draw(matrixStackIn, "Table Of Contents", x, yoffset + 5, 0xFF202020);
			yoffset += 10 + (fonter.lineHeight);
		}
		
		yCache = yoffset;
		
		for (int i = 0; i < pages.length; i++) {
			fonter.draw(matrixStackIn, pages[i], xoffset, yoffset, 0xFF400070);
			yoffset += fonter.lineHeight + 2;
		}
	}

	@Override
	public void overlay(BookScreen parent, MatrixStack matrixStackIn, FontRenderer fonter, int mouseX, int mouseY, int trueX, int trueY) {
		if (title) {
			mouseY -= fonter.lineHeight + 10;
		}
		int index = mouseY / (fonter.lineHeight + 2);
		if (index < pages.length && index >= 0)
			RenderFuncs.drawRect(matrixStackIn, xCache, yCache + (index * (fonter.lineHeight + 2)) - 1, xCache + widthCache, yCache + (index * (fonter.lineHeight + 2) + fonter.lineHeight) - 1, 0x30000000);
	}
	
	protected boolean onElementClick(BookScreen parent, int index, int button) {
		if (index < pages.length) {
			parent.requestPageChange(indices[index]);
			return true;
		}
		return false;
	}

	@Override
	public boolean onClick(BookScreen parent, double mouseX, double mouseY, int button) {
		if (title) {
			mouseY -= fontHeightCache + 10;
		}
		if (button == 0) {
			int index = (int) mouseY / (fontHeightCache + 2);
			if (onElementClick(parent, index, button)) {
				return true;
			}
		}
		
		return false;
	}
	
}