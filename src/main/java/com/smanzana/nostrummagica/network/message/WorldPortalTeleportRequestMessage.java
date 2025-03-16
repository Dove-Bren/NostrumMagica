package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Client requests teleportation in a portal in the world.
 * @author Skyler
 *
 */
public class WorldPortalTeleportRequestMessage {

	public static void handle(WorldPortalTeleportRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ServerPlayer sp = ctx.get().getSender();
		
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

	public static WorldPortalTeleportRequestMessage decode(FriendlyByteBuf buf) {
		return new WorldPortalTeleportRequestMessage(buf.readBlockPos());
	}

	public static void encode(WorldPortalTeleportRequestMessage msg, FriendlyByteBuf buf) {
		buf.writeBlockPos(msg.portalPos);
	}
}
