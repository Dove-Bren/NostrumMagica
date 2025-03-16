package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server is informing the client of a cooldown
 * @author Skyler
 *
 */
public class SpellGlobalCooldownMessage {

	public static void handle(SpellGlobalCooldownMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		Player player = NostrumMagica.instance.proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().submit(() -> {
			NostrumMagica.instance.getSpellCooldownTracker(player.level).overrideGlobalCooldown(player, message.cooldownTicks);
		});
	}
		

	private final int cooldownTicks;
	
	public SpellGlobalCooldownMessage(int cooldownTicks) {
		this.cooldownTicks = cooldownTicks;
	}
	
	public static SpellGlobalCooldownMessage decode(FriendlyByteBuf buf) {
		return new SpellGlobalCooldownMessage(buf.readVarInt());
	}

	public static void encode(SpellGlobalCooldownMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.cooldownTicks);
	}

}
