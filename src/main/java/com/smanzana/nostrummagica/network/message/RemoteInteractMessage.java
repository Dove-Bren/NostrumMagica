package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.server.ServerLifecycleHooks;

/**
 * Message sent back and forth between client and server to negotiate doing a non-standard player interact with
 * the specified location.
 * @author Skyler
 *
 */
public class RemoteInteractMessage {
	
	protected static enum Phase {
		CLIENT_CHECK, // Client's initial checks have passed and is requesting that the server get ready to do the interact
		SERVER_CHECK_RESPONSE, // Server has responded, possibly agreeing or rejecting the attempt
		CLIENT_COMMIT // Client has committed the remote interaction on it's side
	}

	public static void handle(RemoteInteractMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			switch (message.phase) {
			case CLIENT_CHECK:
				handleClientCheck(ctx.get().getSender(), message);
				break;
			case SERVER_CHECK_RESPONSE:
				handleServerCheck(message);
				break;
			case CLIENT_COMMIT:
				handleClientCommit(ctx.get().getSender(), message);
				break;
			}
		});
	}
	
	protected static void handleClientCheck(ServerPlayerEntity sender, RemoteInteractMessage message) {
		// On the server, check any conditions. Namely, make sure block is loaded
		final World world = ServerLifecycleHooks.getCurrentServer().getWorld(message.dimension);
		final boolean success = world != null && NostrumMagica.isBlockLoaded(world, message.pos);
		
		NetworkHandler.sendTo(new RemoteInteractMessage(Phase.SERVER_CHECK_RESPONSE, message.dimension, message.pos, message.hand, success), sender);
	}
	
	protected static void handleServerCheck(RemoteInteractMessage message) {
		// Server responded. Make sure it agrees to proceed
		if (message.success) {
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			if (NostrumMagica.instance.proxy.attemptPlayerInteract(player, player.world, message.pos, message.hand, makeFakeHit(message.pos))) {
				NetworkHandler.sendToServer(new RemoteInteractMessage(Phase.CLIENT_COMMIT, message.dimension, message.pos, message.hand, true));
			}
		} else {
			NostrumMagica.logger.warn("Failed server-side interaction check");
		}
	}
	
	protected static void handleClientCommit(ServerPlayerEntity sender, RemoteInteractMessage message) {
		final World world = ServerLifecycleHooks.getCurrentServer().getWorld(message.dimension);
		if (world != null && NostrumMagica.isBlockLoaded(world, message.pos)) {
			NostrumMagica.instance.proxy.attemptPlayerInteract(sender, world, message.pos, message.hand, makeFakeHit(message.pos));
		}
	}
	
	protected static final BlockRayTraceResult makeFakeHit(BlockPos pos) {
		return new BlockRayTraceResult(Vector3d.copyCentered(pos), Direction.UP, pos, true);
	}

	private final Phase phase;
	private final RegistryKey<World> dimension;
	private final BlockPos pos;
	private final Hand hand;
	private final boolean success;
	
	private RemoteInteractMessage(Phase phase, RegistryKey<World> dimension, BlockPos pos, Hand hand, boolean success) {
		this.phase = phase;
		this.dimension = dimension;
		this.pos = pos;
		this.hand = hand;
		this.success = success;
	}
	
	public RemoteInteractMessage(RegistryKey<World> dimension, BlockPos pos, Hand hand) {
		// Start new volley
		this(Phase.CLIENT_CHECK, dimension, pos, hand, true);
	}

	public static RemoteInteractMessage decode(PacketBuffer buf) {
		return new RemoteInteractMessage(
				buf.readEnumValue(Phase.class),
				RegistryKey.getOrCreateKey(Registry.WORLD_KEY, buf.readResourceLocation()),
				buf.readBlockPos(),
				buf.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND,
				buf.readBoolean()
				);
	}

	public static void encode(RemoteInteractMessage msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.phase);
		buf.writeResourceLocation(msg.dimension.getLocation());
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.hand == Hand.MAIN_HAND ? true : false);
		buf.writeBoolean(msg.success);
	}

}
