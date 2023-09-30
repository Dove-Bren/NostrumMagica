package com.smanzana.nostrummagica.client.gui.book;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.entity.player.PlayerEntity;

public class ReferencePage extends TableOfContentsPage {
	
	private String[] references;
	
	public ReferencePage(String[] pages, String[] references, boolean title) {
		super(pages, null, title);
		this.references = references;
	}
	
	protected boolean onElementClick(BookScreen parent, int index, int button) {
		if (index < references.length) {
			PlayerEntity player = (PlayerEntity) NostrumMagica.instance.proxy.getPlayer();
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return false;
			
			// If we're nested in another screen, set up prev links
			Minecraft mc = Minecraft.getInstance();
			Screen holdingScreen = mc.currentScreen;
			if (holdingScreen == parent) {
				holdingScreen = null;
			}
			
			InfoScreen screen = new InfoScreen(attr, references[index]);
			screen.setPrevScreen(holdingScreen);
			Minecraft.getInstance().displayGuiScreen(screen);
			
			return true;
		}
		return false;
	}

}
