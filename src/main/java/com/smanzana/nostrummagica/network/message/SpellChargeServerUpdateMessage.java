package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellChargeTracker.SpellCharge;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is broadcasting a change to a (different) entity's spell charge
 * @author Skyler
 *
 */
public class SpellChargeServerUpdateMessage {

	public static void handle(SpellChargeServerUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		Minecraft.getInstance().submit(() -> {
			NostrumMagica.instance.getSpellChargeTracker().overrideClientCharge(message.id, message.charge);
		});
	}
		
	private final UUID id;
	private final @Nullable SpellCharge charge;
	
	public SpellChargeServerUpdateMessage(UUID id, @Nullable SpellCharge charge) {
		this.id = id;
		this.charge = charge;
	}
	
	public static SpellChargeServerUpdateMessage decode(FriendlyByteBuf buf) {
		return new SpellChargeServerUpdateMessage(buf.readUUID(), buf.readBoolean() ? SpellCharge.FromNBT(buf.readNbt()) : null);
	}

	public static void encode(SpellChargeServerUpdateMessage msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.id);
		buf.writeBoolean(msg.charge != null);
		if (msg.charge != null) {
			buf.writeNbt(msg.charge.toNBT());
		}
	}

}
