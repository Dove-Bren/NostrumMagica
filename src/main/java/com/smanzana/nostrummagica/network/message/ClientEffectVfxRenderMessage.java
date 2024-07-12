package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server is broadcasting a spell vfx
 * @author Skyler
 *
 */
public class ClientEffectVfxRenderMessage {

	public static void handle(ClientEffectVfxRenderMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().runAsync(() -> {
			final World world = NostrumMagica.instance.proxy.getPlayer().world;
			LivingEntity caster, target;
			caster = target = null;
			
			if (message.caster != null) {
				caster = world.getPlayerByUuid(message.caster);
			}
			
			if (message.target != null) {
				target = Entities.FindLiving(world, message.target);
			}
			
			if (message.effect == null) {
				NostrumMagica.logger.warn("Malformed effect vfx message");
				return;
			}
			
			NostrumMagica.instance.proxy.spawnSpellEffectVfx(NostrumMagica.instance.proxy.getPlayer().world, 
					message.effect,
					caster, message.casterPos, target, message.targetPos);
		});
	}

	private final UUID caster;
	private final Vector3d casterPos;
	private final UUID target;
	private final Vector3d targetPos;
	private final SpellEffectPart effect;
	
	public ClientEffectVfxRenderMessage(
			LivingEntity caster, Vector3d casterPos,
			LivingEntity target, Vector3d targetPos,
			SpellEffectPart effect) {
		this(caster == null ? null : caster.getUniqueID(), casterPos,
				target == null ? null : target.getUniqueID(), targetPos,
				effect);
	}
	
	public ClientEffectVfxRenderMessage(
			UUID caster, Vector3d casterPos,
			UUID target, Vector3d targetPos,
			SpellEffectPart effect) {
		this.caster = caster;
		this.casterPos = casterPos;
		this.target = target;
		this.targetPos = targetPos;
		this.effect = effect;
	}

	public static ClientEffectVfxRenderMessage decode(PacketBuffer buf) {
		final UUID caster;
		final Vector3d casterPos;
		final UUID target;
		final Vector3d targetPos;
		final SpellEffectPart effect;
		
		if (buf.readBoolean()) {
			caster = buf.readUniqueId();
		} else {
			caster = null;
		}
		
		if (buf.readBoolean()) {
			casterPos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			casterPos = null;
		}
		
		if (buf.readBoolean()) {
			target = buf.readUniqueId();
		} else {
			target = null;
		}
		
		if (buf.readBoolean()) {
			targetPos = new Vector3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			targetPos = null;
		}
		
		effect = SpellEffectPart.FromNBT(buf.readCompoundTag());
		
		return new ClientEffectVfxRenderMessage(
				caster, casterPos, target, targetPos,
				effect
				);
	}

	public static void encode(ClientEffectVfxRenderMessage msg, PacketBuffer buf) {
		buf.writeBoolean(msg.caster != null);
		if (msg.caster != null) {
			buf.writeUniqueId(msg.caster);
		}
		
		buf.writeBoolean(msg.casterPos != null);
		if (msg.casterPos != null) {
			buf.writeDouble(msg.casterPos.x);
			buf.writeDouble(msg.casterPos.y);
			buf.writeDouble(msg.casterPos.z);
		}
		
		buf.writeBoolean(msg.target != null);
		if (msg.target != null) {
			buf.writeUniqueId(msg.target);
		}
		
		buf.writeBoolean(msg.targetPos != null);
		if (msg.targetPos != null) {
			buf.writeDouble(msg.targetPos.x);
			buf.writeDouble(msg.targetPos.y);
			buf.writeDouble(msg.targetPos.z);
		}
		
		buf.writeCompoundTag(msg.effect.toNBT(null));
	}

}
