package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ReagentBag;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
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
public class ReagentBagToggleMessage implements IMessage {

	public static class Handler implements IMessageHandler<ReagentBagToggleMessage, IMessage> {

		@Override
		public IMessage onMessage(ReagentBagToggleMessage message, MessageContext ctx) {
			// Is it on?
			
			ServerPlayerEntity sp = ctx.getServerHandler().player;
			
			boolean main = message.tag.getBoolean(NBT_MAIN);
			boolean value = message.tag.getBoolean(NBT_VALUE);

			
			
			sp.getServerWorld().runAsync(() -> {
				ItemStack bag;
				if (main)
					bag = sp.getHeldItemMainhand();
				else
					bag = sp.getHeldItemOffhand();
				if (bag.isEmpty() || !(bag.getItem() instanceof ReagentBag)) {
					NostrumMagica.logger.warn("Reagent bag double-check position was invalid! Is the server behind?");
				}
				
				ReagentBag.setVacuumEnabled(bag, value);
			});
			
			return null;
		}
		
	}

	private static final String NBT_VALUE = "value";
	private static final String NBT_MAIN = "mainhand";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public ReagentBagToggleMessage() {
		tag = new CompoundNBT();
	}
	
	public ReagentBagToggleMessage(boolean isMainHand, boolean isOn) {
		tag = new CompoundNBT();
		
		tag.putBoolean(NBT_VALUE, isOn);
		tag.putBoolean(NBT_MAIN, isMainHand);
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
