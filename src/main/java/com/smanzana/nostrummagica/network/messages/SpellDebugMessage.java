package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.config.ModConfig;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.ITextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * A spell was cast and the server has generated the debug information
 * for the client, if it wants it.
 * @author Skyler
 *
 */
public class SpellDebugMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpellDebugMessage, IMessage> {

		@Override
		public IMessage onMessage(SpellDebugMessage message, MessageContext ctx) {

			if (!ModConfig.config.spellDebug()) {
				return null;
			}
			
			String chat = message.tag.getString(NBT_CHAT);
			if (chat == null || chat.trim().isEmpty())
				return null;
			
			final ITextComponent text = ITextComponent.Serializer.jsonToComponent(chat);
			
			Minecraft.getMinecraft().addScheduledTask(() -> {
				NostrumMagica.proxy.getPlayer().sendMessage(text);
			});
			
			
			return null;
		}
		
	}

	private static final String NBT_CHAT = "chat";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public SpellDebugMessage() {
		tag = new NBTTagCompound();
	}
	
	public SpellDebugMessage(ITextComponent comp) {
		tag = new NBTTagCompound();
		
		tag.setString(NBT_CHAT, ITextComponent.Serializer.componentToJson(comp));
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
