package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.utils.Entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * @author Skyler
 *
 */
public class ClientEffectRenderMessage {

	public static void handle(ClientEffectRenderMessage message, Supplier<NetworkEvent.Context> ctx) {
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
			
			if (message.component == null) {
				NostrumMagica.logger.warn("Malformed effect message");
				return;
			}
			
			NostrumMagica.instance.proxy.spawnEffect(NostrumMagica.instance.proxy.getPlayer().world, 
					message.component,
					caster, message.casterPos, target, message.targetPos,
					message.flavor, message.negative, message.param);
		});
	}

	private final UUID caster;
	private final Vec3d casterPos;
	private final UUID target;
	private final Vec3d targetPos;
	private final SpellComponentWrapper component;
	private final SpellComponentWrapper flavor;
	private final boolean negative;
	private final float param;
	
	public ClientEffectRenderMessage(
			LivingEntity caster, Vec3d casterPos,
			LivingEntity target, Vec3d targetPos,
			SpellComponentWrapper component,
			SpellComponentWrapper flavor,
			boolean negative,
			float param) {
		this(caster == null ? null : caster.getUniqueID(), casterPos,
				target == null ? null : target.getUniqueID(), targetPos,
				component, flavor, negative, param);
	}
	
	public ClientEffectRenderMessage(
			UUID caster, Vec3d casterPos,
			UUID target, Vec3d targetPos,
			SpellComponentWrapper component,
			SpellComponentWrapper flavor,
			boolean negative,
			float param) {
		this.caster = caster;
		this.casterPos = casterPos;
		this.target = target;
		this.targetPos = targetPos;
		this.component = component;
		this.flavor = flavor;
		this.negative = negative;
		this.param = param;
	}

	public static ClientEffectRenderMessage decode(PacketBuffer buf) {
		final UUID caster;
		final Vec3d casterPos;
		final UUID target;
		final Vec3d targetPos;
		final SpellComponentWrapper component;
		final SpellComponentWrapper flavor;
		final boolean negative;
		final float param;
		
		if (buf.readBoolean()) {
			caster = buf.readUniqueId();
		} else {
			caster = null;
		}
		
		if (buf.readBoolean()) {
			casterPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			casterPos = null;
		}
		
		if (buf.readBoolean()) {
			target = buf.readUniqueId();
		} else {
			target = null;
		}
		
		if (buf.readBoolean()) {
			targetPos = new Vec3d(buf.readDouble(), buf.readDouble(), buf.readDouble());
		} else {
			targetPos = null;
		}
		
		component = SpellComponentWrapper.fromKeyString(buf.readString(32767));
		
		if (buf.readBoolean()) {
			flavor = SpellComponentWrapper.fromKeyString(buf.readString(32767));
		} else {
			flavor = null;
		}
		
		negative = buf.readBoolean();
		param = buf.readFloat();
		
		return new ClientEffectRenderMessage(
				caster, casterPos, target, targetPos,
				component, flavor, negative, param
				);
	}

	public static void encode(ClientEffectRenderMessage msg, PacketBuffer buf) {
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
		
		buf.writeString(msg.component.getKeyString());
		
		buf.writeBoolean(msg.flavor != null);
		if (msg.flavor != null) {
			buf.writeString(msg.flavor.getKeyString());
		}
		
		buf.writeBoolean(msg.negative);
		buf.writeFloat(msg.param);
	}

}
