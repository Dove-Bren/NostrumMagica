package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.equipment.ReagentBag;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

/**
 * Client has toggled vacuum setting on their reagent bag
 * @author Skyler
 *
 */
public class ReagentBagToggleMessage {


	public static void handle(ReagentBagToggleMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ctx.get().setPacketHandled(true);
		ServerPlayer sp = ctx.get().getSender();
		
		boolean main = message.isMainHand;
		boolean value = message.isOn;

		
		
		ctx.get().enqueueWork(() -> {
			ItemStack bag;
			if (main)
				bag = sp.getMainHandItem();
			else
				bag = sp.getOffhandItem();
			if (bag.isEmpty() || !(bag.getItem() instanceof ReagentBag)) {
				NostrumMagica.logger.warn("Reagent bag double-check position was invalid! Is the server behind?");
			}
			
			ReagentBag.setVacuumEnabled(bag, value);
		});
	}
		

	private final boolean isMainHand;
	private final boolean isOn;
	
	public ReagentBagToggleMessage(boolean isMainHand, boolean isOn) {
		this.isMainHand = isMainHand;
		this.isOn = isOn;
	}

	public static ReagentBagToggleMessage decode(FriendlyByteBuf buf) {
		return new ReagentBagToggleMessage(buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(ReagentBagToggleMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.isMainHand);
		buf.writeBoolean(msg.isOn);
	}

}
