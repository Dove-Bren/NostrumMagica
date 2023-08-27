package com.smanzana.nostrummagica.trials;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialEarth extends ShrineTrial {

	public TrialEarth() {
		super(EMagicElement.EARTH);
		MinecraftForge.EVENT_BUS.register(this);
	}

	@SubscribeEvent
	public void onBlockBreak(BreakEvent e) {
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(e.getPlayer());
		if (attr == null || !attr.isUnlocked())
			return;
		
		if (!attr.hasTrial(this.element))
			return;
		
		if (e.getState().getBlock() != Blocks.OBSIDIAN)
			return;
		
		if (!e.getPlayer().getHeldItemMainhand().isEmpty())
			return;
		
		this.complete(e.getPlayer());
	}
	
}
