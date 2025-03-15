package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.INBT;
import net.minecraft.nbt.StringNBT;
import net.minecraft.util.text.TextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public enum EMagicTier {
	LOCKED,
	MANI,
	KANI,
	VANI,
	LANI,
	;
	
	private EMagicTier() {
		
	}
	
	public boolean isGreaterOrEqual(EMagicTier tier) {
		return this.ordinal() >= tier.ordinal();
	}
	
	public String getRawName() {
		return getName().getString();
	}
	
	public TextComponent getName() {
		return new TranslationTextComponent("tier." + this.name().toLowerCase() + ".name");
	}
	
	public INBT toNBT() {
		return StringNBT.valueOf(this.name().toLowerCase());
	}

	public static final EMagicTier FromNBT(INBT nbt) {
		StringNBT tag = (StringNBT) nbt;
		try {
			return EMagicTier.valueOf(tag.getAsString().toUpperCase());
		} catch (Exception e) {
			
		}
		return EMagicTier.LOCKED;
	}
}