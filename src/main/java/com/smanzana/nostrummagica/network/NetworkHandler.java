package com.smanzana.nostrummagica.network;

import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.ClientCastReplyMessage;
import com.smanzana.nostrummagica.network.messages.ClientSkillUpMessage;
import com.smanzana.nostrummagica.network.messages.ClientUpdateQuestMessage;
import com.smanzana.nostrummagica.network.messages.ManaMessage;
import com.smanzana.nostrummagica.network.messages.ObeliskTeleportationRequestMessage;
import com.smanzana.nostrummagica.network.messages.ReagentBagToggleMessage;
import com.smanzana.nostrummagica.network.messages.SpellCraftMessage;
import com.smanzana.nostrummagica.network.messages.SpellDebugMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.SpellTomeIncrementMessage;
import com.smanzana.nostrummagica.network.messages.StatRequestMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;

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
		syncChannel.registerMessage(ClientSkillUpMessage.Handler.class, ClientSkillUpMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(ClientUpdateQuestMessage.Handler.class, ClientUpdateQuestMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(StatRequestMessage.Handler.class, StatRequestMessage.class, discriminator++, Side.SERVER);
	}
	
}
