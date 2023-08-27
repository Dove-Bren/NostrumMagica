package com.smanzana.nostrummagica.client.gui;

import com.smanzana.nostrummagica.client.gui.book.BookScreen;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public interface GuiBook {

	@OnlyIn(Dist.CLIENT)
	public BookScreen getScreen(Object userdata);
	
}
