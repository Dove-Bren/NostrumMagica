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
	public void draw(INostrumMagic attr, Minecraft mc, int x, int y, int width, int height) {
		Lore lore = attr.getLore(this.tag);
		
		List<String> data = lore.getData();
		int i = 0;
		for (String line : data)
			mc.fontRendererObj.drawSplitString(line,
					x + 5,
					y + 5 + (i++ * 20),
					width, 0xFFFFFFFF);
	}

}
