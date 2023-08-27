package com.smanzana.nostrummagica.network.messages;


import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is sending a new copy of the mana armor capability
 * @author Skyler
 *
 */
public class ManaArmorSyncMessage implements IMessage {

	public static class Handler implements IMessageHandler<ManaArmorSyncMessage, IMessage> {

		@Override
		public IMessage onMessage(ManaArmorSyncMessage message, MessageContext ctx) {
			//update local attributes
			
			NostrumMagica.logger.info("Recieved Mana Armor sync message from server");
			
			IManaArmor override = CAPABILITY.getDefaultInstance();
			CAPABILITY.getStorage().readNBT(CAPABILITY, override, null, message.tag.getTag(NBT_CAP_DATA));
			
			final int entID = message.tag.getInt(NBT_ENT_ID);
			
			Minecraft.getInstance().runAsync(() -> {
				@Nullable Entity ent = Minecraft.getInstance().player.getEntityWorld().getEntityByID(entID);
				if (ent != null) {
					NostrumMagica.proxy.receiveManaArmorOverride(ent, override);
				}
			});
			
			
			return null;
		}
		
	}
	
	private static final String NBT_CAP_DATA = "capability";
	private static final String NBT_ENT_ID = "ent_id";
	
	@CapabilityInject(IManaArmor.class)
	public static Capability<IManaArmor> CAPABILITY = null;
	
	protected CompoundNBT tag;
	
	public ManaArmorSyncMessage() {
		tag = new CompoundNBT();
	}
	
	public ManaArmorSyncMessage(Entity ent, IManaArmor stats) {
		this();
		
		tag.put(NBT_CAP_DATA, CAPABILITY.getStorage().writeNBT(CAPABILITY, stats, null));
		tag.putInt(NBT_ENT_ID, ent.getEntityId());
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
