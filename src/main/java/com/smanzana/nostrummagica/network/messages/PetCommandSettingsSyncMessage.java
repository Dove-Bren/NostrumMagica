package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client player's attribtes are being refreshed from server
 * @author Skyler
 *
 */
public class PetCommandSettingsSyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<PetCommandSettingsSyncMessage, IMessage> {

		@Override
		public IMessage onMessage(PetCommandSettingsSyncMessage message, MessageContext ctx) {
			Minecraft.getMinecraft().addScheduledTask(() -> {
				NostrumMagica.getPetCommandManager().overrideClientSettings(message.tag);
			});
			
			return null;
		}
		
	}
	
	protected NBTTagCompound tag;
	
	public PetCommandSettingsSyncMessage() {
		tag = new NBTTagCompound();
	}
	
	public PetCommandSettingsSyncMessage(NBTTagCompound nbt) {
		tag = nbt;
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
