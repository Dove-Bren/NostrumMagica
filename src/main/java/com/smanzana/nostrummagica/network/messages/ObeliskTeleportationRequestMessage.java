package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.blocks.NostrumObelisk;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import io.netty.buffer.ByteBuf;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentTranslation;
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
			if (message.tag.hasKey(NBT_FROM, NBT.TAG_COMPOUND)) {
				from = NBTUtil.getPosFromTag(message.tag.getCompoundTag(NBT_FROM));
			}
			to = NBTUtil.getPosFromTag(message.tag.getCompoundTag(NBT_TO));
			
			EntityPlayer sp = ctx.getServerHandler().playerEntity;
			
			serverDoRequest(sp.worldObj, sp, from, to);
			
			return null;
		}
		
	}

	private static final String NBT_FROM = "from";
	private static final String NBT_TO = "to";
	//@CapabilityInject(INostrumMagic.class)
	//public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ObeliskTeleportationRequestMessage() {
		tag = new NBTTagCompound();
	}
	
	/**
	 * 
	 * @param from If null, assumes a ritual with only a to instead of obelisk tele
	 * @param to
	 */
	public ObeliskTeleportationRequestMessage(BlockPos from, BlockPos to) {
		tag = new NBTTagCompound();
		
		if (from != null)
			tag.setTag(NBT_FROM, NBTUtil.createPosTag(from));
		tag.setTag(NBT_TO, NBTUtil.createPosTag(to));
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

	public static boolean serverDoRequest(World world, EntityPlayer player, BlockPos from, BlockPos to) {
		INostrumMagic att = NostrumMagica.getMagicWrapper(player);
		
		if (att == null || !att.isUnlocked()) {
			// drop it on the floor
			return false;
		}
		
		// Verify other obelisk
		IBlockState state = world.getBlockState(to);
		if (state == null || !(state.getBlock() instanceof NostrumObelisk)
				|| !NostrumObelisk.blockIsMaster(state)) {
			player.addChatComponentMessage(new TextComponentTranslation("info.obelisk.dne"));
			return false;
		}
		
		// If we were given from, make sure that's valid too
		if (from != null) {
			state = world.getBlockState(from);
			if (state == null || !(state.getBlock() instanceof NostrumObelisk)
					|| !NostrumObelisk.blockIsMaster(state)
					|| !NostrumObelisk.isValidTarget(world, from, to)) {
				NostrumMagica.logger.error("Something went wrong! Source obelisk does not seem to exist or have the provided target obelisk...");
				player.addChatComponentMessage(new TextComponentTranslation("info.obelisk.dne"));
				return false;
			}
		}
		
		if (player.attemptTeleport(to.getX() + .5, to.getY() + 1, to.getZ() + .5)) {
			if (from != null)
				doEffects(world, from);
				
			doEffects(world, to);
		} else {
			player.addChatComponentMessage(new TextComponentTranslation("info.obelisk.noroom"));
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