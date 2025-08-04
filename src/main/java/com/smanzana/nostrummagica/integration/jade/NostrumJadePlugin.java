package com.smanzana.nostrummagica.integration.jade;

import java.util.Optional;

import com.smanzana.nostrummagica.block.dungeon.MimicBlock;

import mcp.mobius.waila.api.BlockAccessor;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.event.WailaRayTraceEvent;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;

@WailaPlugin
public class NostrumJadePlugin implements IWailaPlugin {
	
	public NostrumJadePlugin() {
		MinecraftForge.EVENT_BUS.addListener(this::onRaytrace);
	}
	
	@Override
	public void register(IWailaCommonRegistration registration) {
		//TODO register data providers and config options here
	}
	
	private static IWailaClientRegistration client;

	@Override
	public void registerClient(IWailaClientRegistration registration) {
		// Stash client handle for event handling
		client = registration;
	}
	
	public void onRaytrace(WailaRayTraceEvent event) {
		// Mostly copied from Jade how-to documentation
		var accessor = event.getAccessor();
		if (accessor instanceof BlockAccessor blockAccessor) {
			if (blockAccessor.getBlock() instanceof MimicBlock) {
				Optional<BlockState> mimicState = MimicBlock.getMirrorState(blockAccessor.getBlockState(), blockAccessor.getLevel(), blockAccessor.getPosition());
				if (mimicState.isPresent()) {
					// Override with the mimic'ed state
					accessor = client.createBlockAccessor(
							mimicState.get(),
							null,
							accessor.getLevel(),
							accessor.getPlayer(),
							null,
							blockAccessor.getHitResult(),
							accessor.isServerConnected()
						);
					event.setAccessor(accessor);
				}
			}
		}
	}
	
}
