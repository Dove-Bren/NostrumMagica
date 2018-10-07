package com.smanzana.nostrummagica.network.messages;

import java.util.UUID;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Server is refreshing client's view on a player's mana
 * @author Skyler
 *
 */
public class ManaMessage implements IMessage {

	public static class Handler implements IMessageHandler<ManaMessage, IMessage> {

		@Override
		public IMessage onMessage(ManaMessage message, MessageContext ctx) {
			
			NostrumMagica.proxy.applyOverride();
			
			UUID id;
			try {
				id = UUID.fromString(message.tag.getString(NBT_UUID));
			} catch (IllegalArgumentException e) {
				System.out.println("x");
				return null; // Just drop it
			}
			
			int mana = message.tag.getInteger(NBT_MANA);
			
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			
			if (player == null) {
				// Haven't finished loading. Just drop it
				return null;
			}
			
			player = player.world.getPlayerEntityByUUID(id);
			
			if (player == null) {
				// Not in this world. Who cares
				return null;
			}
			
			INostrumMagic att = NostrumMagica.getMagicWrapper(player);
			// Regardless of success, server has synced mana with us.
			
			if (att != null)
				att.setMana(mana);
			
			// Success or nah?
			
			// TODO care
			return null;
		}
		
	}

	private static final String NBT_UUID = "uuid";
	private static final String NBT_MANA = "mana";
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	protected NBTTagCompound tag;
	
	public ManaMessage() {
		tag = new NBTTagCompound();
	}
	
	public ManaMessage(EntityPlayer player, int mana) {
		tag = new NBTTagCompound();
		
		tag.setInteger(NBT_MANA, mana);
		tag.setString(NBT_UUID, player.getPersistentID().toString());
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
