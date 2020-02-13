package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import io.netty.buffer.ByteBuf;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is updating client's local value for a special effect
 * @author Skyler
 *
 */
public class MagicEffectUpdate implements IMessage {

	public static class Handler implements IMessageHandler<MagicEffectUpdate, IMessage> {

		@Override
		public IMessage onMessage(MagicEffectUpdate message, MessageContext ctx) {
			
			String typeName = message.tag.getString(NBT_TYPE);
			NBTTagCompound tag = message.tag.getCompoundTag(NBT_DATA);
			EffectData data = tag == null ? null : EffectData.fromNBT(message.tag.getCompoundTag(NBT_DATA));
			try {
				SpecialEffect type = SpecialEffect.valueOf(typeName);
				NostrumMagica.magicEffectProxy.setOverride(type, data);
			} catch (Exception e) {
				NostrumMagica.logger.warn("Failed to apply special effect handler");
			}
			
			return null;
		}
		
	}

	private static final String NBT_TYPE = "type";
	private static final String NBT_DATA = "data";
	
	protected NBTTagCompound tag;
	
	public MagicEffectUpdate() {
		tag = new NBTTagCompound();
	}
	
	public MagicEffectUpdate(SpecialEffect type, EffectData data) {
		tag = new NBTTagCompound();
		
		tag.setString(NBT_TYPE, type.name());
		if (data != null) {
			tag.setTag(NBT_DATA, data.toNBT());
		}
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
