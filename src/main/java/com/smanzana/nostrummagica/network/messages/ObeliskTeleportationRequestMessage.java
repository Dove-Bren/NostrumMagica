package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.NostrumObeliskEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client requests teleportation to an obelisk
 * @author Skyler
 *
 */
public class ObeliskTeleportationRequestMessage implements IMessage {

	public static class Handler implements IMessageHandler<ObeliskTeleportationRequestMessage, ManaMessage> {

		@Override
		public ManaMessage onMessage(ObeliskTeleportationRequestMessage message, MessageContext ctx) {
			// Get from and to
			// Validate to is an obelisk
			// If from is there, validate from
			// Then telport to to + 1
			
			BlockPos from, to;
			from = null;
			if (message.tag.contains(NBT_FROM, NBT.TAG_COMPOUND)) {
				from = NBTUtil.getPosFromTag(message.tag.getCompound(NBT_FROM));
			}
			to = NBTUtil.getPosFromTag(message.tag.getCompound(NBT_TO));
			
			ServerPlayerEntity sp = ctx.getServerHandler().player;
			
			final BlockPos fromFinal = from;
			sp.getServerWorld().runAsync(() -> {
				serverDoRequest(sp.world, sp, fromFinal, to);				
			});
			
			return null;
		}
		
	}

	private static final String NBT_FROM = "from";
	private static final String NBT_TO = "to";
	//@CapabilityInject(INostrumMagic.class)
	//public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public ObeliskTeleportationRequestMessage() {
		tag = new CompoundNBT();
	}
	
	/**
	 * 
	 * @param from If null, assumes a ritual with only a to instead of obelisk tele
	 * @param to
	 */
	public ObeliskTeleportationRequestMessage(BlockPos from, BlockPos to) {
		tag = new CompoundNBT();
		
		if (from != null)
			tag.put(NBT_FROM, NBTUtil.createPosTag(from));
		tag.put(NBT_TO, NBTUtil.createPosTag(to));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

	public static boolean serverDoRequest(World world, PlayerEntity player, BlockPos from, BlockPos to) {
		INostrumMagic att = NostrumMagica.getMagicWrapper(player);
		
		if (att == null || !att.isUnlocked()) {
			// drop it on the floor
			return false;
		}
		
		// Validate obelisks
		if (NostrumObelisk.isValidTarget(world, from, to)) {
			player.sendMessage(new TranslationTextComponent("info.obelisk.dne"));
			return false;
		}
		
		// If we were given from, deduct aether
		if (from != null) {
			TileEntity te = world.getTileEntity(from);
			if (te == null || !(te instanceof NostrumObeliskEntity)) {
				NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
				player.sendMessage(new TranslationTextComponent("info.obelisk.dne"));
				return false;
			}
			
			NostrumObeliskEntity obelisk = (NostrumObeliskEntity) te;
			if (!obelisk.deductForTeleport(to)) {
				NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
				player.sendMessage(new TranslationTextComponent("info.obelisk.aetherfail"));
				return false;
			}
		}
		
		BlockPos targ = null;
		for (BlockPos attempt : new BlockPos[]{to, to.up(), to.north(), to.north().east(), to.north().west(), to.east(), to.west(), to.south(), to.south().east(), to.south().west()}) {
			if (player.attemptTeleport(attempt.getX() + .5, attempt.getY() + 1, attempt.getZ() + .5)) {
				targ = attempt;
				break;
			}
		}
		if (targ != null) {
			if (from != null)
				doEffects(world, from);
				
			doEffects(world, to);
		} else {
			player.sendMessage(new TranslationTextComponent("info.obelisk.noroom"));
		}
		
		return true;
	}
	
	private static void doEffects(World world, BlockPos pos) {
		double x = pos.getX() + .5;
		double y = pos.getY() + 1.4;
		double z = pos.getZ() + .5;
		NostrumMagicaSounds.DAMAGE_ENDER.play(world, x, y, z);
		((WorldServer) world).spawnParticle(EnumParticleTypes.DRAGON_BREATH,
				x,
				y,
				z,
				50,
				.3,
				.5,
				.3,
				.2,
				new int[0]);
	}
	
}
