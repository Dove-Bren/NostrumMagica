package com.smanzana.nostrummagica.network.messages;

import java.util.Map;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * Includes xp and spent reagent totals
 * @author Skyler
 *
 */
public class ClientCastReplyMessage implements IMessage {

	public static class Handler implements IMessageHandler<ClientCastReplyMessage, IMessage> {

		@Override
		public IMessage onMessage(ClientCastReplyMessage message, MessageContext ctx) {

			Minecraft.getInstance().runAsync(() -> {
				PlayerEntity player = NostrumMagica.proxy.getPlayer();
				INostrumMagic att = NostrumMagica.getMagicWrapper(
						player);
				// Regardless of success, server has synced mana with us.
				int mana = message.tag.getInt(NBT_MANA);
				float xp = message.tag.getFloat(NBT_XP);
				boolean success = message.tag.getBoolean(NBT_STATUS);
				
				att.setMana(mana);
				
				if (success) {
					// On success, server sends XP that was added
					att.addXP(xp);
				} else {
					
				}
				
//				if (message.tag.contains(NBT_REAGENTS, NBT.TAG_COMPOUND)) {
//					CompoundNBT regs = message.tag.getCompound(NBT_REAGENTS);
//					if (!regs.keySet().isEmpty())
//					for (String key : regs.keySet()) {
//						int cost = regs.getInt(key);
//						if (cost == 0)
//							continue;
//						
//						try {
//							ReagentType type = ReagentType.valueOf(key);
//							NostrumMagica.removeReagents(player, type, cost);
//						} catch (Exception e) {
//							;
//						}
//					}
//					
//				}
			});
			

			return null;
		}
		
	}

	private static final String NBT_STATUS = "status";
	private static final String NBT_MANA = "mana";
	private static final String NBT_XP = "xp";
	private static final String NBT_REAGENTS = "reagents";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public ClientCastReplyMessage() {
		tag = new CompoundNBT();
	}
	
	public ClientCastReplyMessage(boolean success, int mana, float xp,
			Map<ReagentType, Integer> reagentCost) {
		tag = new CompoundNBT();
		
		tag.putInt(NBT_MANA, mana);
		tag.putFloat(NBT_XP, xp);
		tag.putBoolean(NBT_STATUS, success);
		
		if (reagentCost != null) {
			CompoundNBT nbt = new CompoundNBT();
			for (ReagentType type : reagentCost.keySet()) {
				if (type == null)
					continue;
				
				Integer cost = reagentCost.get(type);
				if (cost == null || cost == 0)
					continue;
				
				nbt.putInt(type.name(), cost);
			}
			tag.put(NBT_REAGENTS, nbt);
		}
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
