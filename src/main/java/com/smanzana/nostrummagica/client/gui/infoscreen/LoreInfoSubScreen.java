package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;

import net.minecraft.client.Minecraft;

public class LoreInfoSubScreen implements IInfoSubScreen {

	private ILoreTagged tag;
	
	public LoreInfoSubScreen(ILoreTagged tag) {
		this.tag = tag;
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, MatrixStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		String title = this.tag.getLoreDisplayName();
		int len = mc.fontRenderer.getStringWidth(title);
		mc.fontRenderer.drawStringWithShadow(matrixStackIn, title, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		
		Lore lore = attr.getLore(this.tag);
		
		List<String> data = lore.getData();
		int i = 0;
		for (String line : data)
			mc.fontRenderer.drawString(matrixStackIn, line,
					x + 5,
					y + 35 + (i++ * 17),
					0xFFFFFFFF);
	}

	@Override
	public Collection<ISubScreenButton> getButtons() {
		return null;
	}

}
