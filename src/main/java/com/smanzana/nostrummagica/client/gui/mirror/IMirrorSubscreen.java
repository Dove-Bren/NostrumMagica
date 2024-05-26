package com.smanzana.nostrummagica.client.gui.mirror;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.ITextComponent;

public interface IMirrorSubscreen {
	
	public ITextComponent getName();
	
	public ItemStack getIcon();
	
	public boolean isVisible(IMirrorScreen parent, PlayerEntity player);

	public void show(IMirrorScreen parent, PlayerEntity player, int width, int height, int guiLeft, int guiTop);
	
	public void hide(IMirrorScreen parent, PlayerEntity player);
	
	public void drawBackground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks);
	
	public void drawForeground(IMirrorScreen parent, MatrixStack matrixStackIn, int width, int height, int mouseX, int mouseY, float partialTicks);
	
}
