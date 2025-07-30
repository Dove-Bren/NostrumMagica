package com.smanzana.nostrummagica.spell;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.util.StringRepresentable;

public enum EMagicElement implements StringRepresentable {

	NEUTRAL(0xFF223344),
	FIRE(0xFFC21C00),
	ICE(0xFF3294A3),
	EARTH(0xFF60350D),
	WIND(0xFF36A035),
	LIGHTNING(0xFFA5A530),
	ENDER(0xFF41117F);
	
	private final int color;
	private final Component name;
	private final Component description;
	private final List<Component> tooltip;
	
	private EMagicElement(int color) {
		this.color = color;
		// I wanted to store opposite, but can't do that in constructor here
		
		this.name = new TranslatableComponent("element." + name().toLowerCase() + ".name");
		this.description = new TranslatableComponent("element." + name().toLowerCase() + ".desc");
		this.tooltip = List.of(
				name.copy().withStyle(ChatFormatting.BOLD),
				description
			);
	}

	public String getBareName() {
		return name.getString();
	}
	
	public Component getDisplayName() {
		return this.name;
	}
	
	public Component getDescription() {
		return this.description;
	}

	public int getColor() {
		return color;
	}
	
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
		case NEUTRAL:
			return ChatFormatting.DARK_GRAY;
		case WIND:
			return ChatFormatting.DARK_GREEN;
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
		case NEUTRAL:
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
		case NEUTRAL:
			return element != NEUTRAL;
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
			return element == NEUTRAL;
		case FIRE:
			return element == WIND;
		case ICE:
			return element == EARTH;
		case LIGHTNING:
			return element == NEUTRAL;
		case NEUTRAL:
			return false;
		case WIND:
			return element == ICE;
		}
		
		return false;
	}
	
	public static EMagicElement getRandom(Random rand) {
		return EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
	}
	
	public static EMagicElement parse(String value) {
		EMagicElement element = EMagicElement.NEUTRAL;
		try {
			element = EMagicElement.valueOf(value.toUpperCase());
		} catch (Exception e) {
			;
		}
		return element;
	}
	
	public Tag toNBT() {
		return StringTag.valueOf(this.name().toLowerCase());
	}
	
	public static final EMagicElement FromNBT(Tag nbt) {
		return EMagicElement.parse(((StringTag) nbt).getAsString().toUpperCase());
	}

	public List<Component> getTooltip() {
		return tooltip;
	}

	@Override
	public String getSerializedName() {
		return this.name().toLowerCase();
	}
	
	@Override
	public String toString() {
		return this.getSerializedName();
	}
}
