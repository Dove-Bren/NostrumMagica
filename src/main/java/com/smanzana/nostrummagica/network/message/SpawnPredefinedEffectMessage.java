package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.util.NetUtils;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is signalling that a ritual has been performed, and the effects should be shown.
 * Perhaps this should be more generic
 * @author Skyler
 *
 */
public class SpawnPredefinedEffectMessage {

	public static void handle(SpawnPredefinedEffectMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Player player = NostrumMagica.instance.proxy.getPlayer();
		if (!DimensionUtils.InDimension(player, message.dimension)) {
			return;
		}
		
		Entity ent = player.level.getEntity(message.entityID);
		Vec3 offset = (ent != null ? Vec3.ZERO : message.position);
		
		if (offset == null) {
			offset = Vec3.ZERO;
		}
		
		ClientPredefinedEffect.Spawn(offset, message.type, message.duration, ent);
	}

	protected final PredefinedEffect type;
	protected final ResourceKey<Level> dimension;
	protected final int duration;
	
	protected final @Nullable Vec3 position;
	protected final int entityID;
	
	public SpawnPredefinedEffectMessage(PredefinedEffect type, int duration, ResourceKey<Level> dimension, Vec3 position) {
		this.type = type;
		this.duration = duration;
		this.dimension = dimension;
		this.position = position;
		this.entityID = 0;
	}
	
	public SpawnPredefinedEffectMessage(PredefinedEffect type, int duration, ResourceKey<Level> dimension, int entityID) {
		this.type = type;
		this.duration = duration;
		this.dimension = dimension;
		this.entityID = entityID;
		this.position = null;
	}

	public static SpawnPredefinedEffectMessage decode(FriendlyByteBuf buf) {
		final PredefinedEffect type;
		final ResourceKey<Level> dimension;
		final int duration;
		
		//final @Nullable Vector3d position;
		//final int entityID;
		
		type = buf.readEnum(PredefinedEffect.class);
		duration = buf.readVarInt();
		dimension = NetUtils.unpackDimension(buf);
		
		if (buf.readBoolean()) {
			return new SpawnPredefinedEffectMessage(type, duration, dimension, new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble()));
		} else {
			return new SpawnPredefinedEffectMessage(type, duration, dimension, buf.readVarInt());
		}
	}

	public static void encode(SpawnPredefinedEffectMessage msg, FriendlyByteBuf buf) {
		buf.writeEnum(msg.type);
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
