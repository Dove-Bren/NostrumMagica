package com.smanzana.nostrummagica.client.gui;

import java.io.IOException;

import javax.annotation.Nullable;

import net.minecraft.client.gui.GuiScreen;

public class StackableScreen extends GuiScreen {

	private @Nullable GuiScreen prevScreen;
	
	public StackableScreen() {
		super();
	}
	
	public StackableScreen(GuiScreen prevScreen) {
		this();
		this.setPrevScreen(prevScreen);
	}
	
	public @Nullable GuiScreen getPrevScreen() {
		return this.prevScreen;
	}
	
	public void setPrevScreen(@Nullable GuiScreen prevScreen) {
		this.prevScreen = prevScreen;
	}
	
	@Override
	protected void keyTyped(char typedChar, int keyCode) throws IOException {
		if (keyCode == 1 && prevScreen != null) {
			this.mc.displayGuiScreen(prevScreen);
			this.setPrevScreen(null);
			return;
		}
		
		super.keyTyped(typedChar, keyCode);
	}
}
