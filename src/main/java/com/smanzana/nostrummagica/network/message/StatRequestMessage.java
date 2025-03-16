package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client is requesting the stats for an entity
 * @author Skyler
 *
 */
public class StatRequestMessage {

	public static void handle(StatRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		final ServerPlayer sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			NetworkHandler.sendTo(new StatSyncMessage(att), sp);
			NetworkHandler.sendTo(new ManaArmorSyncMessage(sp, NostrumMagica.getManaArmor(sp)), sp);
			NetworkHandler.sendTo(new SpellCraftingCapabilitySyncMessage(sp, NostrumMagica.getSpellCrafting(sp)), sp);
		});
	}

	public StatRequestMessage() {
		
	}
	
	public static StatRequestMessage decode(FriendlyByteBuf buf) {
		return new StatRequestMessage();
	}

	public static void encode(StatRequestMessage msg, FriendlyByteBuf buf) {
		;
	}

}
