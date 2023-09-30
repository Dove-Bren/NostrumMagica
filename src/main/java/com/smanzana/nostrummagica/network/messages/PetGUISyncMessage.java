package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;

import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is sending some syncing data to the client GUI
 * @author Skyler
 *
 */
public class PetGUISyncMessage {

	public static void handle(PetGUISyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get ID
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().runAsync(() -> {
			PetGUI.updateClientContainer(message.data);
		});
	}

	private final CompoundNBT data;
	
	public PetGUISyncMessage(CompoundNBT data) {
		this.data = data;
	}

	public static PetGUISyncMessage decode(PacketBuffer buf) {
		return new PetGUISyncMessage(buf.readCompoundTag());
	}

	public static void encode(PetGUISyncMessage msg, PacketBuffer buf) {
		buf.writeCompoundTag(msg.data);
	}

}
