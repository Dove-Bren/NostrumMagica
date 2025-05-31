package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.equipment.SpellTome;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

/**
 * @author Skyler
 *
 */
public class SpellTomeIncrementMessage {

	public static void handle(SpellTomeIncrementMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ctx.get().setPacketHandled(true);
		final ServerPlayer sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			SpellTome.setPageIndex(NostrumMagica.getCurrentTome(sp),
					message.index);
		});
	}

	private final int index;
	
	public SpellTomeIncrementMessage(int index) {
		this.index = index;
	}

	public static SpellTomeIncrementMessage decode(FriendlyByteBuf buf) {
		return new SpellTomeIncrementMessage(buf.readVarInt());
	}

	public static void encode(SpellTomeIncrementMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.index);
	}

}
