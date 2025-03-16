package com.smanzana.nostrummagica.client.gui.book;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.Font;

public interface IBookPage {

	public void draw(BookScreen parent, PoseStack matrixStackIn, Font fonter, int xoffset, int yoffset, int width, int height);

	public void overlay(BookScreen parent, PoseStack matrixStackIn, Font fonter, int mouseX, int mouseY, int trueX, int trueY);
	
}
