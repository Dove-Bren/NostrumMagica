package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is refreshing client's view on a player's mana
 * @author Skyler
 *
 */
public class ManaMessage {

	public static void handle(ManaMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		NostrumMagica.instance.proxy.applyOverride();
		
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		
		if (player == null) {
			// Haven't finished loading. Just drop it
			return;
		}
		
		Minecraft.getInstance().runAsync(() -> {
			PlayerEntity realPlayer = player.world.getPlayerByUuid(message.uuid);
		
			if (realPlayer == null) {
				// Not in this world. Who cares
				return;
			}
			
			INostrumMagic att = NostrumMagica.getMagicWrapper(realPlayer);
			// Regardless of success, server has synced mana with us.
			
			if (att != null)
				att.setMana(message.mana);
		});
		
		// Success or nah?
	}
		

	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final UUID uuid;
	private final int mana;
	
	public ManaMessage(PlayerEntity player, int mana) {
		this(player.getUniqueID(), mana);
	}
	
	public ManaMessage(UUID uuid, int mana) {
		this.uuid = uuid;
		this.mana = mana;
	}

	public static ManaMessage decode(PacketBuffer buf) {
		return new ManaMessage(buf.readUniqueId(), buf.readVarInt());
	}

	public static void encode(ManaMessage msg, PacketBuffer buf) {
		buf.writeUniqueId(msg.uuid);
		buf.writeVarInt(msg.mana);
	}

}
