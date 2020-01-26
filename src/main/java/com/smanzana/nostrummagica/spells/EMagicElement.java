package com.smanzana.nostrummagica.spells;

import com.mojang.realmsclient.gui.ChatFormatting;

import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public enum EMagicElement {

	PHYSICAL("Physical", 0xFF223344),
	LIGHTNING("Lightning", 0xFFA5A530),
	FIRE("Fire", 0xFFC21C00),
	EARTH("Earth", 0xFF60350D),
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
	
	@SideOnly(Side.CLIENT)
	public ChatFormatting getChatColor() {
		switch(this) {
		case EARTH:
			return ChatFormatting.GOLD;
		case ENDER:
			return ChatFormatting.DARK_PURPLE;
		case FIRE:
			return ChatFormatting.DARK_RED;
		case ICE:
			return ChatFormatting.AQUA;
		case LIGHTNING:
			return ChatFormatting.YELLOW;
		case PHYSICAL:
			return ChatFormatting.DARK_GRAY;
		case WIND:
			return ChatFormatting.DARK_GREEN;
		}
		return null;
	}
}
