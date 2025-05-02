package com.smanzana.nostrummagica.client.gui.widget;

import net.minecraft.client.Minecraft;
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
	
	@Override
	public boolean mouseClicked(double p_93641_, double p_93642_, int p_93643_) {
		// The vanilla implementation:
		
		//
		//if (this.active && this.visible) {
		//	if (this.isValidClickButton(p_93643_)) {
		//		boolean flag = this.clicked(p_93641_, p_93642_);
		//		if (flag) {
		//			this.playDownSound(Minecraft.getInstance().getSoundManager());
		//			this.onClick(p_93641_, p_93642_);
		//			return true;
		//		}
		//	}
		//
		//	return false;
		//} else {
		//	return false;
		//}
		//////////////////////////////////////////
		
		
		if (this.active && this.visible) {
			if (this.isValidClickButton(p_93643_)) {
				boolean flag = this.clicked(p_93641_, p_93642_);
				if (flag) {
					this.playDownSound(Minecraft.getInstance().getSoundManager());
					this.onClick(p_93641_, p_93642_, p_93643_);
					return true;
				}
			}

			return false;
		} else {
			return false;
		}
	}
	
	public void onClick(double mouseX, double mouseY, int button) {
		this.onClick(mouseX, mouseY);
	}
	
	
}
