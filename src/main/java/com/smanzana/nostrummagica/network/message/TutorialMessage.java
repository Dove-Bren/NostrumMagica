package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.listener.ClientPlayerListener;
import com.smanzana.nostrummagica.client.listener.NostrumTutorial;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

/**
 * Sent to client to instruct them to start a tutorial
 * @author Skyler
 *
 */
public class TutorialMessage {

	public static void handle(TutorialMessage message, Supplier<NetworkEvent.Context> ctx) {
		//update local attributes
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			((ClientPlayerListener) NostrumMagica.playerListener).getTutorial().setTutorial(message.tutorial);
		});
	}
	
	private final @Nonnull NostrumTutorial.Tutorial tutorial;
	
	public TutorialMessage(@Nonnull NostrumTutorial.Tutorial tutorial) {
		this.tutorial = tutorial;
	}

	public static TutorialMessage decode(FriendlyByteBuf buf) {
		return new TutorialMessage(buf.readEnum(NostrumTutorial.Tutorial.class));
	}

	public static void encode(TutorialMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.tutorial);
	}

}
