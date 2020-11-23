package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.nbt.NBTTagCompound;
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

			Minecraft.getMinecraft().addScheduledTask(() -> {
				EntityLivingBase caster, target;
				Vec3d casterPos, targetPos;
				SpellComponentWrapper component, flavor;
				
				caster = target = null;
				casterPos = targetPos = null;
				flavor = null;
				
				if (message.tag.hasKey(NBT_CASTER_ID, NBT.TAG_STRING)) {
					try {
						UUID id = UUID.fromString(message.tag.getString(NBT_CASTER_ID));
						caster = NostrumMagica.proxy.getPlayer().worldObj.getPlayerEntityByUUID(id);
					} catch (Exception e) {
						;
					}
				}
				
				if (message.tag.hasKey(NBT_CASTER_POS, NBT.TAG_COMPOUND)) {
					NBTTagCompound nbt = message.tag.getCompoundTag(NBT_CASTER_POS);
					casterPos = new Vec3d(
							nbt.getDouble("x"),
							nbt.getDouble("y"),
							nbt.getDouble("z")
							);
				}
				
				if (message.tag.hasKey(NBT_TARGET_ID, NBT.TAG_STRING)) {
					try {
						UUID id = UUID.fromString(message.tag.getString(NBT_TARGET_ID));
						for (Entity e : NostrumMagica.proxy.getPlayer().worldObj.loadedEntityList) {
							if (e.getPersistentID().equals(id)) {
								target = (EntityLivingBase) e;
								break;
							}
						}
					} catch (Exception e) {
						;
					}
				}
				
				if (message.tag.hasKey(NBT_TARGET_POS, NBT.TAG_COMPOUND)) {
					NBTTagCompound nbt = message.tag.getCompoundTag(NBT_TARGET_POS);
					targetPos = new Vec3d(
							nbt.getDouble("x"),
							nbt.getDouble("y"),
							nbt.getDouble("z")
							);
				}
				
				if (message.tag.hasKey(NBT_FLAVOR, NBT.TAG_STRING)) {
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
				
				NostrumMagica.proxy.spawnEffect(NostrumMagica.proxy.getPlayer().worldObj, 
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
	
	protected NBTTagCompound tag;
	
	public ClientEffectRenderMessage() {
		tag = new NBTTagCompound();
	}
	
	public ClientEffectRenderMessage(
			EntityLivingBase caster, Vec3d casterPos,
			EntityLivingBase target, Vec3d targetPos,
			SpellComponentWrapper component,
			SpellComponentWrapper flavor,
			boolean negative,
			float param) {
		tag = new NBTTagCompound();
		
		if (caster != null)
			tag.setString(NBT_CASTER_ID, caster.getPersistentID().toString());
		if (casterPos != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble("x", casterPos.xCoord);
			nbt.setDouble("y", casterPos.yCoord);
			nbt.setDouble("z", casterPos.zCoord);
			tag.setTag(NBT_CASTER_POS, nbt);
		}
			
		if (target != null)
			tag.setString(NBT_TARGET_ID, target.getPersistentID().toString());
		if (targetPos != null) {
			NBTTagCompound nbt = new NBTTagCompound();
			nbt.setDouble("x", targetPos.xCoord);
			nbt.setDouble("y", targetPos.yCoord);
			nbt.setDouble("z", targetPos.zCoord);
			tag.setTag(NBT_TARGET_POS, nbt);
		}
		
		tag.setString(NBT_COMPONENT, component.getKeyString());
		if (flavor != null)
			tag.setString(NBT_FLAVOR, flavor.getKeyString());
		
		tag.setBoolean(NBT_NEGATIVE, negative);
		tag.setFloat(NBT_PARAM, param);
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
