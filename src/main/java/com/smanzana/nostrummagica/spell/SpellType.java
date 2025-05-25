package com.smanzana.nostrummagica.spell;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;

public enum SpellType {
	Crafted(0, true),
	Incantation(40, false),
	AI(0, false),
	;
	
	private final int baseCastTicks;
	private final boolean discountable;
	private final Component title;
	
	private SpellType(int baseCastTicks, boolean discountable) {
		this.baseCastTicks = baseCastTicks;
		this.discountable = discountable;
		this.title = new TranslatableComponent("spelltype." + this.name().toLowerCase() + ".name");
	}

	public int getBaseCastTicks() {
		return baseCastTicks;
	}

	public Component getTitle() {
		return title;
	}
	
	/**
	 * Whether this type of spell can be reagent-free if the weight is <= 0
	 * @return
	 */
	boolean canFreeCast() {
		return discountable;
	}
}
