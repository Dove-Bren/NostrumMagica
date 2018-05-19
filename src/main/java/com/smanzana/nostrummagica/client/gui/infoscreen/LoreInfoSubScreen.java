package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.List;

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
	public void draw(INostrumMagic attr, Minecraft mc, int x, int y, int width, int height, int mouseX, int mouseY) {
		String title = this.tag.getLoreDisplayName();
		int len = mc.fontRendererObj.getStringWidth(title);
		mc.fontRendererObj.drawStringWithShadow(title, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		
		Lore lore = attr.getLore(this.tag);
		
		List<String> data = lore.getData();
		int i = 0;
		for (String line : data)
			mc.fontRendererObj.drawSplitString(line,
					x + 5,
					y + 35 + (i++ * 17),
					width, 0xFFFFFFFF);
	}

}
