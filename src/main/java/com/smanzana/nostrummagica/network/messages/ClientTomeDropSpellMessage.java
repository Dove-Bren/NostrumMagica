package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has requested a spell be dropped from a spell tome
 * @author Skyler
 *
 */
public class ClientTomeDropSpellMessage implements IMessage {
	
	public static class Handler implements IMessageHandler<ClientTomeDropSpellMessage, IMessage> {

		@Override
		public IMessage onMessage(ClientTomeDropSpellMessage message, MessageContext ctx) {
			if (!message.tag.hasKey(NBT_TOME, NBT.TAG_INT) || !message.tag.hasKey(NBT_SPELL, NBT.TAG_INT))
				return null;
			
			final EntityPlayerMP sp = ctx.getServerHandler().playerEntity;
			final int tomeID = message.tag.getInteger(NBT_TOME);
			final int spellID = message.tag.getInteger(NBT_SPELL);
			
			sp.getServerWorld().addScheduledTask(() -> {
				INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
				
				if (att == null) {
					NostrumMagica.logger.warn("Could not look up player magic wrapper");
					return;
				}
				
				ItemStack tome = NostrumMagica.findTome(sp, tomeID);
				if (tome == null) {
					NostrumMagica.logger.warn("Could not find tome from client message");
					return;
				}
				
				if (!SpellTome.removeSpell(tome, spellID)) {
					NostrumMagica.logger.warn("Player requested to remove a spell that wasn't in the tome");
				}
			});
			
			return null;
		}
	}

	private static final String NBT_TOME = "tome_id";
	private static final String NBT_SPELL = "spell_id";
	
	protected NBTTagCompound tag;
	
	public ClientTomeDropSpellMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientTomeDropSpellMessage(ItemStack spellTome, int spellID) {
		tag = new NBTTagCompound();
		
		final int tomeID = SpellTome.getTomeID(spellTome);
		
		tag.setInteger(NBT_TOME, tomeID);
		tag.setInteger(NBT_SPELL, spellID);
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
