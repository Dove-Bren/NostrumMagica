package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client is informing server that it is charging a spell
 * @author Skyler
 *
 */
public class SpellChargeClientUpdateMessage {

	public static void handle(SpellChargeClientUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		final Player player = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			NostrumMagica.instance.getSpellChargeTracker().overrideServerCharge(player.getUUID(), message.charge);
		});
	}
		

	private final @Nullable SpellCharge charge;
	
	public SpellChargeClientUpdateMessage(@Nullable SpellCharge charge) {
		this.charge = charge;
	}
	
	public static SpellChargeClientUpdateMessage decode(FriendlyByteBuf buf) {
		return new SpellChargeClientUpdateMessage(buf.readBoolean() ? SpellCharge.FromNBT(buf.readNbt()) : null);
	}

	public static void encode(SpellChargeClientUpdateMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.charge != null);
		if (msg.charge != null) {
			buf.writeNbt(msg.charge.toNBT());
		}
	}

}
