package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.items.WarlockSword;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has cast a spell
 * @author Skyler
 *
 */
public class BladeCastMessage implements IMessage {

	public static class Handler implements IMessageHandler<BladeCastMessage, ClientCastReplyMessage> {

		@Override
		public ClientCastReplyMessage onMessage(BladeCastMessage message, MessageContext ctx) {
			// Attempt blade cast
			// TODO make this more generic with an interface or something and move trying to find the hand
			// into a helper on the interface instead of in warlock blade
			final ServerPlayerEntity sp = ctx.getServerHandler().player;
			
			sp.getServerWorld().runAsync(() -> {
				WarlockSword.DoCast(sp);
			});
			
			return null;
		}
	}

	protected CompoundNBT tag;
	
	public BladeCastMessage() {
		tag = new CompoundNBT();
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
