package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.SpellCastProperties;
import com.smanzana.nostrummagica.spell.SpellCasting;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has cast a spell that is not a server-side crafted spell
 * @author Skyler
 *
 */
public class ClientCastAdhocMessage {

	public static void handle(ClientCastAdhocMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		final ServerPlayer sp = ctx.get().getSender();
		final Spell spell = message.spell;
		final SpellCastProperties props = message.castProperties.unwrap(id -> sp.level.getEntity(id) instanceof LivingEntity living ? living : null);
		
		
		ctx.get().enqueueWork(() -> {
			boolean success = true;
			
			success = SpellCasting.AttemptToolCast(spell, sp, ItemStack.EMPTY, props).succeeded;

			// Whether it failed or not, sync attributes to client.
			// if it failed because they're out of mana on the server, or don't have the right attribs, etc.
			// then we'd want to sync them. If it succeeded, their mana and xp etc. have been adjusted!
			NostrumMagica.Proxy.syncPlayer(sp);
			
			if (!success) {
				NostrumMagica.logger.debug("Player attempted to cast " + spell.getName() + " but failed server side checks");
			}
		});
	}

	private final Spell spell;
	private final SpellCastProperties.NetworkWrapper castProperties;
	
	public ClientCastAdhocMessage(Spell spell, SpellCastProperties castProperties) {
		this(spell, castProperties.wrap());
	}
	
	public ClientCastAdhocMessage(Spell spell, SpellCastProperties.NetworkWrapper castProperties) {
		this.spell = spell;
		this.castProperties = castProperties;
	}

	public static ClientCastAdhocMessage decode(FriendlyByteBuf buf) {
		return new ClientCastAdhocMessage(
				Spell.FromNBT(buf.readNbt()),
				new SpellCastProperties.NetworkWrapper(buf.readNbt())
				);
	}

	public static void encode(ClientCastAdhocMessage msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.spell.toNBT());
		buf.writeNbt(msg.castProperties.toNBT());
	}

}
