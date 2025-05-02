package com.smanzana.nostrummagica.client.gui.widget;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.TextComponent;

public class SpacerWidget extends ObscurableChildWidget {

	public SpacerWidget(int x, int y, int width, int height) {
		super(x, y, width, height, TextComponent.EMPTY);
	}
	
	@Override
	protected boolean isValidClickButton(int button) {
		return false; // no click consumption
	}
	
	@Override
	public void renderButton(PoseStack matrixStackIn, int mouseX, int mouseY, float partialTicks) {
		; // render nothing
	}

}
