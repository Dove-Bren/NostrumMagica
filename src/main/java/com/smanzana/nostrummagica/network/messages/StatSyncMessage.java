package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client player's attribtes are being refreshed from server
 * @author Skyler
 *
 */
public class StatSyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<StatSyncMessage, IMessage> {

		@Override
		public IMessage onMessage(StatSyncMessage message, MessageContext ctx) {
			//update local attributes
			
			NostrumMagica.logger.info("Recieved Nostrum Magica sync message from server");
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				INostrumMagic override = CAPABILITY.getDefaultInstance();
				CAPABILITY.getStorage().readNBT(CAPABILITY, override, null, message.tag);
				NostrumMagica.proxy.receiveStatOverrides(override);
			});
			
			
			return null;
		}
		
	}
	
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public StatSyncMessage() {
		tag = new NBTTagCompound();
	}
	
	public StatSyncMessage(INostrumMagic stats) {
		tag = (NBTTagCompound) CAPABILITY.getStorage().writeNBT(CAPABILITY, stats, null);
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
