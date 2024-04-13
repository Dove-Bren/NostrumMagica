package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;

import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

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
		
		Minecraft.getInstance().runAsync(() -> {
			NostrumMagica.instance.proxy.getPlayer().sendMessage(message.comp, Util.DUMMY_UUID);
		});
	}

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final ITextComponent comp;
	
	public SpellDebugMessage(ITextComponent comp) {
		this.comp = comp;
	}

	public static SpellDebugMessage decode(PacketBuffer buf) {
		return new SpellDebugMessage(buf.readTextComponent());
	}

	public static void encode(SpellDebugMessage msg, PacketBuffer buf) {
		buf.writeTextComponent(msg.comp);
	}

}
