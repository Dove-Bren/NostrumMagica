package com.smanzana.nostrummagica.client.gui.mirror;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;

public interface IMirrorSubscreen {
	
	public Component getName();
	
	public ItemStack getIcon();
	
	public boolean isVisible(IMirrorScreen parent, Player player);

	public void show(IMirrorScreen parent, Player player, int width, int height, int guiLeft, int guiTop);
	
	public void hide(IMirrorScreen parent, Player player);
	
	public void drawBackground(IMirrorScreen parent, PoseStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks);
	
	public void drawForeground(IMirrorScreen parent, PoseStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks);
	
}
