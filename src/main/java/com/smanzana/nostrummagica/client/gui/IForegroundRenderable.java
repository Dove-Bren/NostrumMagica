package com.smanzana.nostrummagica.client.gui;

import com.mojang.blaze3d.vertex.PoseStack;

public interface IForegroundRenderable {

	public void renderForeground(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks);
	
}
