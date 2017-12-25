package com.smanzana.nostrummagica.network;

import com.smanzana.nostrummagica.network.messages.ClientCastMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestMessage;
import com.smanzana.nostrummagica.network.messages.SpellRequestReplyMessage;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;

import net.minecraftforge.fml.common.network.NetworkRegistry;
import net.minecraftforge.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import net.minecraftforge.fml.relauncher.Side;

public class NetworkHandler {

	private static SimpleNetworkWrapper syncChannel;
	
	private static int discriminator = 10;
	
	private static final String CHANNEL_SYNC_NAME = "nostrummagica_syncchannel";
	
	
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
		syncChannel.registerMessage(SpellRequestMessage.Handler.class, SpellRequestMessage.class, discriminator++, Side.SERVER);
		syncChannel.registerMessage(SpellRequestReplyMessage.Handler.class, SpellRequestReplyMessage.class, discriminator++, Side.SERVER);
	}
	
}
