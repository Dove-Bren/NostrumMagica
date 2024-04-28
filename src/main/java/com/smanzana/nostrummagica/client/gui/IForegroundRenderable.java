package com.smanzana.nostrummagica.client.gui;

import com.mojang.blaze3d.matrix.MatrixStack;

public interface IForegroundRenderable {

	public void renderForeground(MatrixStack matrixStackIn, int parX, int parY, float partialTicks);
	
}
