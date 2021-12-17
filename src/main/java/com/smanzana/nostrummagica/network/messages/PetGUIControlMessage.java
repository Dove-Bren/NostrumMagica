package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;

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
public class PetGUIControlMessage implements IMessage {

	public static class Handler implements IMessageHandler<PetGUIControlMessage, IMessage> {

		@Override
		public IMessage onMessage(PetGUIControlMessage message, MessageContext ctx) {
			// Get ID
			int id = message.tag.getInteger(NBT_KEY);
			NBTTagCompound nbt = message.tag.getCompoundTag(NBT_MESSAGE);
			
			ctx.getServerHandler().player.getServerWorld().addScheduledTask(() -> {
				PetGUI.updateServerContainer(id, nbt);
			});
			
			return null;
		}
		
	}

	private static final String NBT_KEY = "key";
	private static final String NBT_MESSAGE = "message";
	
	protected NBTTagCompound tag;
	
	public PetGUIControlMessage() {
		tag = new NBTTagCompound();
	}
	
	public PetGUIControlMessage(int id, NBTTagCompound data) {
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
