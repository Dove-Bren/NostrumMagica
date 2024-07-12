package com.smanzana.nostrummagica.network.message;

import java.util.UUID;
import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spell.SpellCharacteristics;
import com.smanzana.nostrummagica.spell.component.SpellShapeProperties;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;
import com.smanzana.nostrummagica.util.Entities;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.LivingEntity;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * @author Skyler
 *
 */
public class ClientShapeVfxRenderMessage {

	public static void handle(ClientShapeVfxRenderMessage message, Supplier<NetworkEvent.Context> ctx) {
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
			
			if (message.shape == null || message.properties == null || message.characteristics == null) {
				NostrumMagica.logger.warn("Malformed effect message");
				return;
			}
			
			NostrumMagica.instance.proxy.spawnSpellShapeVfx(NostrumMagica.instance.proxy.getPlayer().world, 
					message.shape, message.properties,
					caster, message.casterPos, target, message.targetPos,
					message.characteristics);
		});
	}

	private final UUID caster;
	private final Vector3d casterPos;
	private final UUID target;
	private final Vector3d targetPos;
	private final SpellShape shape;
	private final SpellShapeProperties properties;
	private final SpellCharacteristics characteristics;
	
	public ClientShapeVfxRenderMessage(
			LivingEntity caster, Vector3d casterPos,
			LivingEntity target, Vector3d targetPos,
			SpellShape shape,
			SpellShapeProperties properties,
			SpellCharacteristics characteristics) {
		this(caster == null ? null : caster.getUniqueID(), casterPos,
				target == null ? null : target.getUniqueID(), targetPos,
				shape, properties, characteristics);
	}
	
	public ClientShapeVfxRenderMessage(
			UUID caster, Vector3d casterPos,
			UUID target, Vector3d targetPos,
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

	public static ClientShapeVfxRenderMessage decode(PacketBuffer buf) {
		final UUID caster;
		final Vector3d casterPos;
		final UUID target;
		final Vector3d targetPos;
		final SpellShape shape;
		final SpellShapeProperties properties;
		final SpellCharacteristics characteristics;
		
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
		
		shape = SpellShape.get(buf.readString());
		if (shape != null) {
			properties = shape.getDefaultProperties().fromNBT(buf.readCompoundTag());
		} else {
			properties = new SpellShapeProperties();
		}
		
		characteristics = SpellCharacteristics.FromNBT(buf.readCompoundTag());
		
		return new ClientShapeVfxRenderMessage(
				caster, casterPos, target, targetPos,
				shape, properties, characteristics
				);
	}

	public static void encode(ClientShapeVfxRenderMessage msg, PacketBuffer buf) {
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
		
		buf.writeString(msg.shape.getShapeKey());
		buf.writeCompoundTag(msg.properties.toNBT());
		buf.writeCompoundTag(msg.characteristics.toNBT());
	}

}
