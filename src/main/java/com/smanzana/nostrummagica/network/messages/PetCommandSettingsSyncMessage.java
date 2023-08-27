package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
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
			Minecraft.getInstance().runAsync(() -> {
				NostrumMagica.getPetCommandManager().overrideClientSettings(message.tag);
			});
			
			return null;
		}
		
	}
	
	protected CompoundNBT tag;
	
	public PetCommandSettingsSyncMessage() {
		tag = new CompoundNBT();
	}
	
	public PetCommandSettingsSyncMessage(CompoundNBT nbt) {
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
