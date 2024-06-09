package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is informing the client of a cooldown
 * @author Skyler
 *
 */
public class SpellCooldownMessage {

	public static void handle(SpellCooldownMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().runAsync(() -> {
			if (message.spell == null) {
				// Spell not found, so ignore
				return;
			}
			
			NostrumMagica.instance.getSpellCooldownTracker(player.world).overrideSpellCooldown(player, message.spell, message.cooldownTicks);
		});
	}
		

	private final Spell spell;
	private final int cooldownTicks;
	
	public SpellCooldownMessage(Spell spell, int cooldownTicks) {
		this.spell = spell;
		this.cooldownTicks = cooldownTicks;
	}
	
	public static SpellCooldownMessage decode(PacketBuffer buf) {
		return new SpellCooldownMessage(NostrumMagica.instance.getSpellRegistry().lookup(buf.readVarInt()), buf.readVarInt());
	}

	public static void encode(SpellCooldownMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.spell.getRegistryID());
		buf.writeVarInt(msg.cooldownTicks);
	}

}
