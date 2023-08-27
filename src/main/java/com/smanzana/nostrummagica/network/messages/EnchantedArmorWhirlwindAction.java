package com.smanzana.nostrummagica.network.messages;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent from a client to server, and requests a whirlwind be spawned
 * @author Skyler
 *
 */
public class EnchantedArmorWhirlwindAction implements IMessage {

	public static class Handler implements IMessageHandler<EnchantedArmorWhirlwindAction, IMessage> {

		@Override
		public IMessage onMessage(EnchantedArmorWhirlwindAction message, MessageContext ctx) {
			//LivingEntity ent = ctx.getServerHandler().player;
//			if (ent != null) {
//				EnchantedArmor.HandleStateUpdate(state, ent, data);
//				if (ctx.side.isServer()) {
//					// Bounce this update to everyone else
//					message.tag.putInt(NBT_ID, ent.getEntityId());
//					NetworkHandler.getSyncChannel().sendToDimension(message, ent.dimension);
//				}
//			}
			
			return null;
		}
		
	}
	
//	private static final String NBT_ID = "id";
//	private static final String NBT_TYPE = "type";
//	private static final String NBT_DATA = "data";
	
	protected CompoundNBT tag;
	
	public EnchantedArmorWhirlwindAction() {
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
