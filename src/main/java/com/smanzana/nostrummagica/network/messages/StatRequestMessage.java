package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client is requesting the stats for an entity
 * @author Skyler
 *
 */
public class StatRequestMessage {

	public static void handle(StatRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			NetworkHandler.getSyncChannel().sendTo(new StatSyncMessage(att), sp);
			NetworkHandler.getSyncChannel().sendTo(new ManaArmorSyncMessage(sp, NostrumMagica.getManaArmor(sp)), sp);
		});
	}

	public StatRequestMessage() {
		
	}
	
	public static StatRequestMessage decode(PacketBuffer buf) {
		return new StatRequestMessage();
	}

	public static void encode(StatRequestMessage msg, PacketBuffer buf) {
		;
	}

}
