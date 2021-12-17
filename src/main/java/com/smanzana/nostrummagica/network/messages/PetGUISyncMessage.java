package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is sending some syncing data to the client GUI
 * @author Skyler
 *
 */
public class PetGUISyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<PetGUISyncMessage, IMessage> {

		@Override
		public IMessage onMessage(PetGUISyncMessage message, MessageContext ctx) {
			// Get ID
			NBTTagCompound nbt = message.tag.getCompoundTag(NBT_MESSAGE);
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				PetGUI.updateClientContainer(nbt);
			});
			
			return null;
		}
		
	}

	private static final String NBT_MESSAGE = "message";
	
	protected NBTTagCompound tag;
	
	public PetGUISyncMessage() {
		tag = new NBTTagCompound();
	}
	
	public PetGUISyncMessage(NBTTagCompound data) {
		this();
		
		tag.setTag(NBT_MESSAGE, data);
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
