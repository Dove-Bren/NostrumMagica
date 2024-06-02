package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;

import io.netty.handler.codec.DecoderException;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has requested a quest become active or completed
 * @author Skyler
 *
 */
public class ClientUpdateQuestMessage {
	
	public static void handle(ClientUpdateQuestMessage message, Supplier<NetworkEvent.Context> ctx) {
		final ServerPlayerEntity sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			if (NostrumMagica.canTakeQuest(sp, message.quest))
				message.quest.startQuest(sp);

			 NetworkHandler.sendTo(new StatSyncMessage(att), sp);
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final NostrumQuest quest;
	
	public ClientUpdateQuestMessage(NostrumQuest quest) {
		this.quest = quest;
	}

	public static ClientUpdateQuestMessage decode(PacketBuffer buf) {
		final String id = buf.readString(32767);
		NostrumQuest quest = NostrumQuest.lookup(id);
		if (quest == null) {
			throw new DecoderException("Could not find Nostrum quest matching " + id);
		}
		return new ClientUpdateQuestMessage(quest);
	}

		public static void encode(ClientUpdateQuestMessage msg, PacketBuffer buf) {
		buf.writeString(msg.quest.getKey());
	}

}
