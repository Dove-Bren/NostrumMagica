package com.smanzana.nostrummagica.client.gui.book;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;

public class ReferencePage extends TableOfContentsPage {
	
	private String[] references;
	
	public ReferencePage(String[] pages, String[] references, boolean title) {
		super(pages, null, title);
		this.references = references;
	}
	
	protected boolean onElementClick(BookScreen parent, int index, int button) {
		if (index < references.length) {
			Player player = (Player) NostrumMagica.instance.proxy.getPlayer();
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return false;
			
			// If we're nested in another screen, set up prev links
			Minecraft mc = Minecraft.getInstance();
			Screen holdingScreen = mc.screen;
			if (holdingScreen == parent) {
				holdingScreen = null;
			}
			
			InfoScreen screen = new InfoScreen(attr, references[index]);
			screen.setPrevScreen(holdingScreen);
			Minecraft.getInstance().setScreen(screen);
			
			return true;
		}
		return false;
	}

}
