package com.smanzana.nostrummagica.loretag;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;

/**
 * Collection of lore information.
 * All strings are expected to already be translated!
 * @author Skyler
 *
 */
public class Lore {

	private List<Component> data;
	
	public Lore() {
		this.data = new ArrayList<>();
	}
	
	public Lore(String unlocalized) {
		this();
		
		if (unlocalized.contains(" ")) {
			NostrumMagica.logger.warn("Unlocalized lore text appears to have a space in it. This seems unintentional: " + unlocalized);
		}
		this.add(this.getLoreLines(unlocalized));
	}
	
	public Lore(ResourceLocation key) {
		this(key.toString().replace(':', '.'));
	}
	
	public Lore(Collection<String> lines) {
		this();
		this.add(lines);
	}
	
	public List<Component> getData() {
		return data;
	}
	
	public Lore add(String line) {
		return add(new TextComponent(line));
	}
	
	public Lore add(Component line) {
		data.add(line);
		return this;
	}
	
	public Lore add(String ... lines) {
		for (String line : lines)
			add(line);
		
		return this;
	}
	
	public Lore add(Collection<String> lines) {
		for (String line : lines)
			add(line);
		
		return this;
	}
	
	protected String[] getLoreLines(String unloc) {
		return I18n.get(unloc).split("\\|");
	}
	
}
