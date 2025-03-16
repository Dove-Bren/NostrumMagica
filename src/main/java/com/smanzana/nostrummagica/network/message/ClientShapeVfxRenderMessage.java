package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * @author Skyler
 *
 */
public class ClientShapeVfxRenderMessage {

	public static void handle(ClientShapeVfxRenderMessage message, Supplier<NetworkEvent.Context> ctx) {
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			final Level world = NostrumMagica.instance.proxy.getPlayer().level;
			LivingEntity caster, target;
			caster = target = null;
			
			if (message.caster != null) {
				caster = world.getPlayerByUUID(message.caster);
			}
			
			if (message.target != null) {
				target = Entities.FindLiving(world, message.target);
			}
			
			if (message.shape == null || message.properties == null || message.characteristics == null) {
				NostrumMagica.logger.warn("Malformed effect message");
				return;
			}
			
			NostrumMagica.instance.proxy.spawnSpellShapeVfx(NostrumMagica.instance.proxy.getPlayer().level, 
					message.shape, message.properties,
					caster, message.casterPos, target, message.targetPos,
					message.characteristics);
		});
	}

	private final UUID caster;
	private final Vec3 casterPos;
	private final UUID target;
	private final Vec3 targetPos;
	private final SpellShape shape;
	private final SpellShapeProperties properties;
	private final SpellCharacteristics characteristics;
	
	public ClientShapeVfxRenderMessage(
			LivingEntity caster, Vec3 casterPos,
			LivingEntity target, Vec3 targetPos,
			SpellShape shape,
			SpellShapeProperties properties,
			SpellCharacteristics characteristics) {
		this(caster == null ? null : caster.getUUID(), casterPos,
				target == null ? null : target.getUUID(), targetPos,
				shape, properties, characteristics);
	}
	
	public ClientShapeVfxRenderMessage(
			UUID caster, Vec3 casterPos,
			UUID target, Vec3 targetPos,
			SpellShape shape,
			SpellShapeProperties properties,
			SpellCharacteristics characteristics) {
		this.caster = caster;
		this.casterPos = casterPos;
		this.target = target;
		this.targetPos = targetPos;
		this.shape = shape;
		this.properties = properties;
		this.characteristics = characteristics;
	}

	public static ClientShapeVfxRenderMessage decode(FriendlyByteBuf buf) {
		final UUID caster;
		final Vec3 casterPos;
		final UUID target;
		final Vec3 targetPos;
		final SpellShape shape;
		final SpellShapeProperties properties;
		final SpellCharacteristics characteristics;
		
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
		
		shape = SpellShape.get(buf.readUtf());
		if (shape != null) {
			properties = shape.getDefaultProperties().fromNBT(buf.readNbt());
		} else {
			properties = new SpellShapeProperties();
		}
		
		characteristics = SpellCharacteristics.FromNBT(buf.readNbt());
		
		return new ClientShapeVfxRenderMessage(
				caster, casterPos, target, targetPos,
				shape, properties, characteristics
				);
	}

	public static void encode(ClientShapeVfxRenderMessage msg, FriendlyByteBuf buf) {
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
		
		buf.writeUtf(msg.shape.getShapeKey());
		buf.writeNbt(msg.properties.toNBT());
		buf.writeNbt(msg.characteristics.toNBT());
	}

}
