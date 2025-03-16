package com.smanzana.nostrummagica.client.gui.mirror;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.network.chat.Component;

public interface IMirrorMinorTab {

	public void onClick(IMirrorScreen parent, IMirrorSubscreen subscreen);
	
	public Component getName();
	
	public void renderTab(IMirrorScreen parent, IMirrorSubscreen subscreen, PoseStack matrixStackIn, int width, int height);
	
	public boolean hasNewEntry(IMirrorScreen parent, IMirrorSubscreen subscreen);
	
	public boolean isVisible(IMirrorScreen parent, IMirrorSubscreen subscreen);
	
}
