package com.smanzana.nostrummagica.capabilities;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class CapabilityHandler {

	private static final ResourceLocation CAPABILITY_MAGIC_LOC = new ResourceLocation(NostrumMagica.MODID, "magicattrib");
	private static final ResourceLocation CAPABILITY_MANARMOR_LOC = new ResourceLocation(NostrumMagica.MODID, "manaarmorattrib");
	private static final ResourceLocation CAPABILITY_SPELLCRAFTING_LOC = NostrumMagica.Loc("spellcrafting");
	private static final ResourceLocation CAPABILITY_BONUSJUMP_LOC = NostrumMagica.Loc("bonusjump");
	
	public static final Capability<INostrumMagic> CAPABILITY_MAGIC = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<IManaArmor> CAPABILITY_MANAARMOR = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<ISpellCrafting> CAPABILITY_SPELLCRAFTING = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<IBonusJumpCapability> CAPABILITY_BONUSJUMP = CapabilityManager.get(new CapabilityToken<>(){});
	public static final Capability<INostrumMana> CAPABILITY_MANA = CapabilityManager.get(new CapabilityToken<>(){});
	
	public CapabilityHandler() {
		MinecraftForge.EVENT_BUS.register(this);
	}
	
	@SubscribeEvent
	public void attachCapability(AttachCapabilitiesEvent<Entity> event) {
		
		// Automatically add things for players. Otherwise, let mod devs more natively support the capabilities.
		if (event.getObject() instanceof Player) {
			Player player = (Player) event.getObject();
			event.addCapability(CAPABILITY_MAGIC_LOC, new NostrumMagicProvider(new NostrumMagic(player)));
			event.addCapability(CAPABILITY_MANARMOR_LOC, new AutoCapabilityProvider<>(CAPABILITY_MANAARMOR, new ManaArmor(player)));
			event.addCapability(CAPABILITY_SPELLCRAFTING_LOC, new AutoCapabilityProvider<>(CAPABILITY_SPELLCRAFTING, new SpellCraftingCapability()));
			event.addCapability(CAPABILITY_BONUSJUMP_LOC, new AutoCapabilityProvider<>(CAPABILITY_BONUSJUMP, new BonusJumpCapability()));
		}
	}
	
	@SubscribeEvent
	public void onClone(PlayerEvent.Clone event) {
		if (event.isWasDeath()) {
			event.getOriginal().reviveCaps();
			INostrumMagic cap = NostrumMagica.getMagicWrapper(event.getOriginal());
			event.getPlayer().getCapability(CAPABILITY_MAGIC, null).orElse(null)
				.copy(cap);
			
			IManaArmor armor = NostrumMagica.getManaArmor(event.getOriginal());
			event.getPlayer().getCapability(CAPABILITY_MANAARMOR, null).orElse(null)
				.copy(armor);
			
			ISpellCrafting crafting = NostrumMagica.getSpellCrafting(event.getOriginal());
			event.getPlayer().getCapability(CAPABILITY_SPELLCRAFTING, null).orElse(null)
				.copy(crafting);
			
			IBonusJumpCapability jump = NostrumMagica.getBonusJump(event.getOriginal());
			event.getPlayer().getCapability(CAPABILITY_BONUSJUMP, null).orElse(null)
				.copy(jump);
			event.getOriginal().invalidateCaps();
		}
		//if (!event.getEntityPlayer().world.isRemote)
		//	NostrumMagica.instance.proxy.syncPlayer((ServerPlayerEntity) event.getEntityPlayer());
	}
}
