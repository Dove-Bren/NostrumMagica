package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.item.equipment.RuneBag;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client has toggled vacuum setting on their rune bag
 * @author Skyler
 *
 */
public class RuneBagToggleMessage {

	public static void handle(RuneBagToggleMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Is it on?
		ServerPlayer sp = ctx.get().getSender();
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(()-> {
			ItemStack bag;
			if (message.isMainHand)
				bag = sp.getMainHandItem();
			else
				bag = sp.getOffhandItem();
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

	public static RuneBagToggleMessage decode(FriendlyByteBuf buf) {
		return new RuneBagToggleMessage(buf.readBoolean(), buf.readBoolean());
	}

	public static void encode(RuneBagToggleMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.isMainHand);
		buf.writeBoolean(msg.isOn);
	}

}
