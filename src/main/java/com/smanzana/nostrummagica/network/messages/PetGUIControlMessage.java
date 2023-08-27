package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
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
			int id = message.tag.getInt(NBT_KEY);
			CompoundNBT nbt = message.tag.getCompound(NBT_MESSAGE);
			
			ctx.getServerHandler().player.getServerWorld().runAsync(() -> {
				PetGUI.updateServerContainer(id, nbt);
			});
			
			return null;
		}
		
	}

	private static final String NBT_KEY = "key";
	private static final String NBT_MESSAGE = "message";
	
	protected CompoundNBT tag;
	
	public PetGUIControlMessage() {
		tag = new CompoundNBT();
	}
	
	public PetGUIControlMessage(int id, CompoundNBT data) {
		this();
		
		tag.putInt(NBT_KEY, id);
		tag.put(NBT_MESSAGE, data);
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
