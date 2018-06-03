package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * @author Skyler
 *
 */
public class ClientCastReplyMessage implements IMessage {

	public static class Handler implements IMessageHandler<ClientCastReplyMessage, IMessage> {

		@Override
		public IMessage onMessage(ClientCastReplyMessage message, MessageContext ctx) {

			INostrumMagic att = NostrumMagica.getMagicWrapper(
					NostrumMagica.proxy.getPlayer());
			// Regardless of success, server has synced mana with us.
			int mana = message.tag.getInteger(NBT_MANA);
			float xp = message.tag.getFloat(NBT_XP);
			boolean success = message.tag.getBoolean(NBT_STATUS);
			
			att.setMana(mana);
			
			if (success) {
				// On success, server sends XP that was added
				att.addXP(xp);
			} else {
				
			}
			
			// Success or nah?
			

			return null;
		}
		
	}

	private static final String NBT_STATUS = "status";
	private static final String NBT_MANA = "mana";
	private static final String NBT_XP = "xp";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientCastReplyMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientCastReplyMessage(boolean success, int mana, float xp) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_MANA, mana);
		tag.setFloat(NBT_XP, xp);
		tag.setBoolean(NBT_STATUS, success);
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
