package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.client.gui.dragongui.TamedDragonGUI;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has performed some action in a dragon GUI
 * @author Skyler
 *
 */
public class TamedDragonGUIControlMessage implements IMessage {

	public static class Handler implements IMessageHandler<TamedDragonGUIControlMessage, IMessage> {

		@Override
		public IMessage onMessage(TamedDragonGUIControlMessage message, MessageContext ctx) {
			// Get ID
			int id = message.tag.getInteger(NBT_KEY);
			NBTTagCompound nbt = message.tag.getCompoundTag(NBT_MESSAGE);
			
			ctx.getServerHandler().playerEntity.getServerWorld().addScheduledTask(() -> {
				TamedDragonGUI.updateContainer(id, nbt);
			});
			
			return null;
		}
		
	}

	private static final String NBT_KEY = "key";
	private static final String NBT_MESSAGE = "message";
	
	protected NBTTagCompound tag;
	
	public TamedDragonGUIControlMessage() {
		tag = new NBTTagCompound();
	}
	
	public TamedDragonGUIControlMessage(int id, NBTTagCompound data) {
		this();
		
		tag.setInteger(NBT_KEY, id);
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
