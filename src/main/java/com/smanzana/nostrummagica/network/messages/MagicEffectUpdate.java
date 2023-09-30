package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

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
		this(entity.getUniqueID(), type, data);
	}
	
	public MagicEffectUpdate(UUID entityID, SpecialEffect type, EffectData data) {
		this.entityID = entityID;
		this.type = type;
		this.data = data;
	}

	public static MagicEffectUpdate decode(PacketBuffer buf) {
		return new MagicEffectUpdate(
				buf.readUniqueId(),
				buf.readEnumValue(SpecialEffect.class),
				buf.readBoolean()
						? EffectData.fromNBT(buf.readCompoundTag())
						: null
				);
	}

	public static void encode(MagicEffectUpdate msg, PacketBuffer buf) {
		buf.writeUniqueId(msg.entityID);
		buf.writeEnumValue(msg.type);
		buf.writeBoolean(msg.data != null);
		if (msg.data != null) {
			buf.writeCompoundTag(msg.data.toNBT());
		}
	}

}
