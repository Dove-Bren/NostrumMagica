package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.RuneBag;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client has toggled vacuum setting on their rune bag
 * @author Skyler
 *
 */
public class RuneBagToggleMessage {

	public static void handle(RuneBagToggleMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ServerPlayerEntity sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(()-> {
			ItemStack bag;
			if (message.isMainHand)
				bag = sp.getHeldItemMainhand();
			else
				bag = sp.getHeldItemOffhand();
			if (bag.isEmpty() || !(bag.getItem() instanceof RuneBag)) {
				NostrumMagica.logger.warn("Rune bag double-check position was invalid! Is the server behind?");
			}
			
			RuneBag.setVacuumEnabled(bag, message.isOn);
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private boolean isMainHand;
	private boolean isOn;
	
	public RuneBagToggleMessage(boolean isMainHand, boolean isOn) {
		this.isMainHand = isMainHand;
		this.isOn = isOn;
	}

	public static RuneBagToggleMessage decode(PacketBuffer buf) {
		return new RuneBagToggleMessage(buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(RuneBagToggleMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.isMainHand);
		buf.writeBoolean(msg.isOn);
	}

}
