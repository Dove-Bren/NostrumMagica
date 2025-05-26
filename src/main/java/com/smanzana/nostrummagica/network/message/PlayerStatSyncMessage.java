package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.stat.PlayerStats;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is providing an updated view of this client's stats
 * @author Skyler
 *
 */
public class PlayerStatSyncMessage {


	public static void handle(PlayerStatSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Note: This handler is not done on the game thread since the spell registry is thread safe.
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			final UUID myID = NostrumMagica.Proxy.getPlayer().getUUID();
			if (!myID.equals(message.id)) {
				NostrumMagica.logger.error("Received PlayerStatSync message for a different player: " + message.id);
			} else {
				NostrumMagica.instance.getPlayerStats().override(NostrumMagica.Proxy.getPlayer(), message.stats);
			}
		});
	}
		
	private final @Nonnull UUID id;
	private final @Nonnull PlayerStats stats;
	
	public PlayerStatSyncMessage(@Nonnull UUID id, @Nonnull PlayerStats stats) {
		this.id = id;
		this.stats = stats;
	}

	public static PlayerStatSyncMessage decode(FriendlyByteBuf buf) {
		UUID id = buf.readUUID();
		PlayerStats stats = PlayerStats.FromNBT(buf.readNbt());
		
		return new PlayerStatSyncMessage(id, stats);
	}

	public static void encode(PlayerStatSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeUUID(msg.id);
		buf.writeNbt(msg.stats.toNBT(null));
	}

}
