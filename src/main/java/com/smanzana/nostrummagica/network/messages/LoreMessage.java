package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import io.netty.buffer.ByteBuf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

/**
 * Sent to client to let them know they've unlocked new lore
 * Comes with a fresh copy of attributes
 * @author Skyler
 *
 */
public class LoreMessage implements IMessage {

	public static class Handler implements IMessageHandler<LoreMessage, IMessage> {

		@Override
		public IMessage onMessage(LoreMessage message, MessageContext ctx) {
			//update local attributes
			
			INostrumMagic override = CAPABILITY.getDefaultInstance();
			CAPABILITY.getStorage().readNBT(CAPABILITY, override, null, message.tag.getTag(NBT_ATTRIBUTES));
			NostrumMagica.proxy.receiveStatOverrides(override);
			
			ILoreTagged lore = LoreRegistry.instance().lookup(message.tag.getString(NBT_LORE_KEY));
			String name = lore.getLoreDisplayName();
			EntityPlayer player = NostrumMagica.proxy.getPlayer();
			player.addChatMessage(new TextComponentTranslation("info.lore.get", name));
			NostrumMagicaSounds.UI_TICK.play(player, player.worldObj, player.posX, player.posY, player.posZ);
			
			return null;
		}
		
	}
	
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	private static final String NBT_LORE_KEY = "lore_key";
	private static final String NBT_ATTRIBUTES = "attr";
	
	protected NBTTagCompound tag;
	
	public LoreMessage() {
		tag = new NBTTagCompound();
	}
	
	public LoreMessage(ILoreTagged lore, INostrumMagic stats) {
		tag = new NBTTagCompound();
		tag.setTag(NBT_ATTRIBUTES, (NBTTagCompound) CAPABILITY.getStorage().writeNBT(CAPABILITY, stats, null));
		tag.setString(NBT_LORE_KEY, lore.getLoreKey());
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
