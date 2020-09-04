package com.smanzana.nostrummagica.client.gui.book;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;

public class ReferencePage extends TableOfContentsPage {
	
	private String[] references;
	
	public ReferencePage(String[] pages, String[] references, boolean title) {
		super(pages, null, title);
		this.references = references;
	}
	
	protected boolean onElementClick(BookScreen parent, int index, int button) {
		if (index < references.length) {
			EntityPlayer player = (EntityPlayer) NostrumMagica.proxy.getPlayer();
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return false;
			
			Minecraft.getMinecraft().displayGuiScreen(new InfoScreen(attr, references[index]));
			return true;
		}
		return false;
	}

}
