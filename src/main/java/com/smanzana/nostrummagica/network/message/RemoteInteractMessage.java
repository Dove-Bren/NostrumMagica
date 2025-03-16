package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.network.NetworkHandler;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;
import net.minecraftforge.fmllegacy.server.ServerLifecycleHooks;

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
	
	protected static void handleClientCheck(ServerPlayer sender, RemoteInteractMessage message) {
		// On the server, check any conditions. Namely, make sure block is loaded
		final Level world = ServerLifecycleHooks.getCurrentServer().getLevel(message.dimension);
		final boolean success = world != null && NostrumMagica.isBlockLoaded(world, message.pos);
		
		NetworkHandler.sendTo(new RemoteInteractMessage(Phase.SERVER_CHECK_RESPONSE, message.dimension, message.pos, message.hand, success), sender);
	}
	
	protected static void handleServerCheck(RemoteInteractMessage message) {
		// Server responded. Make sure it agrees to proceed
		if (message.success) {
			Player player = NostrumMagica.instance.proxy.getPlayer();
			if (NostrumMagica.instance.proxy.attemptPlayerInteract(player, player.level, message.pos, message.hand, makeFakeHit(message.pos))) {
				NetworkHandler.sendToServer(new RemoteInteractMessage(Phase.CLIENT_COMMIT, message.dimension, message.pos, message.hand, true));
			}
		} else {
			NostrumMagica.logger.warn("Failed server-side interaction check");
		}
	}
	
	protected static void handleClientCommit(ServerPlayer sender, RemoteInteractMessage message) {
		final Level world = ServerLifecycleHooks.getCurrentServer().getLevel(message.dimension);
		if (world != null && NostrumMagica.isBlockLoaded(world, message.pos)) {
			NostrumMagica.instance.proxy.attemptPlayerInteract(sender, world, message.pos, message.hand, makeFakeHit(message.pos));
		}
	}
	
	protected static final BlockHitResult makeFakeHit(BlockPos pos) {
		return new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, true);
	}

	private final Phase phase;
	private final ResourceKey<Level> dimension;
	private final BlockPos pos;
	private final InteractionHand hand;
	private final boolean success;
	
	private RemoteInteractMessage(Phase phase, ResourceKey<Level> dimension, BlockPos pos, InteractionHand hand, boolean success) {
		this.phase = phase;
		this.dimension = dimension;
		this.pos = pos;
		this.hand = hand;
		this.success = success;
	}
	
	public RemoteInteractMessage(ResourceKey<Level> dimension, BlockPos pos, InteractionHand hand) {
		// Start new volley
		this(Phase.CLIENT_CHECK, dimension, pos, hand, true);
	}

	public static RemoteInteractMessage decode(FriendlyByteBuf buf) {
		return new RemoteInteractMessage(
				buf.readEnum(Phase.class),
				ResourceKey.create(Registry.DIMENSION_REGISTRY, buf.readResourceLocation()),
				buf.readBlockPos(),
				buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND,
				buf.readBoolean()
				);
	}

	public static void encode(RemoteInteractMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.phase);
		buf.writeResourceLocation(msg.dimension.location());
		buf.writeBlockPos(msg.pos);
		buf.writeBoolean(msg.hand == InteractionHand.MAIN_HAND ? true : false);
		buf.writeBoolean(msg.success);
	}

}
