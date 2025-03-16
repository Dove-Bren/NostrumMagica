package com.smanzana.nostrummagica.client.gui.mirror;

import com.smanzana.nostrummagica.client.gui.book.BookScreen;

import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.components.AbstractWidget;

public interface IMirrorScreen {

	public void addWidget(AbstractWidget widget);
	
	public void resetWidgets();
	
	public void addMinorTab(IMirrorMinorTab tab);
	
	public void showPopupScreen(BookScreen popup);
	
	public Screen getGuiHelper();
	
}
