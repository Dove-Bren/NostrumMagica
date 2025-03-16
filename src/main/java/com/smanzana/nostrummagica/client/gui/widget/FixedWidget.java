package com.smanzana.nostrummagica.client.gui.widget;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.network.chat.Component;

/**
 * Vanilla's widget class has a bad implementation of the mouse released method that
 * greedily always returns true if it's the left-mouse-button. This makes things that use
 * widgets not be able to base anything on the return of the method.
 * 
 * So just fix it.
 * @author Skyler
 *
 */
public abstract class FixedWidget extends AbstractWidget {

	public FixedWidget(int x, int y, int width, int height, Component title) {
		super(x, y, width, height, title);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		// The Vanilla implementation:
		
		//////////////////////////////////////////
		//if (this.isValidClickButton(button)) {
		//	this.onRelease(mouseX, mouseY);
		//	return true;
		//} else {
		//	return false;
		//}
		//////////////////////////////////////////
		
		// I'm not sure it's even worth calling this?
		if (this.isValidClickButton(button)) {
			this.onRelease(mouseX, mouseY);
		}
		
		return false;
	}
	
	
}
