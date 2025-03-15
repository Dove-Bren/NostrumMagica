package com.smanzana.nostrummagica.client.gui;

import javax.annotation.Nullable;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.util.text.StringTextComponent;

public class StackableScreen extends Screen {

	private @Nullable Screen prevScreen;
	
	public StackableScreen() {
		super(new StringTextComponent("StableSceenParent"));
	}
	
	public StackableScreen(Screen prevScreen) {
		this();
		this.setPrevScreen(prevScreen);
	}
	
	public @Nullable Screen getPrevScreen() {
		return this.prevScreen;
	}
	
	public void setPrevScreen(@Nullable Screen prevScreen) {
		this.prevScreen = prevScreen;
	}
	
	@Override
	public boolean keyPressed(int p_keyPressed_1_, int p_keyPressed_2_, int p_keyPressed_3_) {
		if (p_keyPressed_1_ == 256 && prevScreen != null) {
			this.minecraft.setScreen(prevScreen);
			this.setPrevScreen(null);
			return true;
		}
		
		return super.keyPressed(p_keyPressed_1_, p_keyPressed_2_, p_keyPressed_3_);
	}
}
