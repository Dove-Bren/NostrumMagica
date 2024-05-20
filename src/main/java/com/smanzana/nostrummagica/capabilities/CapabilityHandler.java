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
	public static final ResourceLocation CAPABILITY_SPELLCRAFTING_LOC = NostrumMagica.Loc("spellcrafting");
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		
		//if player. Or not. Should get config going. For now, if it's a player make it?
		//also need to catch death, etc
		if (event.getObject() instanceof PlayerEntity) {
			event.addCapability(CAPABILITY_MAGIC_LOC, new NostrumMagicAttributeProvider(event.getObject()));
			event.addCapability(CAPABILITY_MANARMOR_LOC, new ManaArmorAttributeProvider(event.getObject()));
			event.addCapability(CAPABILITY_SPELLCRAFTING_LOC, new SpellCraftingCapabilityProvider());
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
			
			ISpellCrafting crafting = NostrumMagica.getSpellCrafting(event.getOriginal());
			event.getPlayer().getCapability(SpellCraftingCapabilityProvider.CAPABILITY, null).orElse(null)
				.copy(crafting);
		//}
		//if (!event.getEntityPlayer().world.isRemote)
		//	NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) event.getEntityPlayer());
	}
}
