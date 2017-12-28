package com.smanzana.nostrummagica.potions;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.potion.Potion;
import net.minecraft.util.ResourceLocation;

public class MagicResistPotion extends Potion {

	private static final ResourceLocation Resource = new ResourceLocation(
			NostrumMagica.MODID, "potions-magres");
	
	private static MagicResistPotion instance;
	public static MagicResistPotion instance() {
		if (instance == null)
			instance = new MagicResistPotion();
		
		return instance;
	}
	
	private MagicResistPotion() {
		super(false, 0xFFA5359A);
		
		NostrumMagica.registerPotion(this, Resource);
	}
	
	public boolean isReady(int duration, int amp) {
		return duration > 0; // Every tick
	}
	
}
