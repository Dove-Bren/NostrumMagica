package com.smanzana.nostrummagica.client.gui;

import com.smanzana.nostrummagica.client.gui.book.BookScreen;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public interface GuiBook {

	@SideOnly(Side.CLIENT)
	public BookScreen getScreen();
	
}
