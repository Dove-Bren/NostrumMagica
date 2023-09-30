package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.items.WarlockSword;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has cast a spell
 * @author Skyler
 *
 */
public class BladeCastMessage {

	public BladeCastMessage() {
		;
	}

	public static void handle(BladeCastMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Attempt blade cast
		// TODO make this more generic with an interface or something and move trying to find the hand
		// into a helper on the interface instead of in warlock blade
		final ServerPlayerEntity sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			WarlockSword.DoCast(sp);
		});
	}
	
	public static BladeCastMessage decode(PacketBuffer buf) {
		return new BladeCastMessage();
	}

	public static void encode(BladeCastMessage msg, PacketBuffer buf) {
		;
	}

}
