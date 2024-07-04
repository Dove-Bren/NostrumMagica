package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.world.dungeon.DungeonRecord;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is providing an update to a player's dungeon status
 * @author Skyler
 *
 */
public class DungeonTrackerUpdateMessage {


	public static void handle(DungeonTrackerUpdateMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			final PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			final UUID myID = player.getUniqueID();
			if (!myID.equals(message.id)) {
				NostrumMagica.logger.error("Received DungeonTrackerUpdateMessage message for a different player: " + message.id);
			} else {
				NostrumMagica.instance.getDungeonTracker().overrideClientDungeon(player, message.record);
			}
		});
	}
		
	private final @Nonnull UUID id;
	private final @Nullable DungeonRecord record;
	
	public DungeonTrackerUpdateMessage(@Nonnull UUID id, @Nullable DungeonRecord record) {
		this.id = id;
		this.record = record;
	}

	public static DungeonTrackerUpdateMessage decode(PacketBuffer buf) {
		UUID id = buf.readUniqueId();
		DungeonRecord record = null;
		if (buf.readBoolean()) {
			record = DungeonRecord.FromNBT(buf.readCompoundTag());
		}
		
		return new DungeonTrackerUpdateMessage(id, record);
	}

	public static void encode(DungeonTrackerUpdateMessage msg, PacketBuffer buf) {
		buf.writeUniqueId(msg.id);
		buf.writeBoolean(msg.record != null);
		if (msg.record != null) {
			buf.writeCompoundTag(msg.record.toNBT());
		}		
	}

}
