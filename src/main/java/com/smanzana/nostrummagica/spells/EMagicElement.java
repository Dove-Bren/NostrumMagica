package com.smanzana.nostrummagica.spells;

public enum EMagicElement {

	PHYSICAL("Physical", 0xFF223344),
	FIRE("Fire", 0xFFC21C00),
	EARTH("Earth", 0xFF60350D),
	LIGHTNING("Lightining", 0xFFA5A530),
	ICE("Ice", 0xFF3294A3),
	WIND("Wind", 0xFF36A035),
	ENDER("Ender", 0xFF41117F);
	
	private String name;
	private int color;
	
	private EMagicElement(String name, int color) {
		this.name = name;
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}
}
