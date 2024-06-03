package com.smanzana.nostrummagica.spell;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum EElementalMastery {
	UNKNOWN,
	NOVICE,
	ADEPT,
	MASTER;
	
	private final ITextComponent name;
	
	private EElementalMastery() {
		name = new TranslationTextComponent("mastery." + this.getTranslationKey() + ".name");
	}
	
	public INBT toNBT() {
		return StringNBT.valueOf(this.name());
	}
	
	public static EElementalMastery fromNBT(INBT nbt) {
		try {
			return EElementalMastery.valueOf(
					((StringNBT) nbt).getString().toUpperCase()
				);
		} catch (Exception e) {
			return EElementalMastery.NOVICE;
		}
	}
	
	public String getTranslationKey() {
		return this.name().toLowerCase();
	}
	
	public ITextComponent getName() {
		return name;
	}
	
	public boolean isGreaterOrEqual(EElementalMastery other) {
		// Cheat and use ordinals
		return this.ordinal() >= other.ordinal();
	}
}