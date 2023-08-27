package com.smanzana.nostrummagica.network.messages;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.command.CommandInfoScreenGoto;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
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
			
			Minecraft.getInstance().runAsync(() -> {
				NostrumMagica.proxy.receiveStatOverrides(override);
				
				ILoreTagged lore = LoreRegistry.instance().lookup(message.tag.getString(NBT_LORE_KEY));
				if (lore == null) {
					NostrumMagica.logger.warn("Tried to award lore with key " + message.tag.getString(NBT_LORE_KEY)
							+ ", but that lore is not registered!");
					// Register it with LoreRegistry.register
				} else {
					String name = lore.getLoreDisplayName();
					PlayerEntity player = NostrumMagica.proxy.getPlayer();
					ITextComponent comp = new TranslationTextComponent("info.lore.get", name);
					Style style = new Style()
							.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("info.screen.goto")))
							.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, CommandInfoScreenGoto.Command + " " + ILoreTagged.GetInfoKey(lore)));
					comp = comp.setStyle(style);
					
					player.sendMessage(comp);
					NostrumMagicaSounds.LORE.play(player, player.world, player.posX, player.posY, player.posZ);
				}
			});
			
			return null;
		}
		
	}
	
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	private static final String NBT_LORE_KEY = "lore_key";
	private static final String NBT_ATTRIBUTES = "attr";
	
	protected CompoundNBT tag;
	
	public LoreMessage() {
		tag = new CompoundNBT();
	}
	
	public LoreMessage(ILoreTagged lore, INostrumMagic stats) {
		tag = new CompoundNBT();
		tag.put(NBT_ATTRIBUTES, (CompoundNBT) CAPABILITY.getStorage().writeNBT(CAPABILITY, stats, null));
		tag.putString(NBT_LORE_KEY, lore.getLoreKey());
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
