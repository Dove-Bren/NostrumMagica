package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI;

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
public class TamedDragonGUISyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<TamedDragonGUISyncMessage, IMessage> {

		@Override
		public IMessage onMessage(TamedDragonGUISyncMessage message, MessageContext ctx) {
			// Get ID
			NBTTagCompound nbt = message.tag.getCompoundTag(NBT_MESSAGE);
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				TamedDragonGUI.updateClientContainer(nbt);
			});
			
			return null;
		}
		
	}

	private static final String NBT_MESSAGE = "message";
	
	protected NBTTagCompound tag;
	
	public TamedDragonGUISyncMessage() {
		tag = new NBTTagCompound();
	}
	
	public TamedDragonGUISyncMessage(NBTTagCompound data) {
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
