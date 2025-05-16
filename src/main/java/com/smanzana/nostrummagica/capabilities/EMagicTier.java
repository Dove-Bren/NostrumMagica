package com.smanzana.nostrummagica.capabilities;

import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.TranslatableComponent;

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
	
	public BaseComponent getName() {
		return new TranslatableComponent("tier." + this.name().toLowerCase() + ".name");
	}
	
	public Tag toNBT() {
		return StringTag.valueOf(this.name().toLowerCase());
	}

	public static final EMagicTier FromNBT(Tag nbt) {
		StringTag tag = (StringTag) nbt;
		try {
			return EMagicTier.valueOf(tag.getAsString().toUpperCase());
		} catch (Exception e) {
			
		}
		return EMagicTier.LOCKED;
	}
}