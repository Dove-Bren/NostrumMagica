package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server is informing the client to discard all spell cooldowns
 * @author Skyler
 *
 */
public class SpellCooldownResetMessage {

	public static void handle(SpellCooldownResetMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		Player player = NostrumMagica.instance.proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().submit(() -> {
			NostrumMagica.instance.getSpellCooldownTracker(player.level).clearCooldowns(player);
		});
	}
		

	public SpellCooldownResetMessage() {
		;
	}
	
	public static SpellCooldownResetMessage decode(FriendlyByteBuf buf) {
		return new SpellCooldownResetMessage();
	}

	public static void encode(SpellCooldownResetMessage msg, FriendlyByteBuf buf) {
		;
	}

}
