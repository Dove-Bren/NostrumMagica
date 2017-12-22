package com.smanzana.nostrummagica.capabilities;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CapabilityHandler {

	public static final ResourceLocation CAPABILITY_LOC = new ResourceLocation(NostrumMagica.MODID, "magicattrib");
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent.Entity event) {
		//if player. Or not. Should get config going. For now, if it's a player make it?
		//also need to catch death, etc
		//http://www.planetminecraft.com/blog/forge-tutorial-capability-system/
		//TODO
		if (event.getEntity() instanceof EntityPlayer) {
			//attach that shizz
			event.addCapability(CAPABILITY_LOC, new AttributeProvider());
		}
	}
}
