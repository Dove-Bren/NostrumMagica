package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
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
public class ClientCastMessage implements IMessage {

	public static class Handler implements IMessageHandler<ClientCastMessage, IMessage> {

		@Override
		public IMessage onMessage(ClientCastMessage message, MessageContext ctx) {
			//update local attributes
			
			System.out.println("Debug: Net stuff sync message received");
			NostrumMagica.logger.info("Debug: Recieved sync message from server");
			EntityPlayer sp = NostrumMagica.proxy.getPlayer();
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Server is pushing into DnDAttributes, but they don't exist on our player!");
				return null;
			}
			
			INostrumMagic cap = CAPABILITY.getDefaultInstance();
			CAPABILITY.getStorage().readNBT(CAPABILITY, cap, null, message.tag);
			att.copy(cap);

			return null;
		}
		
	}
	
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientCastMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientCastMessage(INostrumMagic stats) {
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
