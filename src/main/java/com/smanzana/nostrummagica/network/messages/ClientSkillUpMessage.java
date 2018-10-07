package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has requested a skillpoint be spent
 * @author Skyler
 *
 */
public class ClientSkillUpMessage implements IMessage {
	
	public static enum Type {
		TECHNIQUE,
		FINESSE,
		CONTROL,
	}

	public static class Handler implements IMessageHandler<ClientSkillUpMessage, StatSyncMessage> {

		@Override
		public StatSyncMessage onMessage(ClientSkillUpMessage message, MessageContext ctx) {
			EntityPlayer sp = ctx.getServerHandler().player;
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return null;
			}
			
			if (!message.tag.hasKey(NBT_TYPE, NBT.TAG_INT))
				return null;
			
			int ord = message.tag.getInteger(NBT_TYPE);
			
			Type type = Type.values()[ord];
			
			if (att.getSkillPoints() > 0) {
				switch (type) {
				case CONTROL:
					att.addControl();
					break;
				case FINESSE:
					att.addFinesse();
					break;
				case TECHNIQUE:
					att.addTech();
					break;
				default: // don't take point when something's wrong!
					return null;
				}
				att.takeSkillPoint();
			}
			

			return new StatSyncMessage(att);
		}
	}

	private static final String NBT_TYPE = "type";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientSkillUpMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientSkillUpMessage(Type type) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_TYPE, type.ordinal());
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
