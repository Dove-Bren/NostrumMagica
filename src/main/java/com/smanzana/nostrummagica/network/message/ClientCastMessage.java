package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCasting;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has cast a spell
 * @author Skyler
 *
 */
public class ClientCastMessage {

	public static void handle(ClientCastMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Figure out what spell they have
		// cast it if they can
		ctx.get().setPacketHandled(true);
		
		final ServerPlayer sp = ctx.get().getSender();
		
		// What spell?
		Spell spell = NostrumMagica.instance.getSpellRegistry().lookup(
				message.id
				);
		
		if (spell == null) {
			NostrumMagica.logger.warn("Could not find matching spell from client cast request");
			return;
		}
		
		boolean isScroll = message.isScroll;
		int tomeID = message.tomeId;
		
		ctx.get().enqueueWork(() -> {
			boolean success = true;
			
			// Look up tome if there's supposed to be one
			ItemStack tome = ItemStack.EMPTY;
			if (!isScroll) {
				// Find the tome this was cast from, if any
				tome = NostrumMagica.findTome(sp, tomeID);
				
				if (!tome.isEmpty() && tome.getItem() instanceof SpellTome
						&& SpellTome.getTomeID(tome) == tomeID) {
					// Casting from a tome.
					success = SpellCasting.AttemptToolCast(spell, sp, tome).succeeded;
				} else {
					NostrumMagica.logger.warn("Got cast from client with mismatched tome");
					success = false;
				}
			} else {
				success = SpellCasting.AttemptScrollCast(spell, sp).succeeded;
			}

			// Whether it failed or not, sync attributes to client.
			// if it failed because they're out of mana on the server, or don't have the right attribs, etc.
			// then we'd want to sync them. If it succeeded, their mana and xp etc. have been adjusted!
			NostrumMagica.instance.proxy.syncPlayer(sp);
			
			if (!success) {
				NostrumMagica.logger.debug("Player attempted to cast " + spell.getName() + " but failed server side checks");
			}
		});
	}

	private final int id;
	private final int tomeId;
	private final boolean isScroll;
	
	public ClientCastMessage(Spell spell, boolean scroll, int tomeID) {
		this(spell.getRegistryID(), scroll, tomeID);
	}
	
	public ClientCastMessage(int id, boolean scroll, int tomeID) {
		this.id = id;
		this.isScroll = scroll;
		this.tomeId = tomeID;
	}

	public static ClientCastMessage decode(FriendlyByteBuf buf) {
		return new ClientCastMessage(buf.readInt(), buf.readBoolean(), buf.readInt());
	}

		public static void encode(ClientCastMessage msg, FriendlyByteBuf buf) {
		buf.writeInt(msg.id);
		buf.writeBoolean(msg.isScroll);
		buf.writeInt(msg.tomeId);
	}

}
