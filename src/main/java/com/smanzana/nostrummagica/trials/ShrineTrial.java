package com.smanzana.nostrummagica.trials;

import java.util.EnumMap;
import java.util.Map;

import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.entity.player.EntityPlayer;

public interface ShrineTrial {

	public static Map<EMagicElement, ShrineTrial> shrineTrials = new EnumMap<>(EMagicElement.class);
	
	public boolean canTake(EntityPlayer entityPlayer, INostrumMagic attr);
	
	public void start(EntityPlayer entityPlayer, INostrumMagic attr);
		
}
