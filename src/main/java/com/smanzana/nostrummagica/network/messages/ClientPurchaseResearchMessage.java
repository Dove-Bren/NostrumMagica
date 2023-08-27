package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.research.NostrumResearch;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has requested a research item be purchased
 * @author Skyler
 *
 */
public class ClientPurchaseResearchMessage implements IMessage {
	
	public static class Handler implements IMessageHandler<ClientPurchaseResearchMessage, StatSyncMessage> {

		@Override
		public StatSyncMessage onMessage(ClientPurchaseResearchMessage message, MessageContext ctx) {
			if (!message.tag.contains(NBT_RESEARCH, NBT.TAG_STRING))
				return null;
			
			final ServerPlayerEntity sp = ctx.getServerHandler().player;
			
			sp.getServerWorld().runAsync(() -> {
				INostrumMagic att = NostrumMagica.getMagicWrapper(sp);
				
				if (att == null) {
					NostrumMagica.logger.warn("Could not look up player magic wrapper");
					return;
				}
				
				NostrumResearch research = NostrumResearch.lookup(message.tag.getString(NBT_RESEARCH));
				
				if (research == null) {
					NostrumMagica.logger.warn("Player requested a research that DNE");
					return;
				}
				
				if (NostrumMagica.canPurchaseResearch(sp, research) && att.getResearchPoints() > 0) {
					att.takeResearchPoint();
					NostrumResearch.unlockResearch(sp, research);
				}
	
				 NetworkHandler.getSyncChannel().sendTo(new StatSyncMessage(att), sp);
			});
			
			return null;
		}
	}

	private static final String NBT_RESEARCH = "research";
	
	protected CompoundNBT tag;
	
	public ClientPurchaseResearchMessage() {
		tag = new CompoundNBT();
	}
	
	public ClientPurchaseResearchMessage(NostrumResearch research) {
		tag = new CompoundNBT();
		
		tag.putString(NBT_RESEARCH, research.getKey());
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
