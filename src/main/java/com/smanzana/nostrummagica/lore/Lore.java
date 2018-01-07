package com.smanzana.nostrummagica.lore;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Collection of lore information.
 * All strings are expected to already be translated!
 * @author Skyler
 *
 */
public class Lore {

	private List<String> data;
	
	public Lore() {
		this.data = new LinkedList<>();
	}
	
	public Lore(String firstLine) {
		this();
		this.add(firstLine);
	}
	
	public Lore(Collection<String> lines) {
		this();
		this.add(lines);
	}
	
	public List<String> getData() {
		return data;
	}
	
	public Lore add(String line) {
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
	
}
