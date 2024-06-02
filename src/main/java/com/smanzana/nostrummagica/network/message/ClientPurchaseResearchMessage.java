package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.progression.research.NostrumResearch;

import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has requested a research item be purchased
 * @author Skyler
 *
 */
public class ClientPurchaseResearchMessage {
	
	public static void handle(ClientPurchaseResearchMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity sp = ctx.get().getSender();
		
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

	public static ClientPurchaseResearchMessage decode(PacketBuffer buf) {
		final String researchKey = buf.readString(32767);
		NostrumResearch research = NostrumResearch.lookup(researchKey);
		if (research == null) {
			throw new DecoderException("Failed to find nostrum research for " + researchKey);
		}
		
		return new ClientPurchaseResearchMessage(research);
	}

	public static void encode(ClientPurchaseResearchMessage msg, PacketBuffer buf) {
		buf.writeString(msg.research.getKey());
	}

}
