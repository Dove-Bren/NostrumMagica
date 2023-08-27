package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.EnchantedArmor;
import com.smanzana.nostrummagica.network.NetworkHandler;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent from a client to server, or from a server to a bunch of clients. Updates state information about
 * an entities enchanted armor.
 * @author Skyler
 *
 */
public class EnchantedArmorStateUpdate implements IMessage {

	public static class Handler implements IMessageHandler<EnchantedArmorStateUpdate, IMessage> {

		@Override
		public IMessage onMessage(EnchantedArmorStateUpdate message, MessageContext ctx) {
			
			final int id = message.tag.getInt(NBT_ID);
			ArmorState state;
			try {
				state = ArmorState.valueOf(message.tag.getString(NBT_TYPE).toUpperCase());
			} catch (Exception e) {
				state = ArmorState.JUMP;
			}
			final boolean data = message.tag.getBoolean(NBT_DATA);
			
			LivingEntity ent = (ctx.side.isClient() ? (LivingEntity) NostrumMagica.proxy.getPlayer().world.getEntityByID(id) : ctx.getServerHandler().player);
			if (ent != null) {
				EnchantedArmor.HandleStateUpdate(state, ent, data);
				if (ctx.side.isServer()) {
					// Bounce this update to everyone else
					message.tag.putInt(NBT_ID, ent.getEntityId());
					NetworkHandler.getSyncChannel().sendToDimension(message, ent.dimension);
				}
			}
			
			return null;
		}
		
	}
	
	public static enum ArmorState {
		FLYING,
		DRAGON_FLIGHT_TICK,
		JUMP,
		ENDER_DASH_SIDE,
		ENDER_DASH_BACK,
		WIND_TORNADO,
		EFFECT_TOGGLE,
		WIND_JUMP_WHIRLWIND,
	}
	
	private static final String NBT_ID = "id";
	private static final String NBT_TYPE = "type";
	private static final String NBT_DATA = "data";
	
	protected CompoundNBT tag;
	
	public EnchantedArmorStateUpdate() {
		tag = new CompoundNBT();
	}
	
	public EnchantedArmorStateUpdate(ArmorState state, boolean data) {
		this(state, data, 0);
	}
	
	public EnchantedArmorStateUpdate(ArmorState state, boolean data, int entityID) {
		this();
		tag.putString(NBT_TYPE, state.name().toLowerCase());
		tag.putBoolean(NBT_DATA, data);
		tag.putInt(NBT_ID, entityID);
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
