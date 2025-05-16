package com.smanzana.nostrummagica.spell;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum EElementalMastery {
	UNKNOWN,
	NOVICE,
	ADEPT,
	MASTER;
	
	private final Component name;
	
	private EElementalMastery() {
		name = new TranslatableComponent("mastery." + this.getTranslationKey() + ".name");
	}
	
	public Tag toNBT() {
		return StringTag.valueOf(this.name());
	}
	
	public static EElementalMastery fromNBT(Tag nbt) {
		try {
			return EElementalMastery.valueOf(
					((StringTag) nbt).getAsString().toUpperCase()
				);
		} catch (Exception e) {
			return EElementalMastery.NOVICE;
		}
	}
	
	public String getTranslationKey() {
		return this.name().toLowerCase();
	}
	
	public Component getName() {
		return name;
	}
	
	public boolean isGreaterOrEqual(EElementalMastery other) {
		// Cheat and use ordinals
		return this.ordinal() >= other.ordinal();
	}
}