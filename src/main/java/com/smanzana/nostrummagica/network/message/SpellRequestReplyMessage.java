package com.smanzana.nostrummagica.network.message;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is replying to client detail request
 * @author Skyler
 *
 */
public class SpellRequestReplyMessage {


	public static void handle(SpellRequestReplyMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Note: This handler is not done on the game thread since the spell registry is thread safe.
		ctx.get().setPacketHandled(true);
		// What spells?
		if (message.clean) {
			NostrumMagica.logger.info("Cleaning spell registry to receive server's copy");
			NostrumMagica.instance.getSpellRegistry().clear();
		}
		
		for (Spell spell : message.spells) {
			NostrumMagica.instance.getSpellRegistry().override(spell.getRegistryID(), spell);
		}
	}
		
	private final List<Spell> spells;
	private final boolean clean;
	
	public SpellRequestReplyMessage(List<Spell> spells) {
		this(spells, false);
	}
	
	public SpellRequestReplyMessage(List<Spell> spells, boolean clean) {
		this.spells = spells;
		this.clean = clean;
	}

	public static SpellRequestReplyMessage decode(PacketBuffer buf) {
		List<Spell> spells = new ArrayList<>();
		boolean clean = buf.readBoolean();
		int spellCount = buf.readVarInt();
		
		for (int i = 0; i < spellCount; i++) {
			int spellID = buf.readVarInt();
			CompoundNBT tag = buf.readNbt();
			Spell spell = Spell.fromNBT(tag, spellID);
			
			if (spell != null) {
				spells.add(spell);
			}
		}

		return new SpellRequestReplyMessage(spells, clean);
	}

	public static void encode(SpellRequestReplyMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.clean);
		buf.writeVarInt(msg.spells.size());
		
		for (Spell spell : msg.spells) {
			buf.writeVarInt(spell.getRegistryID());
			buf.writeNbt(spell.toNBT());
		}
	}

}
