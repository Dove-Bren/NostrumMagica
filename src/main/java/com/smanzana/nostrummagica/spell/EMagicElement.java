package com.smanzana.nostrummagica.spell;

import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextFormatting;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public enum EMagicElement {

	PHYSICAL("Physical", 0xFF223344),
	LIGHTNING("Lightning", 0xFFA5A530),
	FIRE("Fire", 0xFFC21C00),
	EARTH("Earth", 0xFF60350D),
	ICE("Ice", 0xFF3294A3),
	WIND("Wind", 0xFF36A035),
	ENDER("Ender", 0xFF41117F);
	
	private final String name;
	private final int color;
	
	private EMagicElement(String name, int color) {
		this.name = name;
		this.color = color;
		// I wanted to store opposite, but can't do that in constructor here
	}

	public String getName() {
		return name;
	}

	public int getColor() {
		return color;
	}
	
	@OnlyIn(Dist.CLIENT)
	public TextFormatting getChatColor() {
		switch(this) {
		case EARTH:
			return TextFormatting.GOLD;
		case ENDER:
			return TextFormatting.DARK_PURPLE;
		case FIRE:
			return TextFormatting.DARK_RED;
		case ICE:
			return TextFormatting.AQUA;
		case LIGHTNING:
			return TextFormatting.YELLOW;
		case PHYSICAL:
			return TextFormatting.DARK_GRAY;
		case WIND:
			return TextFormatting.DARK_GREEN;
		}
		return null;
	}
	
	public @Nullable EMagicElement getOpposite() {
		switch (this) {
		case EARTH:
			return WIND;
		case ENDER:
			return LIGHTNING;
		case FIRE:
			return ICE;
		case ICE:
			return FIRE;
		case LIGHTNING:
			return ENDER;
		case PHYSICAL:
			return null;
		case WIND:
			return EARTH;
		}
		
		return null;
	}
	
	public boolean isSupportingElement(EMagicElement element) {
		switch (this) {
		case EARTH:
			return element == ICE;
		case ENDER:
			return element == LIGHTNING;
		case FIRE:
			return element == EARTH;
		case ICE:
			return element == WIND;
		case LIGHTNING:
			return element == ENDER;
		case PHYSICAL:
			return element != PHYSICAL;
		case WIND:
			return element == FIRE;
		}
		
		return false;
	}
	
	public boolean isOpposingElement(EMagicElement element) {
		switch (this) {
		case EARTH:
			return element == FIRE;
		case ENDER:
			return element == PHYSICAL;
		case FIRE:
			return element == WIND;
		case ICE:
			return element == EARTH;
		case LIGHTNING:
			return element == PHYSICAL;
		case PHYSICAL:
			return false;
		case WIND:
			return element == ICE;
		}
		
		return false;
	}
	
	public static EMagicElement getRandom(Random rand) {
		return EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
	}
	
	public static String[] GetNames() {
		String[] names = new String[values().length];
		int i = 0;
		for (EMagicElement elem : values()) {
			names[i++] = elem.getName();
		}
		return names;
	}
	
	public INBT toNBT() {
		return StringNBT.valueOf(this.name().toLowerCase());
	}
	
	public static final EMagicElement FromNBT(INBT nbt) {
		EMagicElement element = EMagicElement.PHYSICAL;
		try {
			element = EMagicElement.valueOf(((StringNBT) nbt).getAsString().toUpperCase());
		} catch (Exception e) {
			;
		}
		return element;
	}
}
