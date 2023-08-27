package com.smanzana.nostrummagica.capabilities;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityHandler {

	public static final ResourceLocation CAPABILITY_MAGIC_LOC = new ResourceLocation(NostrumMagica.MODID, "magicattrib");
	public static final ResourceLocation CAPABILITY_MANARMOR_LOC = new ResourceLocation(NostrumMagica.MODID, "manaarmorattrib");
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		
		//if player. Or not. Should get config going. For now, if it's a player make it?
		//also need to catch death, etc
		if (event.getObject() instanceof PlayerEntity) {
			//attach that shizz
			event.addCapability(CAPABILITY_MAGIC_LOC, new NostrumMagicAttributeProvider(event.getObject()));
			event.addCapability(CAPABILITY_MANARMOR_LOC, new ManaArmorAttributeProvider(event.getObject()));
			
			if (event.getObject().world != null && event.getObject().world.isRemote) {
				NostrumMagica.proxy.requestStats((PlayerEntity) event.getObject());
			}
		}
	}
	
	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) {
		//if (event.isWasDeath()) {
			INostrumMagic cap = NostrumMagica.getMagicWrapper(event.getOriginal());
			event.getPlayer().getCapability(NostrumMagicAttributeProvider.CAPABILITY, null).orElse(null)
				.copy(cap);
			
			IManaArmor armor = NostrumMagica.getManaArmor(event.getOriginal());
			event.getPlayer().getCapability(ManaArmorAttributeProvider.CAPABILITY, null).orElse(null)
				.copy(armor);
		//}
		//if (!event.getEntityPlayer().world.isRemote)
		//	NostrumMagica.proxy.syncPlayer((ServerPlayerEntity) event.getEntityPlayer());
	}
}
