package com.smanzana.nostrummagica.network.messages;

import java.util.List;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.Spell;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
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
			// Note: This handler is not done on the game thread since the spell registry is thread safe.
			
			// What spells?
			ListNBT list = message.tag.getList(NBT_SPELLS, NBT.TAG_COMPOUND);
			if (list == null)
				return null;
			
			boolean clean = message.tag.getBoolean(NBT_CLEAN);
			if (clean) {
				NostrumMagica.logger.info("Cleaning spell registry to receive server's copy");
				NostrumMagica.getSpellRegistry().clear();
			}
			
			for (int i = 0; i < list.size(); i++) {
				CompoundNBT nbt = list.getCompoundTagAt(i);
				int id = nbt.getInt(NBT_ID);
				nbt = nbt.getCompound(NBT_SPELL);
				Spell spell = Spell.fromNBT(nbt, id);
				
				if (spell != null)
					NostrumMagica.getSpellRegistry().override(id, spell);
			}
			
			return null;
		}
		
	}

	private static final String NBT_SPELLS = "spells";
	private static final String NBT_SPELL = "spell";
	private static final String NBT_ID = "id";
	private static final String NBT_CLEAN = "clean";
	protected CompoundNBT tag;
	
	public SpellRequestReplyMessage() {
		tag = new CompoundNBT();
	}
	
	public SpellRequestReplyMessage(List<Spell> spells) {
		this(spells, false);
	}
	
	public SpellRequestReplyMessage(List<Spell> spells, boolean clean) {
		tag = new CompoundNBT();
		
		ListNBT list = new ListNBT();
		for (Spell spell : spells) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.putInt(NBT_ID, spell.getRegistryID());
			nbt.put(NBT_SPELL, spell.toNBT());
			list.add(nbt);
		}
		
		tag.put(NBT_SPELLS, list);
		tag.putBoolean(NBT_CLEAN, clean);
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
