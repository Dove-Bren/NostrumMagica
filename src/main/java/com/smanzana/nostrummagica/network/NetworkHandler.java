package com.smanzana.nostrummagica.network;

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
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.ModifyMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskSelectMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.network.messages.PetCommandMessage;
import com.smanzana.nostrummagica.network.messages.PetCommandSettingsSyncMessage;
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
import com.smanzana.nostrummagica.network.messages.PetGUIControlMessage;
import com.smanzana.nostrummagica.network.messages.PetGUIOpenMessage;
import com.smanzana.nostrummagica.network.messages.PetGUISyncMessage;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

	private static SimpleNetworkWrapper syncChannel;
	
	private static int discriminator = 10;
	
	private static final String CHANNEL_SYNC_NAME = "nostrum_channel";
	
	
	public static SimpleNetworkWrapper getSyncChannel() {
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
		
		syncChannel = NetworkRegistry.INSTANCE.newSimpleChannel(CHANNEL_SYNC_NAME);
		
		syncChannel.registerMessage(StatSyncMessage.Handler.class, StatSyncMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(ClientCastMessage.Handler.class, ClientCastMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ClientCastReplyMessage.Handler.class, ClientCastReplyMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(SpellRequestMessage.Handler.class, SpellRequestMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpellRequestReplyMessage.Handler.class, SpellRequestReplyMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(ManaMessage.Handler.class, ManaMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(ReagentBagToggleMessage.Handler.class, ReagentBagToggleMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpellTomeIncrementMessage.Handler.class, SpellTomeIncrementMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpellCraftMessage.Handler.class, SpellCraftMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpellDebugMessage.Handler.class, SpellDebugMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(ObeliskTeleportationRequestMessage.Handler.class, ObeliskTeleportationRequestMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ObeliskSelectMessage.Handler.class, ObeliskSelectMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ClientSkillUpMessage.Handler.class, ClientSkillUpMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ClientUpdateQuestMessage.Handler.class, ClientUpdateQuestMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(StatRequestMessage.Handler.class, StatRequestMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ClientEffectRenderMessage.Handler.class, ClientEffectRenderMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(ModifyMessage.Handler.class, ModifyMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(LoreMessage.Handler.class, LoreMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(RuneBagToggleMessage.Handler.class, RuneBagToggleMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(PetGUIControlMessage.Handler.class, PetGUIControlMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(PetGUIOpenMessage.Handler.class, PetGUIOpenMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(PetGUISyncMessage.Handler.class, PetGUISyncMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(MagicEffectUpdate.Handler.class, MagicEffectUpdate.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(ClientPurchaseResearchMessage.Handler.class, ClientPurchaseResearchMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(CandleIgniteMessage.Handler.class, CandleIgniteMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(EnchantedArmorStateUpdate.Handler.class, EnchantedArmorStateUpdate.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(EnchantedArmorStateUpdate.Handler.class, EnchantedArmorStateUpdate.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ClientTomeDropSpellMessage.Handler.class, ClientTomeDropSpellMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpawnNostrumParticleMessage.Handler.class, SpawnNostrumParticleMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(BladeCastMessage.Handler.class, BladeCastMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpawnNostrumRitualEffectMessage.Handler.class, SpawnNostrumRitualEffectMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(SpawnPredefinedEffectMessage.Handler.class, SpawnPredefinedEffectMessage.class, discriminator++, Side.CLIENT);
		syncChannel.registerMessage(PetCommandMessage.Handler.class, PetCommandMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(PetCommandSettingsSyncMessage.Handler.class, PetCommandSettingsSyncMessage.class, discriminator++, Side.CLIENT);
	}
	
}
