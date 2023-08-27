package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server has processed spell cast request and sent back
 * the status (as well as final mana)
 * @author Skyler
 *
 */
public class ClientEffectRenderMessage implements IMessage {

	public static class Handler implements IMessageHandler<ClientEffectRenderMessage, IMessage> {

		@Override
		public IMessage onMessage(ClientEffectRenderMessage message, MessageContext ctx) {

			Minecraft.getInstance().runAsync(() -> {
				LivingEntity caster, target;
				Vec3d casterPos, targetPos;
				SpellComponentWrapper component, flavor;
				
				caster = target = null;
				casterPos = targetPos = null;
				flavor = null;
				
				if (message.tag.contains(NBT_CASTER_ID, NBT.TAG_STRING)) {
					try {
						UUID id = UUID.fromString(message.tag.getString(NBT_CASTER_ID));
						caster = NostrumMagica.proxy.getPlayer().world.getPlayerEntityByUUID(id);
					} catch (Exception e) {
						;
					}
				}
				
				if (message.tag.contains(NBT_CASTER_POS, NBT.TAG_COMPOUND)) {
					CompoundNBT nbt = message.tag.getCompound(NBT_CASTER_POS);
					casterPos = new Vec3d(
							nbt.getDouble("x"),
							nbt.getDouble("y"),
							nbt.getDouble("z")
							);
				}
				
				if (message.tag.contains(NBT_TARGET_ID, NBT.TAG_STRING)) {
					try {
						UUID id = UUID.fromString(message.tag.getString(NBT_TARGET_ID));
						for (Entity e : NostrumMagica.proxy.getPlayer().world.loadedEntityList) {
							if (e.getPersistentID().equals(id)) {
								target = (LivingEntity) e;
								break;
							}
						}
					} catch (Exception e) {
						;
					}
				}
				
				if (message.tag.contains(NBT_TARGET_POS, NBT.TAG_COMPOUND)) {
					CompoundNBT nbt = message.tag.getCompound(NBT_TARGET_POS);
					targetPos = new Vec3d(
							nbt.getDouble("x"),
							nbt.getDouble("y"),
							nbt.getDouble("z")
							);
				}
				
				if (message.tag.contains(NBT_FLAVOR, NBT.TAG_STRING)) {
					String key = message.tag.getString(NBT_FLAVOR);
					flavor = SpellComponentWrapper.fromKeyString(key);
				}
				
				component = SpellComponentWrapper.fromKeyString(message.tag.getString(NBT_COMPONENT));
				
				if (component == null) {
					NostrumMagica.logger.warn("Malformed effect message");
					return;
				}
				
				final boolean negative = message.tag.getBoolean(NBT_NEGATIVE);
				final float param = message.tag.getFloat(NBT_PARAM);
				
				NostrumMagica.proxy.spawnEffect(NostrumMagica.proxy.getPlayer().world, 
						component,
						caster, casterPos, target, targetPos, flavor, negative, param);
			});

			return null;
		}
		
	}

	private static final String NBT_CASTER_ID = "caster_id";
	private static final String NBT_CASTER_POS = "caster_pos";
	private static final String NBT_TARGET_ID = "target_id";
	private static final String NBT_TARGET_POS = "target_pos";
	
	private static final String NBT_COMPONENT = "comp";
	private static final String NBT_FLAVOR = "flavor";
	private static final String NBT_NEGATIVE = "negative";
	private static final String NBT_PARAM = "param";
	
	protected CompoundNBT tag;
	
	public ClientEffectRenderMessage() {
		tag = new CompoundNBT();
	}
	
	public ClientEffectRenderMessage(
			LivingEntity caster, Vec3d casterPos,
			LivingEntity target, Vec3d targetPos,
			SpellComponentWrapper component,
			SpellComponentWrapper flavor,
			boolean negative,
			float param) {
		tag = new CompoundNBT();
		
		if (caster != null)
			tag.putString(NBT_CASTER_ID, caster.getPersistentID().toString());
		if (casterPos != null) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.setDouble("x", casterPos.x);
			nbt.setDouble("y", casterPos.y);
			nbt.setDouble("z", casterPos.z);
			tag.put(NBT_CASTER_POS, nbt);
		}
			
		if (target != null)
			tag.putString(NBT_TARGET_ID, target.getPersistentID().toString());
		if (targetPos != null) {
			CompoundNBT nbt = new CompoundNBT();
			nbt.setDouble("x", targetPos.x);
			nbt.setDouble("y", targetPos.y);
			nbt.setDouble("z", targetPos.z);
			tag.put(NBT_TARGET_POS, nbt);
		}
		
		tag.putString(NBT_COMPONENT, component.getKeyString());
		if (flavor != null)
			tag.putString(NBT_FLAVOR, flavor.getKeyString());
		
		tag.putBoolean(NBT_NEGATIVE, negative);
		tag.putFloat(NBT_PARAM, param);
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		tag = ByteBufUtils.readTag(buf);
	}

	@Override
	public void toBytes(ByteBuf buf) {
		ByteBufUtils.writeTag(buf, tag);
	}

}
