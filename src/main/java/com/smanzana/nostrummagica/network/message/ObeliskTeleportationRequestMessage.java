package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumObelisk;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tile.NostrumObeliskEntity;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Client requests teleportation to an obelisk
 * @author Skyler
 *
 */
public class ObeliskTeleportationRequestMessage {

	public static void handle(ObeliskTeleportationRequestMessage message, Supplier<NetworkEvent.Context> ctx) {
		// Get from and to
		// Validate to is an obelisk
		// If from is there, validate from
		// Then telport to to + 1
		ctx.get().setPacketHandled(true);
		ServerPlayerEntity sp = ctx.get().getSender();
		
		ctx.get().enqueueWork(() -> {
			serverDoRequest(sp.world, sp, message.from, message.to);				
		});
	}

	private final @Nullable BlockPos from;
	private final BlockPos to;
	
	/**
	 * 
	 * @param from If null, assumes a ritual with only a to instead of obelisk tele
	 * @param to
	 */
	public ObeliskTeleportationRequestMessage(BlockPos from, BlockPos to) {
		this.from = from;
		this.to = to;
	}

	public static ObeliskTeleportationRequestMessage decode(PacketBuffer buf) {
		return new ObeliskTeleportationRequestMessage(
				buf.readBoolean() ? buf.readBlockPos() : null,
						buf.readBlockPos()
				);
	}

	public static void encode(ObeliskTeleportationRequestMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.from != null);
		if (msg.from != null) {
			buf.writeBlockPos(msg.from);
		}
		buf.writeBlockPos(msg.to);
	}

	public static boolean serverDoRequest(World world, PlayerEntity player, BlockPos from, BlockPos to) {
		INostrumMagic att = NostrumMagica.getMagicWrapper(player);
		
		if (att == null || !att.isUnlocked()) {
			// drop it on the floor
			return false;
		}
		
		// Validate obelisks
		if (NostrumObelisk.isValidTarget(world, from, to)) {
			player.sendMessage(new TranslationTextComponent("info.obelisk.dne"), Util.DUMMY_UUID);
			return false;
		}
		
		// If we were given from, deduct aether
		if (from != null) {
			TileEntity te = world.getTileEntity(from);
			if (te == null || !(te instanceof NostrumObeliskEntity)) {
				NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
				player.sendMessage(new TranslationTextComponent("info.obelisk.dne"), Util.DUMMY_UUID);
				return false;
			}
			
			NostrumObeliskEntity obelisk = (NostrumObeliskEntity) te;
			if (!obelisk.deductForTeleport(to)) {
				NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
				player.sendMessage(new TranslationTextComponent("info.obelisk.aetherfail"), Util.DUMMY_UUID);
				return false;
			}
		}
		
		BlockPos targ = null;
		for (BlockPos attempt : new BlockPos[]{to, to.up(), to.north(), to.north().east(), to.north().west(), to.east(), to.west(), to.south(), to.south().east(), to.south().west()}) {
			if (player.attemptTeleport(attempt.getX() + .5, attempt.getY() + 1, attempt.getZ() + .5, false)) {
				targ = attempt;
				break;
			}
		}
		if (targ != null) {
			if (from != null)
				doEffects(world, from);
				
			doEffects(world, to);
		} else {
			player.sendMessage(new TranslationTextComponent("info.obelisk.noroom"), Util.DUMMY_UUID);
		}
		
		return true;
	}
	
	private static void doEffects(World world, BlockPos pos) {
		double x = pos.getX() + .5;
		double y = pos.getY() + 1.4;
		double z = pos.getZ() + .5;
		NostrumMagicaSounds.DAMAGE_ENDER.play(world, x, y, z);
		((ServerWorld) world).addParticle(ParticleTypes.DRAGON_BREATH,
				x,
				y,
				z,
				//50, TODO did this 50 times?
				.3,
				.5,
				.3//,
				//.2
				);
	}
	
}
