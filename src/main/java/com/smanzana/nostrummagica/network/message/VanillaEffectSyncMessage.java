package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.network.NetworkEvent;

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
	public final @Nonnull MobEffect effect;
	public final @Nullable MobEffectInstance instance;
	
	private VanillaEffectSyncMessage(int entityID, MobEffect effect, @Nullable MobEffectInstance instance) {
		this.entityID = entityID;
		this.effect = effect;
		this.instance = instance;
		
		if (instance != null && instance.getEffect() != effect) {
			throw new RuntimeException("Effect and instance effect do not match.");
		}
	}

	public VanillaEffectSyncMessage(int entityID, @Nonnull MobEffectInstance effect) {
		this(entityID, effect.getEffect(), effect);
	}

	public VanillaEffectSyncMessage(Entity entity, @Nonnull MobEffectInstance effect) {
		this(entity.getId(), effect.getEffect(), effect);
	}
	
	public VanillaEffectSyncMessage(int entityID, @Nonnull MobEffect effect) {
		this(entityID, effect, null);
	}
	
	public VanillaEffectSyncMessage(Entity entity, @Nonnull MobEffect effect) {
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
	
	public static VanillaEffectSyncMessage decode(FriendlyByteBuf buf) {
		final int entityID = buf.readVarInt();
		final int effectID = buf.readVarInt();
		final MobEffect effect = MobEffect.byId(effectID);
		if (effect == null) {
			throw new RuntimeException("Unrecognized effect in sync message: " + effectID);
		}
		
		final @Nullable MobEffectInstance instance;
		if (buf.readBoolean()) {
			final int duration = buf.readVarInt();
			final int amp = buf.readVarInt();
			instance = new MobEffectInstance(effect, duration, amp);
		} else {
			instance = null;
		}
		
		return new VanillaEffectSyncMessage(entityID, effect, instance);
	}

	public static void encode(VanillaEffectSyncMessage msg, FriendlyByteBuf buf) {
		buf.writeVarInt(msg.entityID);
		buf.writeVarInt(MobEffect.getId(msg.effect));
		buf.writeBoolean(msg.instance != null);
		if (msg.instance != null) {
			buf.writeVarInt(msg.instance.getDuration());
			buf.writeVarInt(msg.instance.getAmplifier());
		}
	}

}
