package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is updating client's local value for a special effect
 * @author Skyler
 *
 */
public class MagicEffectUpdate {

	public static void handle(MagicEffectUpdate message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			NostrumMagica.magicEffectProxy.setOverride(message.entityID, message.type, message.data);
		});
	}

	private final UUID entityID;
	private final SpecialEffect type;
	private final EffectData data;
	
	public MagicEffectUpdate(LivingEntity entity, SpecialEffect type, EffectData data) {
		this(entity.getUUID(), type, data);
	}
	
	public MagicEffectUpdate(UUID entityID, SpecialEffect type, EffectData data) {
		this.entityID = entityID;
		this.type = type;
		this.data = data;
	}

	public static MagicEffectUpdate decode(PacketBuffer buf) {
		return new MagicEffectUpdate(
				buf.readUUID(),
				buf.readEnum(SpecialEffect.class),
				buf.readBoolean()
						? EffectData.fromNBT(buf.readNbt())
						: null
				);
	}

	public static void encode(MagicEffectUpdate msg, PacketBuffer buf) {
		buf.writeUUID(msg.entityID);
		buf.writeEnum(msg.type);
		buf.writeBoolean(msg.data != null);
		if (msg.data != null) {
			buf.writeNbt(msg.data.toNBT());
		}
	}

}
