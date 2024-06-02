package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client player's attribtes are being refreshed from server
 * @author Skyler
 *
 */
public class StatSyncMessage {

	public static void handle(StatSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		//update local attributes
		
		NostrumMagica.logger.info("Recieved Nostrum Magica sync message from server");
		
		Minecraft.getInstance().runAsync(() -> {
			INostrumMagic override = CAPABILITY.getDefaultInstance();
			CAPABILITY.getStorage().readNBT(CAPABILITY, override, null, message.tag);
			NostrumMagica.instance.proxy.receiveStatOverrides(override);
		});
		
		ctx.get().setPacketHandled(true);
	}
	
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public StatSyncMessage(CompoundNBT tag) {
		this.tag = tag == null ? new CompoundNBT() : tag;
	}
	
	public StatSyncMessage(INostrumMagic stats) {
		tag = (CompoundNBT) CAPABILITY.getStorage().writeNBT(CAPABILITY, stats, null);
	}

	public static StatSyncMessage decode(PacketBuffer buf) {
		return new StatSyncMessage(buf.readCompoundTag());
	}

	public static void encode(StatSyncMessage msg, PacketBuffer buf) {
		buf.writeCompoundTag(msg.tag);
	}

}
