package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;

import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has requested a research item be purchased
 * @author Skyler
 *
 */
public class ClientPurchaseResearchMessage {
	
	public static void handle(ClientPurchaseResearchMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayer sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			if (NostrumMagica.canPurchaseResearch(sp, message.research) && att.getResearchPoints() > 0) {
				att.takeResearchPoint();
				NostrumResearch.unlockResearch(sp, message.research);
			}

			 NetworkHandler.sendTo(new StatSyncMessage(att), sp);
		});
	}

	private final NostrumResearch research;
	
	public ClientPurchaseResearchMessage(NostrumResearch research) {
		this.research = research;
	}

	public static ClientPurchaseResearchMessage decode(FriendlyByteBuf buf) {
		final String researchKey = buf.readUtf(32767);
		NostrumResearch research = NostrumResearch.lookup(researchKey);
		if (research == null) {
			throw new DecoderException("Failed to find nostrum research for " + researchKey);
		}
		
		return new ClientPurchaseResearchMessage(research);
	}

	public static void encode(ClientPurchaseResearchMessage msg, FriendlyByteBuf buf) {
		buf.writeUtf(msg.research.getKey());
	}

}
