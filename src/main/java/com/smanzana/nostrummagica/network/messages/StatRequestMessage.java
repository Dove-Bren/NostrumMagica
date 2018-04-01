package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client is requesting the stats for an entity
 * @author Skyler
 *
 */
public class StatRequestMessage implements IMessage {

	public static class Handler implements IMessageHandler<StatRequestMessage, StatSyncMessage> {

		@Override
		public StatSyncMessage onMessage(StatRequestMessage message, MessageContext ctx) {
			
			EntityPlayer sp = ctx.getServerHandler().playerEntity;
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return null;
			}

			return new StatSyncMessage(att);
		}

	}

	public StatRequestMessage() {
		
	}
	
	@Override
	public void fromBytes(ByteBuf buf) {
		;
	}

	@Override
	public void toBytes(ByteBuf buf) {
		;
	}

}
