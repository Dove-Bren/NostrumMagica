package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is informing client about their custom dimension
 * @author Skyler
 *
 */
public class DimensionSyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<DimensionSyncMessage, IMessage> {

		@Override
		public IMessage onMessage(DimensionSyncMessage message, MessageContext ctx) {
			NostrumMagica.logger.info("Recieved dimension info from server");
			
			int count = message.tag.getInteger(NBT_COUNT);
			
			if (count != ModConfig.config.dimensionCount()) {
				throw new RuntimeException(
						"Server dimension count ("
								+ count
								+ ") DOES NOT MATCH client configuration ("
								+ ModConfig.config.dimensionCount()
								+ "). Sync Nostrum Magica configuration with server to continue!");
			}
			
			
			return null;
		}
		
	}
	
	private static final String NBT_COUNT = "count";
	protected NBTTagCompound tag;
	
	public DimensionSyncMessage() {
		tag = new NBTTagCompound();
	}
	
	public DimensionSyncMessage(int count) {
		tag = new NBTTagCompound();
		tag.setInteger(NBT_COUNT, count);
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
