package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client requests teleportation in a portal in the world.
 * @author Skyler
 *
 */
public class WorldPortalTeleportRequestMessage {

	public static void handle(WorldPortalTeleportRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			NostrumMagica.instance.proxy.attemptBlockTeleport(sp, message.portalPos);				
		});
	}

	private final BlockPos portalPos;
	
	/**
	 * 
	 * @param portalPos the pos of the portal
	 */
	public WorldPortalTeleportRequestMessage(BlockPos portalPos) {
		this.portalPos = portalPos;
	}

	public static WorldPortalTeleportRequestMessage decode(PacketBuffer buf) {
		return new WorldPortalTeleportRequestMessage(buf.readBlockPos());
	}

	public static void encode(WorldPortalTeleportRequestMessage msg, PacketBuffer buf) {
		buf.writeBlockPos(msg.portalPos);
	}
}
