package com.smanzana.nostrummagica.client.gui.mirror;

import com.smanzana.nostrummagica.client.gui.book.BookScreen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.Widget;

public interface IMirrorScreen {

	public void addWidget(Widget widget);
	
	public void resetWidgets();
	
	public void addMinorTab(IMirrorMinorTab tab);
	
	public void showPopupScreen(BookScreen popup);
	
	public Screen getGuiHelper();
	
}
