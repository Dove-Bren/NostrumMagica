package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.client.gui.petgui.PetGUI;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundNBT;
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
			CompoundNBT nbt = message.tag.getCompound(NBT_MESSAGE);
			
			Minecraft.getInstance().runAsync(() -> {
				PetGUI.updateClientContainer(nbt);
			});
			
			return null;
		}
		
	}

	private static final String NBT_MESSAGE = "message";
	
	protected CompoundNBT tag;
	
	public PetGUISyncMessage() {
		tag = new CompoundNBT();
	}
	
	public PetGUISyncMessage(CompoundNBT data) {
		this();
		
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
