package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;

public interface IInfoSubScreen {

	public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY);

	public default void drawForeground(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {}
	
	public Collection<AbstractWidget> getWidgets(INostrumMagic attr, int x, int y, int width, int height);
	
}
