package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is informing a client of a status effect that is affecting an entity other than the
 * player.
 * Vanilla does not sync these so we will for ones that are important.
 * Note: Can't use vanilla packets as there isn't an easy way to send them. Can't send through SimpleImpl or TrackedEntity...
 * @author Skyler
 *
 */
public class VanillaEffectSyncMessage {
	
	public final int entityID;
	public final @Nonnull Effect effect;
	public final @Nullable EffectInstance instance;
	
	private VanillaEffectSyncMessage(int entityID, Effect effect, @Nullable EffectInstance instance) {
		this.entityID = entityID;
		this.effect = effect;
		this.instance = instance;
		
		if (instance != null && instance.getEffect() != effect) {
			throw new RuntimeException("Effect and instance effect do not match.");
		}
	}

	public VanillaEffectSyncMessage(int entityID, @Nonnull EffectInstance effect) {
		this(entityID, effect.getEffect(), effect);
	}

	public VanillaEffectSyncMessage(Entity entity, @Nonnull EffectInstance effect) {
		this(entity.getId(), effect.getEffect(), effect);
	}
	
	public VanillaEffectSyncMessage(int entityID, @Nonnull Effect effect) {
		this(entityID, effect, null);
	}
	
	public VanillaEffectSyncMessage(Entity entity, @Nonnull Effect effect) {
		this(entity.getId(), effect, null);
	}

	public static void handle(VanillaEffectSyncMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		ctx.get().enqueueWork(() -> {
			final Minecraft mc = Minecraft.getInstance();
			if (mc.level == null) {
				return;
			}
			
			@Nullable Entity ent = mc.level.getEntity(message.entityID);
			if (ent == null || !(ent instanceof LivingEntity)) {
				return;
			}
			
			LivingEntity living = (LivingEntity) ent;
			if (message.instance == null) {
				living.removeEffect(message.effect);
			} else {
				if (!living.addEffect(message.instance)) {
					// Force it
					living.removeEffect(message.effect);
					living.addEffect(message.instance);
				}
			}
		});
	}
	
	public static VanillaEffectSyncMessage decode(PacketBuffer buf) {
		final int entityID = buf.readVarInt();
		final int effectID = buf.readVarInt();
		final Effect effect = Effect.byId(effectID);
		if (effect == null) {
			throw new RuntimeException("Unrecognized effect in sync message: " + effectID);
		}
		
		final @Nullable EffectInstance instance;
		if (buf.readBoolean()) {
			final int duration = buf.readVarInt();
			final int amp = buf.readVarInt();
			instance = new EffectInstance(effect, duration, amp);
		} else {
			instance = null;
		}
		
		return new VanillaEffectSyncMessage(entityID, effect, instance);
	}

	public static void encode(VanillaEffectSyncMessage msg, PacketBuffer buf) {
		buf.writeVarInt(msg.entityID);
		buf.writeVarInt(Effect.getId(msg.effect));
		buf.writeBoolean(msg.instance != null);
		if (msg.instance != null) {
			buf.writeVarInt(msg.instance.getDuration());
			buf.writeVarInt(msg.instance.getAmplifier());
		}
	}

}
