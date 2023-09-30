package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has requested a spell be dropped from a spell tome
 * @author Skyler
 *
 */
public class ClientTomeDropSpellMessage {
	
	public static void handle(ClientTomeDropSpellMessage message, Supplier<NetworkEvent.Context> ctx) {
		final ServerPlayerEntity sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		
		ctx.get().enqueueWork(() -> {
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return;
			}
			
			ItemStack tome = NostrumMagica.findTome(sp, message.tomeID);
			if (tome == null) {
				NostrumMagica.logger.warn("Could not find tome from client message");
				return;
			}
			
			if (!SpellTome.removeSpell(tome, message.spellID)) {
				NostrumMagica.logger.warn("Player requested to remove a spell that wasn't in the tome");
			}
		});
	}

	private final int tomeID;
	private final int spellID;
	
	public ClientTomeDropSpellMessage(ItemStack spellTome, int spellID) {
		this(SpellTome.getTomeID(spellTome), spellID);
	}
	
	public ClientTomeDropSpellMessage(int tomeID, int spellID) {
		this.tomeID = tomeID;
		this.spellID = spellID;
	}

	public static ClientTomeDropSpellMessage decode(PacketBuffer buf) {
		return new ClientTomeDropSpellMessage(buf.readVarInt(), buf.readVarInt());
	}

	public static void encode(ClientTomeDropSpellMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.tomeID);
		buf.writeVarInt(msg.spellID);
	}

}
