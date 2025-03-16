package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.item.equipment.WarlockSword;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
		final ServerPlayer sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			WarlockSword.DoCast(sp);
		});
	}
	
	public static BladeCastMessage decode(FriendlyByteBuf buf) {
		return new BladeCastMessage();
	}

	public static void encode(BladeCastMessage msg, FriendlyByteBuf buf) {
		;
	}

}
