package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * A spell was cast and the server has generated the debug information
 * for the client, if it wants it.
 * @author Skyler
 *
 */
public class SpellDebugMessage {

	public static void handle(SpellDebugMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		if (!ModConfig.config.spellDebug()) {
			return;
		}
		
		Minecraft.getInstance().submit(() -> {
			NostrumMagica.instance.proxy.getPlayer().sendMessage(message.comp, Util.NIL_UUID);
		});
	}

	private final Component comp;
	
	public SpellDebugMessage(Component comp) {
		this.comp = comp;
	}

	public static SpellDebugMessage decode(FriendlyByteBuf buf) {
		return new SpellDebugMessage(buf.readComponent());
	}

	public static void encode(SpellDebugMessage msg, FriendlyByteBuf buf) {
		buf.writeComponent(msg.comp);
	}

}
