package com.smanzana.nostrummagica.network.messages;

import java.util.LinkedList;
import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.Spell;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client is asking server for details about a spell (or multiple)
 * @author Skyler
 *
 */
public class SpellRequestMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpellRequestMessage, SpellRequestReplyMessage> {

		@Override
		public SpellRequestReplyMessage onMessage(SpellRequestMessage message, MessageContext ctx) {
			// What spells?
			System.out.println("Request for some spells...");
			int ids[] = message.tag.getIntArray(NBT_IDS);
			if (ids == null)
				return null;
			
			System.out.println("requesting " + ids.length + " spells");
			
			List<Spell> spells = new LinkedList<>();
			Spell spell;
			for (int id : ids) {
				spell = NostrumMagica.spellRegistry.lookup(id);
				if (spell != null)
					spells.add(spell);
				else
					System.out.println("Couldn't match spell for id " + id);
			}
			
			if (spells.isEmpty()) {
				System.out.println("Failed to find any spells at all!");
				return null;
			}

			System.out.println("Sending reply");
			return new SpellRequestReplyMessage(spells);
		}
		
	}

	private static final String NBT_IDS = "ids";
	protected NBTTagCompound tag;
	
	public SpellRequestMessage() {
		tag = new NBTTagCompound();
	}
	
	public SpellRequestMessage(int ids[]) {
		tag = new NBTTagCompound();
		
		tag.setIntArray(NBT_IDS, ids);
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
