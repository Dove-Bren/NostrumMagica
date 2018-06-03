package com.smanzana.nostrummagica.config;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig.Key;

import net.minecraft.client.gui.GuiScreen;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.fml.client.config.GuiConfig;
import net.minecraftforge.fml.client.config.IConfigElement;

public class ModConfigGui extends GuiConfig {

	public ModConfigGui(GuiScreen parent) {
		super(parent,
		fetchComponents(),
		NostrumMagica.MODID, false, false, GuiConfig.getAbridgedConfigPath(ModConfig.config.base.toString()));
	}
	
	private static List<IConfigElement> fetchComponents() {
		List<IConfigElement> elementList = new LinkedList<IConfigElement>();
		
//		for (Key key : ModConfig.Key.values())
//		if (key.isRuntime()) {
//			elementList.add(new ConfigElement(ModConfig.config.base.getCategory(key.getCategory())));
//		}
		
		for (Key.Category category : ModConfig.Key.Category.values()) {
			if (category == Key.Category.SERVER)
				continue;
			
			ConfigCategory cat = ModConfig.config.base.getCategory(category.getName());
			prepCategory(category);
			elementList.add(new ConfigElement(cat));
		}
		
		return elementList;
	}
	
	private static ConfigCategory prepCategory(Key.Category category) {
		ConfigCategory result = ModConfig.config.base.getCategory(category.getName());
		
		for (ModConfig.Key key : ModConfig.Key.getCategoryKeys(category)) {
			Property prop = result.get(key.getKey());
			prop.setShowInGui(key.isRuntime() && !key.isServerBound());
		}
		
		return result;
	}
	
}
