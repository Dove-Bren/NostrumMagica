package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client player's attribtes are being refreshed from server
 * @author Skyler
 *
 */
public class PetCommandSettingsSyncMessage {

	public static void handle(PetCommandSettingsSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().runAsync(() -> {
			NostrumMagica.instance.getPetCommandManager().overrideClientSettings(message.data);
		});
	}
		
	private final CompoundNBT data;
	
	public PetCommandSettingsSyncMessage(CompoundNBT nbt) {
		data = nbt;
	}

	public static PetCommandSettingsSyncMessage decode(PacketBuffer buf) {
		return new PetCommandSettingsSyncMessage(buf.readCompoundTag());
	}

	public static void encode(PetCommandSettingsSyncMessage msg, PacketBuffer buf) {
		buf.writeCompoundTag(msg.data);
	}

}
