package com.smanzana.nostrummagica.network;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.messages.BladeCastMessage;
import com.smanzana.nostrummagica.network.messages.CandleIgniteMessage;
import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.ClientCastReplyMessage;
import com.smanzana.nostrummagica.network.messages.ClientEffectRenderMessage;
import com.smanzana.nostrummagica.network.messages.ClientPurchaseResearchMessage;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage;
import com.smanzana.nostrummagica.network.messages.ClientTomeDropSpellMessage;
import com.smanzana.nostrummagica.network.messages.ClientUpdateQuestMessage;
import com.smanzana.nostrummagica.network.messages.EnchantedArmorStateUpdate;
import com.smanzana.nostrummagica.network.messages.LoreMessage;
import com.smanzana.nostrummagica.network.messages.MagicEffectUpdate;
import com.smanzana.nostrummagica.network.messages.ManaArmorSyncMessage;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.ModifyMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskSelectMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.network.messages.PetCommandMessage;
import com.smanzana.nostrummagica.network.messages.PetCommandSettingsSyncMessage;
import com.smanzana.nostrummagica.network.messages.PetGUIControlMessage;
import com.smanzana.nostrummagica.network.messages.PetGUIOpenMessage;
import com.smanzana.nostrummagica.network.messages.PetGUISyncMessage;
import com.smanzana.nostrummagica.network.messages.ReagentBagToggleMessage;
import com.smanzana.nostrummagica.network.messages.RuneBagToggleMessage;
import com.smanzana.nostrummagica.network.messages.SpawnNostrumParticleMessage;
import com.smanzana.nostrummagica.network.messages.SpawnNostrumRitualEffectMessage;
import com.smanzana.nostrummagica.network.messages.SpawnPredefinedEffectMessage;
import com.smanzana.nostrummagica.network.messages.SpellCraftMessage;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.network.messages.StatRequestMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;

public class NetworkHandler {

	private static SimpleChannel syncChannel;
	
	private static int discriminator = 10;
	
	private static final String CHANNEL_SYNC_NAME = "nostrum_channel";
	private static final String PROTOCOL = "1";
	
	public static SimpleChannel getSyncChannel() {
		getInstance();
		return syncChannel;
	}
	
	private static NetworkHandler instance;
	
	public static NetworkHandler getInstance() {
		if (instance == null)
			instance = new NetworkHandler();
		
		return instance;
	}
	
	public NetworkHandler() {
		
		syncChannel = NetworkRegistry.newSimpleChannel(new ResourceLocation(NostrumMagica.MODID, CHANNEL_SYNC_NAME),
				() -> PROTOCOL,
				PROTOCOL::equals,
				PROTOCOL::equals
				);
		
		syncChannel.registerMessage(discriminator++, StatSyncMessage.class, StatSyncMessage::encode, StatSyncMessage::decode, StatSyncMessage::handle);
		syncChannel.registerMessage(discriminator++, ClientCastMessage.class, ClientCastMessage::encode, ClientCastMessage::decode, ClientCastMessage::handle);
		syncChannel.registerMessage(discriminator++, ClientCastReplyMessage.class, ClientCastReplyMessage::encode, ClientCastReplyMessage::decode, ClientCastReplyMessage::handle);
		syncChannel.registerMessage(discriminator++, SpellRequestMessage.class, SpellRequestMessage::encode, SpellRequestMessage::decode, SpellRequestMessage::handle);
		syncChannel.registerMessage(discriminator++, SpellRequestReplyMessage.class, SpellRequestReplyMessage::encode, SpellRequestReplyMessage::decode, SpellRequestReplyMessage::handle);
		syncChannel.registerMessage(discriminator++, ManaMessage.class, ManaMessage::encode, ManaMessage::decode, ManaMessage::handle);
		syncChannel.registerMessage(discriminator++, ReagentBagToggleMessage.class, ReagentBagToggleMessage::encode, ReagentBagToggleMessage::decode, ReagentBagToggleMessage::handle);
		syncChannel.registerMessage(discriminator++, SpellTomeIncrementMessage.class, SpellTomeIncrementMessage::encode, SpellTomeIncrementMessage::decode, SpellTomeIncrementMessage::handle);
		syncChannel.registerMessage(discriminator++, SpellCraftMessage.class, SpellCraftMessage::encode, SpellCraftMessage::decode, SpellCraftMessage::handle);
		syncChannel.registerMessage(discriminator++, SpellDebugMessage.class, SpellDebugMessage::encode, SpellDebugMessage::decode, SpellDebugMessage::handle);
		syncChannel.registerMessage(discriminator++, ObeliskTeleportationRequestMessage.class, ObeliskTeleportationRequestMessage::encode, ObeliskTeleportationRequestMessage::decode, ObeliskTeleportationRequestMessage::handle);
		syncChannel.registerMessage(discriminator++, ObeliskSelectMessage.class, ObeliskSelectMessage::encode, ObeliskSelectMessage::decode, ObeliskSelectMessage::handle);
		syncChannel.registerMessage(discriminator++, ClientSkillUpMessage.class, ClientSkillUpMessage::encode, ClientSkillUpMessage::decode, ClientSkillUpMessage::handle);
		syncChannel.registerMessage(discriminator++, ClientUpdateQuestMessage.class, ClientUpdateQuestMessage::encode, ClientUpdateQuestMessage::decode, ClientUpdateQuestMessage::handle);
		syncChannel.registerMessage(discriminator++, StatRequestMessage.class, StatRequestMessage::encode, StatRequestMessage::decode, StatRequestMessage::handle);
		syncChannel.registerMessage(discriminator++, ClientEffectRenderMessage.class, ClientEffectRenderMessage::encode, ClientEffectRenderMessage::decode, ClientEffectRenderMessage::handle);
		syncChannel.registerMessage(discriminator++, ModifyMessage.class, ModifyMessage::encode, ModifyMessage::decode, ModifyMessage::handle);
		syncChannel.registerMessage(discriminator++, LoreMessage.class, LoreMessage::encode, LoreMessage::decode, LoreMessage::handle);
		syncChannel.registerMessage(discriminator++, RuneBagToggleMessage.class, RuneBagToggleMessage::encode, RuneBagToggleMessage::decode, RuneBagToggleMessage::handle);
		syncChannel.registerMessage(discriminator++, PetGUIControlMessage.class, PetGUIControlMessage::encode, PetGUIControlMessage::decode, PetGUIControlMessage::handle);
		syncChannel.registerMessage(discriminator++, PetGUIOpenMessage.class, PetGUIOpenMessage::encode, PetGUIOpenMessage::decode, PetGUIOpenMessage::handle);
		syncChannel.registerMessage(discriminator++, PetGUISyncMessage.class, PetGUISyncMessage::encode, PetGUISyncMessage::decode, PetGUISyncMessage::handle);
		syncChannel.registerMessage(discriminator++, MagicEffectUpdate.class, MagicEffectUpdate::encode, MagicEffectUpdate::decode, MagicEffectUpdate::handle);
		syncChannel.registerMessage(discriminator++, ClientPurchaseResearchMessage.class, ClientPurchaseResearchMessage::encode, ClientPurchaseResearchMessage::decode, ClientPurchaseResearchMessage::handle);
		syncChannel.registerMessage(discriminator++, CandleIgniteMessage.class, CandleIgniteMessage::encode, CandleIgniteMessage::decode, CandleIgniteMessage::handle);
		syncChannel.registerMessage(discriminator++, EnchantedArmorStateUpdate.class, EnchantedArmorStateUpdate::encode, EnchantedArmorStateUpdate::decode, EnchantedArmorStateUpdate::handle);
		//syncChannel.registerMessage(discriminator++, EnchantedArmorStateUpdate.class, EnchantedArmorStateUpdate::encode, EnchantedArmorStateUpdate::decode, EnchantedArmorStateUpdate::handle);
		syncChannel.registerMessage(discriminator++, ClientTomeDropSpellMessage.class, ClientTomeDropSpellMessage::encode, ClientTomeDropSpellMessage::decode, ClientTomeDropSpellMessage::handle);
		syncChannel.registerMessage(discriminator++, SpawnNostrumParticleMessage.class, SpawnNostrumParticleMessage::encode, SpawnNostrumParticleMessage::decode, SpawnNostrumParticleMessage::handle);
		syncChannel.registerMessage(discriminator++, BladeCastMessage.class, BladeCastMessage::encode, BladeCastMessage::decode, BladeCastMessage::handle);
		syncChannel.registerMessage(discriminator++, SpawnNostrumRitualEffectMessage.class, SpawnNostrumRitualEffectMessage::encode, SpawnNostrumRitualEffectMessage::decode, SpawnNostrumRitualEffectMessage::handle);
		syncChannel.registerMessage(discriminator++, SpawnPredefinedEffectMessage.class, SpawnPredefinedEffectMessage::encode, SpawnPredefinedEffectMessage::decode, SpawnPredefinedEffectMessage::handle);
		syncChannel.registerMessage(discriminator++, PetCommandMessage.class, PetCommandMessage::encode, PetCommandMessage::decode, PetCommandMessage::handle);
		syncChannel.registerMessage(discriminator++, PetCommandSettingsSyncMessage.class, PetCommandSettingsSyncMessage::encode, PetCommandSettingsSyncMessage::decode, PetCommandSettingsSyncMessage::handle);
		syncChannel.registerMessage(discriminator++, ManaArmorSyncMessage.class, ManaArmorSyncMessage::encode, ManaArmorSyncMessage::decode, ManaArmorSyncMessage::handle);
	}
	
}
