package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ReagentBag;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has toggled vacuum setting on their reagent bag
 * @author Skyler
 *
 */
public class ReagentBagToggleMessage {


	public static void handle(ReagentBagToggleMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity sp = ctx.get().getSender();
		
		boolean main = message.isMainHand;
		boolean value = message.isOn;

		
		
		ctx.get().enqueueWork(() -> {
			ItemStack bag;
			if (main)
				bag = sp.getHeldItemMainhand();
			else
				bag = sp.getHeldItemOffhand();
			if (bag.isEmpty() || !(bag.getItem() instanceof ReagentBag)) {
				NostrumMagica.logger.warn("Reagent bag double-check position was invalid! Is the server behind?");
			}
			
			ReagentBag.setVacuumEnabled(bag, value);
		});
	}
		

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final boolean isMainHand;
	private final boolean isOn;
	
	public ReagentBagToggleMessage(boolean isMainHand, boolean isOn) {
		this.isMainHand = isMainHand;
		this.isOn = isOn;
	}

	public static ReagentBagToggleMessage decode(PacketBuffer buf) {
		return new ReagentBagToggleMessage(buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(ReagentBagToggleMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.isMainHand);
		buf.writeBoolean(msg.isOn);
	}

}
