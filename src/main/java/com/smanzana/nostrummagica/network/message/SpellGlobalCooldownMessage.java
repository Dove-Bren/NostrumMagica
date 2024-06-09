package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is informing the client of a cooldown
 * @author Skyler
 *
 */
public class SpellGlobalCooldownMessage {

	public static void handle(SpellGlobalCooldownMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().runAsync(() -> {
			NostrumMagica.instance.getSpellCooldownTracker(player.world).overrideGlobalCooldown(player, message.cooldownTicks);
		});
	}
		

	private final int cooldownTicks;
	
	public SpellGlobalCooldownMessage(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
	}
	
	public static SpellGlobalCooldownMessage decode(PacketBuffer buf) {
		return new SpellGlobalCooldownMessage(buf.readVarInt());
	}

	public static void encode(SpellGlobalCooldownMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.cooldownTicks);
	}

}
