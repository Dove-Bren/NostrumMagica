package com.smanzana.nostrummagica.client.gui.mirror;

import com.mojang.blaze3d.matrix.MatrixStack;

import net.minecraft.util.text.ITextComponent;

public interface IMirrorMinorTab {

	public void onClick(IMirrorScreen parent, IMirrorSubscreen subscreen);
	
	public ITextComponent getName();
	
	public void renderTab(IMirrorScreen parent, IMirrorSubscreen subscreen, MatrixStack matrixStackIn, int width, int height);
	
	public boolean hasNewEntry(IMirrorScreen parent, IMirrorSubscreen subscreen);
	
	public boolean isVisible(IMirrorScreen parent, IMirrorSubscreen subscreen);
	
}
