package com.smanzana.nostrummagica.config.network;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server config given to clients on connect
 * @author Skyler
 *
 */
public class ServerConfigMessage implements IMessage {

	public static class Handler implements IMessageHandler<ServerConfigMessage, IMessage> {

		@Override
		public IMessage onMessage(ServerConfigMessage message, MessageContext ctx) {
			//have tag, now read it into local config
			NostrumMagica.logger.info("Reading server config overrides");

			for (ModConfig.Key key : ModConfig.Key.values())
			if (key.isServerBound()) {
				//load up value from nbt tag
				ModConfig.config.updateLocal(key, key.valueFromNBT(message.tag));
			}
			
			return null;
		}
		
	}
	
	protected NBTTagCompound tag;
	
	
	public ServerConfigMessage() {
		tag = new NBTTagCompound();
	}
	
	public ServerConfigMessage(ModConfig config) {
		this();
		
		//pull out server values from base, send them over
		for (ModConfig.Key key : ModConfig.Key.values())
		if (key.isServerBound()) {
			key.saveToNBT(config, tag);
		}
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
