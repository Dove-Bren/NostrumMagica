package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is signalling that some particles should be spawned
 * @author Skyler
 *
 */
public class SpawnNostrumParticleMessage {

	public static void handle(SpawnNostrumParticleMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			message.type.spawn(NostrumMagica.instance.proxy.getPlayer().getCommandSenderWorld(), message.params);
		});
	}

	private final NostrumParticles type;
	private final SpawnParams params;
	
	public SpawnNostrumParticleMessage(NostrumParticles type, SpawnParams params) {
		this.type = type;
		this.params = params;
	}

	public static SpawnNostrumParticleMessage decode(FriendlyByteBuf buf) {
		return new SpawnNostrumParticleMessage(
				buf.readEnum(NostrumParticles.class),
				SpawnParams.FromNBT(buf.readNbt())
				);
	}

	public static void encode(SpawnNostrumParticleMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.type);
		buf.writeNbt(msg.params.toNBT(null));
	}

}
