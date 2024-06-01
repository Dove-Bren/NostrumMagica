package com.smanzana.nostrummagica.listeners;

import net.minecraftforge.common.MinecraftForge;

public class PlayerStatListener {

	public PlayerStatListener() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
}
