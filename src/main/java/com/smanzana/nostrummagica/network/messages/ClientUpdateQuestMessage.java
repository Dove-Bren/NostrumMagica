package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.quests.NostrumQuest;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has requested a quest become active or completed
 * @author Skyler
 *
 */
public class ClientUpdateQuestMessage implements IMessage {
	
	public static class Handler implements IMessageHandler<ClientUpdateQuestMessage, StatSyncMessage> {

		@Override
		public StatSyncMessage onMessage(ClientUpdateQuestMessage message, MessageContext ctx) {
			if (!message.tag.hasKey(NBT_QUEST, NBT.TAG_STRING))
				return null;
			
			final EntityPlayerMP sp = ctx.getServerHandler().player;
			
			sp.getServerWorld().addScheduledTask(() -> {
				INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
				
				if (att == null) {
					NostrumMagica.logger.warn("Could not look up player magic wrapper");
					return;
				}
				
				NostrumQuest quest = NostrumQuest.lookup(message.tag.getString(NBT_QUEST));
				
				if (quest == null) {
					NostrumMagica.logger.warn("Player requested a quest that DNE");
					return;
				}
				
				if (att.getCurrentQuests().contains(quest.getKey())) {
					if (quest.getObjective().isComplete(att))
						quest.completeQuest(sp);
				} else {
					if (NostrumMagica.canTakeQuest(sp, quest))
						quest.startQuest(sp);
				}
	
				 NetworkHandler.getSyncChannel().sendTo(new StatSyncMessage(att), sp);
			});
			
			return null;
		}
	}

	private static final String NBT_QUEST = "quest";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ClientUpdateQuestMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientUpdateQuestMessage(NostrumQuest quest) {
		tag = new NBTTagCompound();
		
		tag.setString(NBT_QUEST, quest.getKey());
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
