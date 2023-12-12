package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.MagicArmor;
import com.smanzana.nostrummagica.network.NetworkHandler;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Sent from a client to server, or from a server to a bunch of clients. Updates state information about
 * an entities enchanted armor.
 * @author Skyler
 *
 */
public class EnchantedArmorStateUpdate {

	public static void handle(EnchantedArmorStateUpdate message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		LivingEntity ent = (ctx.get().getDirection().getReceptionSide().isClient()
				? (LivingEntity) NostrumMagica.instance.proxy.getPlayer().world.getEntityByID(message.entityID)
						: ctx.get().getSender());
		if (ent != null) {
			MagicArmor.HandleStateUpdate(message.state, ent, message.data);
			if (ctx.get().getDirection().getReceptionSide().isServer()) {
				// Bounce this update to everyone else
				EnchantedArmorStateUpdate bouncedMessage = new EnchantedArmorStateUpdate(message.state, message.data, ent.getEntityId());
				NetworkHandler.sendToDimension(bouncedMessage, ent.dimension);
			}
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
	
	private final ArmorState state;
	private final boolean data;
	private final int entityID;
	
	public EnchantedArmorStateUpdate(ArmorState state, boolean data) {
		this(state, data, 0);
	}
	
	public EnchantedArmorStateUpdate(ArmorState state, boolean data, int entityID) {
		this.state = state;
		this.data = data;
		this.entityID = entityID;
	}

	public static EnchantedArmorStateUpdate decode(PacketBuffer buf) {
		return new EnchantedArmorStateUpdate(
				buf.readEnumValue(ArmorState.class),
				buf.readBoolean(),
				buf.readVarInt()
				);
	}

	public static void encode(EnchantedArmorStateUpdate msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.state);
		buf.writeBoolean(msg.data);
		buf.writeVarInt(msg.entityID);
	}

}
