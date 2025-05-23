package com.smanzana.nostrummagica.trial;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class TrialEarth extends WorldTrial {

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
		if (!e.getState().is(Tags.Blocks.OBSIDIAN))
			return;
		
		if (!e.getPlayer().getMainHandItem().isEmpty())
			return;
		
		this.complete(e.getPlayer());
	}
	
}
