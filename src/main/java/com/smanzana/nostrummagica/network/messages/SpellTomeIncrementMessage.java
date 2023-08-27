package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellTome;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Client has toggled vacuum setting on their reagent bag
 * @author Skyler
 *
 */
public class SpellTomeIncrementMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpellTomeIncrementMessage, IMessage> {

		@Override
		public IMessage onMessage(SpellTomeIncrementMessage message, MessageContext ctx) {
			// Is it on?
			
			final ServerPlayerEntity sp = ctx.getServerHandler().player;
			
			int value = message.tag.getInt(NBT_VALUE);
			
			sp.getServerWorld().runAsync(() -> {
				SpellTome.setIndex(NostrumMagica.getCurrentTome(sp),
						value);
			});
			
			return null;
		}
		
	}

	private static final String NBT_VALUE = "index";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public SpellTomeIncrementMessage() {
		tag = new CompoundNBT();
	}
	
	public SpellTomeIncrementMessage(int index) {
		tag = new CompoundNBT();
		
		tag.putInt(NBT_VALUE, index);
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
