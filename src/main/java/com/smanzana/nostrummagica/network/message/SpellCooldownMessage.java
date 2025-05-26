package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.RegisteredSpell;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is informing the client of a cooldown
 * @author Skyler
 *
 */
public class SpellCooldownMessage {

	public static void handle(SpellCooldownMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		Player player = NostrumMagica.Proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().submit(() -> {
			if (message.spell == null) {
				// Spell not found, so ignore
				return;
			}
			
			NostrumMagica.instance.getSpellCooldownTracker(player.level).overrideSpellCooldown(player, message.spell, message.cooldownTicks);
		});
	}
		

	private final RegisteredSpell spell;
	private final int cooldownTicks;
	
	public SpellCooldownMessage(RegisteredSpell spell, int cooldownTicks) {
		this.spell = spell;
		this.cooldownTicks = cooldownTicks;
	}
	
	public static SpellCooldownMessage decode(FriendlyByteBuf buf) {
		return new SpellCooldownMessage(NostrumMagica.instance.getSpellRegistry().lookup(buf.readVarInt()), buf.readVarInt());
	}

	public static void encode(SpellCooldownMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.spell.getRegistryID());
		buf.writeVarInt(msg.cooldownTicks);
	}

}
