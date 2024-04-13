package com.smanzana.nostrummagica.network.messages;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.utils.DimensionUtils;
import com.smanzana.nostrummagica.utils.NetUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.RegistryKey;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is signalling that a ritual has been performed, and the effects should be shown.
 * Perhaps this should be more generic
 * @author Skyler
 *
 */
public class SpawnPredefinedEffectMessage {

	public static void handle(SpawnPredefinedEffectMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
		if (!DimensionUtils.InDimension(player, message.dimension)) {
			return;
		}
		
		Entity ent = player.world.getEntityByID(message.entityID);
		Vector3d offset = (ent != null ? Vector3d.ZERO : message.position);
		
		if (offset == null) {
			offset = Vector3d.ZERO;
		}
		
		ClientPredefinedEffect.Spawn(offset, message.type, message.duration, ent);
	}

	protected final PredefinedEffect type;
	protected final RegistryKey<World> dimension;
	protected final int duration;
	
	protected final @Nullable Vector3d position;
	protected final int entityID;
	
	public SpawnPredefinedEffectMessage(PredefinedEffect type, int duration, RegistryKey<World> dimension, Vector3d position) {
		this.type = type;
		this.duration = duration;
		this.dimension = dimension;
		this.position = position;
		this.entityID = 0;
	}
	
	public SpawnPredefinedEffectMessage(PredefinedEffect type, int duration, RegistryKey<World> dimension, int entityID) {
		this.type = type;
		this.duration = duration;
		this.dimension = dimension;
		this.entityID = entityID;
		this.position = null;
	}

	public static SpawnPredefinedEffectMessage decode(PacketBuffer buf) {
		final PredefinedEffect type;
		final RegistryKey<World> dimension;
		final int duration;
		
		//final @Nullable Vector3d position;
		//final int entityID;
		
		type = buf.readEnumValue(PredefinedEffect.class);
		duration = buf.readVarInt();
		dimension = NetUtils.unpackDimension(buf);
		
		if (buf.readBoolean()) {
			return new SpawnPredefinedEffectMessage(type, duration, dimension, new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		} else {
			return new SpawnPredefinedEffectMessage(type, duration, dimension, buf.readVarInt());
		}
	}

	public static void encode(SpawnPredefinedEffectMessage msg, PacketBuffer buf) {
		buf.writeEnumValue(msg.type);
		buf.writeVarInt(msg.duration);
		NetUtils.packDimension(buf, msg.dimension);
		
		buf.writeBoolean(msg.position != null);
		if (msg.position != null) {
			buf.writeDouble(msg.position.x);
			buf.writeDouble(msg.position.y);
			buf.writeDouble(msg.position.z);
		} else {
			buf.writeVarInt(msg.entityID);
		}
	}

}
