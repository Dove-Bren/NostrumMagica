package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client player's attribtes are being refreshed from server
 * @author Skyler
 *
 */
public class StatSyncMessage {

	public static void handle(StatSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		//update local attributes
		
		NostrumMagica.logger.info("Recieved Nostrum Magica sync message from server");
		
		Minecraft.getInstance().submit(() -> {
			INostrumMagic override = new NostrumMagic(null);
			override.deserializeNBT(message.tag);
			NostrumMagica.instance.proxy.receiveStatOverrides(override);
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	protected CompoundTag tag;
	
	public StatSyncMessage(CompoundTag tag) {
		this.tag = tag == null ? new CompoundTag() : tag;
	}
	
	public StatSyncMessage(INostrumMagic stats) {
		tag = stats.serializeNBT();
	}

	public static StatSyncMessage decode(FriendlyByteBuf buf) {
		return new StatSyncMessage(buf.readNbt());
	}

	public static void encode(StatSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.tag);
	}

}
