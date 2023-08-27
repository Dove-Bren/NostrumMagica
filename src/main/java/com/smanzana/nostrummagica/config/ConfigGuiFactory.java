package com.smanzana.nostrummagica.config;

import java.util.Set;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screen.Screen;
import net.minecraftforge.fml.client.IModGuiFactory;

public class ConfigGuiFactory implements IModGuiFactory {

	@Override
	public void initialize(Minecraft minecraftInstance) {
		;
	}

	@Override
	public Set<RuntimeOptionCategoryElement> runtimeGuiCategories() {
		return null;
	}

	@Override
	public boolean hasConfigGui() {
		return true;
	}

	@Override
	public Screen createConfigGui(Screen parentScreen) {
		return new ModConfigGui(parentScreen);
	}

}
