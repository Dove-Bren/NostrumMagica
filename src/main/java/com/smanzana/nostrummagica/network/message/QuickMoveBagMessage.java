package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ReagentAndRuneTransfer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has toggled vacuum setting on their rune bag
 * @author Skyler
 *
 */
public class QuickMoveBagMessage {

	public static void handle(QuickMoveBagMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ServerPlayer sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(()-> {
			if (sp.containerMenu == null || sp.containerMenu.containerId != message.containerID) {
				NostrumMagica.logger.error("Recieved request to transfer reagents and runes, but for a container that isn't open (anymore?)");
			} else if (!ReagentAndRuneTransfer.ShouldAddTo(sp, sp.containerMenu)) {
				NostrumMagica.logger.error("Recieved request to transfer reagents and runes, but open container doesn't support it");
			} else {
				ReagentAndRuneTransfer.ProcessContainerItems(sp, sp.containerMenu);
			}
		});
	}
	
	private final int containerID;

	public QuickMoveBagMessage(int containerID) {
		this.containerID = containerID;
	}
	
	public QuickMoveBagMessage(AbstractContainerMenu container) {
		this(container.containerId);
	}
	public static QuickMoveBagMessage decode(FriendlyByteBuf buf) {
		return new QuickMoveBagMessage(buf.readVarInt());
	}

	public static void encode(QuickMoveBagMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.containerID);
	}

}
