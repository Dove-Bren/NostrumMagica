package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

/**
 * Server is broadcasting a spell vfx
 * @author Skyler
 *
 */
public class ClientEffectVfxRenderMessage {

	public static void handle(ClientEffectVfxRenderMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			final Level world = NostrumMagica.Proxy.getPlayer().level;
			LivingEntity caster, target;
			caster = target = null;
			
			if (message.caster != null) {
				caster = world.getPlayerByUUID(message.caster);
			}
			
			if (message.target != null) {
				target = Entities.FindLiving(world, message.target);
			}
			
			if (message.effect == null) {
				NostrumMagica.logger.warn("Malformed effect vfx message");
				return;
			}
			
			NostrumMagica.Proxy.spawnSpellEffectVfx(NostrumMagica.Proxy.getPlayer().level, 
					message.effect,
					caster, message.casterPos, target, message.targetPos);
		});
	}

	private final UUID caster;
	private final Vec3 casterPos;
	private final UUID target;
	private final Vec3 targetPos;
	private final SpellEffectPart effect;
	
	public ClientEffectVfxRenderMessage(
			LivingEntity caster, Vec3 casterPos,
			LivingEntity target, Vec3 targetPos,
			SpellEffectPart effect) {
		this(caster == null ? null : caster.getUUID(), casterPos,
				target == null ? null : target.getUUID(), targetPos,
				effect);
	}
	
	public ClientEffectVfxRenderMessage(
			UUID caster, Vec3 casterPos,
			UUID target, Vec3 targetPos,
			SpellEffectPart effect) {
		this.caster = caster;
		this.casterPos = casterPos;
		this.target = target;
		this.targetPos = targetPos;
		this.effect = effect;
	}

	public static ClientEffectVfxRenderMessage decode(FriendlyByteBuf buf) {
		final UUID caster;
		final Vec3 casterPos;
		final UUID target;
		final Vec3 targetPos;
		final SpellEffectPart effect;
		
		if (buf.readBoolean()) {
			caster = buf.readUUID();
		} else {
			caster = null;
		}
		
		if (buf.readBoolean()) {
			casterPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			casterPos = null;
		}
		
		if (buf.readBoolean()) {
			target = buf.readUUID();
		} else {
			target = null;
		}
		
		if (buf.readBoolean()) {
			targetPos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			targetPos = null;
		}
		
		effect = SpellEffectPart.FromNBT(buf.readNbt());
		
		return new ClientEffectVfxRenderMessage(
				caster, casterPos, target, targetPos,
				effect
				);
	}

	public static void encode(ClientEffectVfxRenderMessage msg, FriendlyByteBuf buf) {
		buf.writeBoolean(msg.caster != null);
		if (msg.caster != null) {
			buf.writeUUID(msg.caster);
		}
		
		buf.writeBoolean(msg.casterPos != null);
		if (msg.casterPos != null) {
			buf.writeDouble(msg.casterPos.x);
			buf.writeDouble(msg.casterPos.y);
			buf.writeDouble(msg.casterPos.z);
		}
		
		buf.writeBoolean(msg.target != null);
		if (msg.target != null) {
			buf.writeUUID(msg.target);
		}
		
		buf.writeBoolean(msg.targetPos != null);
		if (msg.targetPos != null) {
			buf.writeDouble(msg.targetPos.x);
			buf.writeDouble(msg.targetPos.y);
			buf.writeDouble(msg.targetPos.z);
		}
		
		buf.writeNbt(msg.effect.toNBT(null));
	}

}
