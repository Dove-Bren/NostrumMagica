package com.smanzana.nostrummagica.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;

public class TextUtils {

	/**
	 * Translates "key" and then turns '|' into newlines
	 * @param key
	 * @return
	 */
	public static final List<Component> GetTranslatedList(String key, Object ... parameters) {
		final String base = I18n.get(key, parameters);
		final String[] lines = base.split("\\|");
		final List<Component> list = new ArrayList<>(lines.length);
		for (String line : lines) {
			list.add(new TextComponent(line));
		}
		return list;
	}
	
}
