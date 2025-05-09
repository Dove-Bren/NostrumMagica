package com.smanzana.nostrummagica.client.gui.commonwidget;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class Tooltip implements ITooltip {
	
	protected final List<Component> staticTooltip;

	private Tooltip(List<Component> staticTooltip) {
		this.staticTooltip = staticTooltip;
	}
	
	public static Tooltip create(List<Component> tooltip) {
		return new Tooltip(tooltip);
	}
	
	public static Tooltip create(Component tooltip) {
		return new Tooltip(List.of(tooltip));
	}
	
	@Override
	public List<Component> get() {
		return this.staticTooltip;
	}
	
	public static final void RenderTooltip(ITooltip tooltip, Screen parent, PoseStack matrixStackIn, int mouseX, int mouseY) {
		final var rawTooltip = tooltip.get();
		if (rawTooltip != null && !rawTooltip.isEmpty()) {
			matrixStackIn.pushPose();
			matrixStackIn.translate(0, 0, 100);
			parent.renderComponentTooltip(matrixStackIn, rawTooltip, mouseX, mouseY);
			matrixStackIn.popPose();
		}
	}
	
}
