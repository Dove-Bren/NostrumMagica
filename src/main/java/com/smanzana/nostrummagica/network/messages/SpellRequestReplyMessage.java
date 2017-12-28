package com.smanzana.nostrummagica.network.messages;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.Spell;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is replying to client detail request
 * @author Skyler
 *
 */
public class SpellRequestReplyMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpellRequestReplyMessage, IMessage> {

		@Override
		public IMessage onMessage(SpellRequestReplyMessage message, MessageContext ctx) {
			// What spells?
			NBTTagList list = message.tag.getTagList(NBT_SPELLS, NBT.TAG_COMPOUND);
			if (list == null)
				return null;
			
			boolean clean = message.tag.getBoolean(NBT_CLEAN);
			if (clean) {
				NostrumMagica.spellRegistry.clear();
			}
			
			for (int i = 0; i < list.tagCount(); i++) {
				NBTTagCompound nbt = list.getCompoundTagAt(i);
				int id = nbt.getInteger(NBT_ID);
				nbt = nbt.getCompoundTag(NBT_SPELL);
				Spell spell = Spell.fromNBT(nbt, id);
				
				if (spell != null)
					NostrumMagica.spellRegistry.override(id, spell);
			}
			
			return null;
		}
		
	}

	private static final String NBT_SPELLS = "spells";
	private static final String NBT_SPELL = "spell";
	private static final String NBT_ID = "id";
	private static final String NBT_CLEAN = "clean";
	protected NBTTagCompound tag;
	
	public SpellRequestReplyMessage() {
		tag = new NBTTagCompound();
	}
	
	public SpellRequestReplyMessage(List<Spell> spells) {
		this(spells, false);
	}
	
	public SpellRequestReplyMessage(List<Spell> spells, boolean clean) {
		tag = new NBTTagCompound();
		
		NBTTagList list = new NBTTagList();
		for (Spell spell : spells) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setInteger(NBT_ID, spell.getRegistryID());
			nbt.setTag(NBT_SPELL, spell.toNBT());
			list.appendTag(nbt);
		}
		
		tag.setTag(NBT_SPELLS, list);
		tag.setBoolean(NBT_CLEAN, clean);
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
