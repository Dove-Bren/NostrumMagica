package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.blocks.tiles.NostrumObeliskEntity;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has selected which other target the obelisk should target
 * @author Skyler
 *
 */
public class ObeliskSelectMessage implements IMessage {

	public static class Handler implements IMessageHandler<ObeliskSelectMessage, ManaMessage> {

		@Override
		public ManaMessage onMessage(ObeliskSelectMessage message, MessageContext ctx) {
			// Get from and to
			// Validate to is an obelisk and then try to update it
			
			if (message.tag.hasKey(NBT_OBELISK, NBT.TAG_LONG)
					&& message.tag.hasKey(NBT_INDEX, NBT.TAG_INT)) {
				final BlockPos obeliskPos = BlockPos.fromLong(message.tag.getLong(NBT_OBELISK));
				final int index = message.tag.getInteger(NBT_INDEX);
				
				EntityPlayerMP player = ctx.getServerHandler().player;
				player.getServerWorld().addScheduledTask(() -> {
					TileEntity te = player.world.getTileEntity(obeliskPos);
					if (te != null && te instanceof NostrumObeliskEntity) {
						((NostrumObeliskEntity) te).setTargetIndex(index);
					}
				});
			}
			
			return null;
		}
		
	}

	private static final String NBT_OBELISK = "obelisk";
	private static final String NBT_INDEX = "index";
	//@CapabilityInject(INostrumMagic.class)
	//public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ObeliskSelectMessage() {
		tag = new NBTTagCompound();
	}
	
	/**
	 * 
	 * @param from If null, assumes a ritual with only a to instead of obelisk tele
	 * @param to
	 */
	public ObeliskSelectMessage(BlockPos obeliskPos, int index) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_INDEX, index);
		tag.setLong(NBT_OBELISK, obeliskPos.toLong());
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}
}
