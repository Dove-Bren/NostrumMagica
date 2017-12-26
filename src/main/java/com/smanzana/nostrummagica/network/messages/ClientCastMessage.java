package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.spells.Spell;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has cast a spell
 * @author Skyler
 *
 */
public class ClientCastMessage implements IMessage {

	public static class Handler implements IMessageHandler<ClientCastMessage, ClientCastReplyMessage> {

		@Override
		public ClientCastReplyMessage onMessage(ClientCastMessage message, MessageContext ctx) {
			// Figure out what spell they have
			// cast it if they can
			
			EntityPlayer sp = ctx.getServerHandler().playerEntity;
			
			// What spell?
			Spell spell = NostrumMagica.spellRegistry.lookup(
					message.tag.getInteger(NBT_ID)
					);
			
			if (spell == null) {
				NostrumMagica.logger.warn("Could not find matching spell from client cast request");
				return null;
			}
			
			INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
			
			if (att == null) {
				NostrumMagica.logger.warn("Could not look up player magic wrapper");
				return null;
			}
			
			// Cast it!
			int cost = spell.getManaCost();
			if (att.getMana() < cost)
				return new ClientCastReplyMessage(false, att.getMana());
			
			att.addMana(-cost);
			boolean seen = att.wasSpellDone(spell);
			spell.cast(sp);
			
			att.addXP(spell.getXP(seen));

			return new ClientCastReplyMessage(true, att.getMana());
		}
		
	}

	private static final String NBT_ID = "id";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientCastMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientCastMessage(Spell spell) {
		this(spell.getRegistryID());
	}
	
	public ClientCastMessage(int id) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_ID, id);
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