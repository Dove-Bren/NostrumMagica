package com.smanzana.nostrummagica.client.gui.infoscreen;

import java.util.Collection;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.Lore;
import com.smanzana.nostrummagica.util.RenderFuncs;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.AbstractWidget;

public class LoreInfoSubScreen implements IInfoSubScreen {

	private ILoreTagged tag;
	
	public LoreInfoSubScreen(ILoreTagged tag) {
		this.tag = tag;
	}
	
	@Override
	public void draw(INostrumMagic attr, Minecraft mc, PoseStack matrixStackIn, int x, int y, int width, int height, int mouseX, int mouseY) {
		String title = this.tag.getLoreDisplayName();
		int len = mc.font.width(title);
		mc.font.drawShadow(matrixStackIn, title, x + (width / 2) + (-len / 2), y, 0xFFFFFFFF);
		
		Lore lore = attr.getLore(this.tag);
		
		List<String> data = lore.getData();
		int i = 0;
		for (String line : data) {
			// Draw line, splitting where we have to. Record how much vertical space it took.
			i += RenderFuncs.drawSplitString(matrixStackIn, mc.font, line,
					x + 5,
					y + 35 + i,
					width, 0xFFFFFFFF);
			// Add an extra line break to space out lines.
			i += mc.font.lineHeight;
		}
	}

	@Override
	public Collection<AbstractWidget> getWidgets(int x, int y, int width, int height) {
		return null;
	}

}
