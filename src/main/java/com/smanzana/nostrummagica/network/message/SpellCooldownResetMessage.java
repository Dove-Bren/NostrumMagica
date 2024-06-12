package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is informing the client to discard all spell cooldowns
 * @author Skyler
 *
 */
public class SpellCooldownResetMessage {

	public static void handle(SpellCooldownResetMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().runAsync(() -> {
			NostrumMagica.instance.getSpellCooldownTracker(player.world).clearCooldowns(player);
		});
	}
		

	public SpellCooldownResetMessage() {
		;
	}
	
	public static SpellCooldownResetMessage decode(PacketBuffer buf) {
		return new SpellCooldownResetMessage();
	}

	public static void encode(SpellCooldownResetMessage msg, PacketBuffer buf) {
		;
	}

}
