package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has toggled vacuum setting on their reagent bag
 * @author Skyler
 *
 */
public class SpellTomeIncrementMessage {

	public static void handle(SpellTomeIncrementMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ctx.get().setPacketHandled(true);
		final ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			SpellTome.setIndex(NostrumMagica.getCurrentTome(sp),
					message.index);
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final int index;
	
	public SpellTomeIncrementMessage(int index) {
		this.index = index;
	}

	public static SpellTomeIncrementMessage decode(PacketBuffer buf) {
		return new SpellTomeIncrementMessage(buf.readVarInt());
	}

	public static void encode(SpellTomeIncrementMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.index);
	}

}
