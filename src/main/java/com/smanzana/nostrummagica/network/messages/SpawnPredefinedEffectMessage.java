package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect;
import com.smanzana.nostrummagica.client.effects.ClientPredefinedEffect.PredefinedEffect;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.util.Constants.NBT;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is signalling that a ritual has been performed, and the effects should be shown.
 * Perhaps this should be more generic
 * @author Skyler
 *
 */
public class SpawnPredefinedEffectMessage implements IMessage {

	public static class Handler implements IMessageHandler<SpawnPredefinedEffectMessage, IMessage> {

		@Override
		public IMessage onMessage(SpawnPredefinedEffectMessage message, MessageContext ctx) {
			
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			if (player.dimension != message.dimension) {
				return null;
			}
			
			Entity ent = player.world.getEntityByID(message.entityID);
			Vec3d offset = (ent != null ? Vec3d.ZERO : message.position);
			
			if (offset == null) {
				offset = Vec3d.ZERO;
			}
			
			ClientPredefinedEffect.Spawn(offset, message.type, message.duration, ent);
			
			return null;
		}
		
	}

	private static final String NBT_TYPE = "type";
	private static final String NBT_DIMENSION_ID = "dim";
	private static final String NBT_POS = "pos";
	private static final String NBT_DURATION = "duration";
	private static final String NBT_ENTITY = "entityID";

	protected PredefinedEffect type;
	protected int dimension;
	protected Vec3d position;
	protected int entityID;
	protected int duration;
	
	public SpawnPredefinedEffectMessage() {
		this(PredefinedEffect.SOUL_DAGGER_STAB, 20, 1, Vec3d.ZERO);
	}
	
	public SpawnPredefinedEffectMessage(PredefinedEffect type, int duration, int dimensionID, Vec3d position) {
		this.type = type;
		this.duration = duration;
		this.dimension = dimensionID;
		this.position = position;
		this.entityID = 0;
	}
	
	public SpawnPredefinedEffectMessage(PredefinedEffect type, int duration, int dimensionID, int entityID) {
		this.type = type;
		this.duration = duration;
		this.dimension = dimensionID;
		this.entityID = entityID;
		this.position = null;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		NBTTagCompound tag = ByteBufUtils.readTag(buf);
		try {
			this.type = PredefinedEffect.valueOf(tag.getString(NBT_TYPE).toUpperCase()); 
		} catch (Exception e) {
			e.printStackTrace();
			this.type = PredefinedEffect.SOUL_DAGGER_STAB;
		}
		
		this.duration = tag.getInteger(NBT_DURATION);
		this.dimension = tag.getInteger(NBT_DIMENSION_ID);
		if (tag.hasKey(NBT_POS, NBT.TAG_COMPOUND)) {
			this.entityID = 0;
			final NBTTagCompound subtag = tag.getCompoundTag(NBT_POS);
			final double x = subtag.getDouble("x");
			final double y = subtag.getDouble("y");
			final double z = subtag.getDouble("z");
			this.position = new Vec3d(x, y, z);
		} else {
			this.position = null;
			this.entityID = tag.getInteger(NBT_ENTITY);
		}
	}

	@Override
	public void toBytes(ByteBuf buf) {
		NBTTagCompound tag = new NBTTagCompound();
		
		tag.setString(NBT_TYPE, type.name());
		tag.setInteger(NBT_DIMENSION_ID, dimension);
		tag.setInteger(NBT_DURATION, duration);
		if (this.position != null) {
			NBTTagCompound subtag = new NBTTagCompound();
			subtag.setDouble("x", position.x);
			subtag.setDouble("y", position.y);
			subtag.setDouble("z", position.z);
			tag.setTag(NBT_POS, subtag);
		} else {
			tag.setInteger(NBT_ENTITY, entityID);
		}
		
		ByteBufUtils.writeTag(buf, tag);
	}

}
