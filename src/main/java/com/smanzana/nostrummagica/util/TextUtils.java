package com.smanzana.nostrummagica.util;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;

public class TextUtils {

	/**
	 * Translates "key" and then turns '|' into newlines
	 * @param key
	 * @return
	 */
	public static final List<ITextComponent> GetTranslatedList(String key, Object ... parameters) {
		final String base = I18n.format(key, parameters);
		final String[] lines = base.split("\\|");
		final List<ITextComponent> list = new ArrayList<>(lines.length);
		for (String line : lines) {
			list.add(new StringTextComponent(line));
		}
		return list;
	}
	
}
