package com.smanzana.nostrummagica.network.message;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.spell.RegisteredSpell;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client is asking server for details about a spell (or multiple)
 * @author Skyler
 *
 */
public class SpellRequestMessage {

	public static void handle(SpellRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		
		// Note: This message handler is not on main thread, but the spell registry is threadsafe.
		ctx.get().setPacketHandled(true);
		List<RegisteredSpell> spells = new LinkedList<>();
		RegisteredSpell spell;
		for (int id : message.ids) {
			spell = NostrumMagica.instance.getSpellRegistry().lookup(id);
			if (spell != null)
				spells.add(spell);
			else
				System.out.println("Couldn't match spell for id " + id);
		}
		
		if (spells.isEmpty()) {
			System.out.println("Failed to find any spells at all!");
			return;
		}

		System.out.println("Sending reply");
		NetworkHandler.sendTo(new SpellRequestReplyMessage(spells), ctx.get().getSender());
	}

	private final int[] ids;
	
	public SpellRequestMessage(int ids[]) {
		this.ids = ids;
	}

	public static SpellRequestMessage decode(FriendlyByteBuf buf) {
		return new SpellRequestMessage(buf.readVarIntArray());
	}

	public static void encode(SpellRequestMessage msg, FriendlyByteBuf buf) {
		buf.writeVarIntArray(msg.ids);
	}

}
