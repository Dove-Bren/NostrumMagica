package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.command.CommandInfoScreenGoto;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import io.netty.handler.codec.DecoderException;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Util;
import net.minecraft.util.text.Color;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

/**
 * Sent to client to let them know they've unlocked new lore
 * Comes with a fresh copy of attributes
 * @author Skyler
 *
 */
public class LoreMessage {

	public static void handle(LoreMessage message, Supplier<NetworkEvent.Context> ctx) {
		//update local attributes
		ctx.get().setPacketHandled(true);
		Minecraft.getInstance().submit(() -> {
			NostrumMagica.instance.proxy.receiveStatOverrides(message.stats);
			
			StringTextComponent loreName = new StringTextComponent("[" + message.lore.getLoreDisplayName() + "]");
			Style style = Style.EMPTY
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslationTextComponent("info.screen.goto")))
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + CommandInfoScreenGoto.Command + " \"" + ILoreTagged.GetInfoKey(message.lore) + "\""))
					.withColor(Color.fromLegacyFormat(TextFormatting.LIGHT_PURPLE))
				;
					
			loreName.setStyle(style);
			
			PlayerEntity player = NostrumMagica.instance.proxy.getPlayer();
			IFormattableTextComponent comp = new TranslationTextComponent("info.lore.get", loreName);
			
			player.sendMessage(comp, Util.NIL_UUID);
			NostrumMagicaSounds.LORE.play(player, player.level, player.getX(), player.getY(), player.getZ());
		});
	}
	
	@CapabilityInject(INostrumMagic.class)
	public static Capability<INostrumMagic> CAPABILITY = null;
	
	private final ILoreTagged lore;
	private final INostrumMagic stats;
	
	public LoreMessage(ILoreTagged lore, INostrumMagic stats) {
		this.lore = lore;
		this.stats = stats;
	}

	public static LoreMessage decode(PacketBuffer buf) {
		INostrumMagic stats = CAPABILITY.getDefaultInstance();
		CAPABILITY.getStorage().readNBT(CAPABILITY, stats, null, buf.readNbt());
		
		final String loreID = buf.readUtf(32767);
		ILoreTagged lore = LoreRegistry.instance().lookup(loreID);
		if (lore == null) {
			throw new DecoderException("Failed to find lore based on " + loreID);
		}
		
		return new LoreMessage(lore, stats);
	}

	public static void encode(LoreMessage msg, PacketBuffer buf) {
		buf.writeNbt((CompoundNBT) CAPABILITY.getStorage().writeNBT(CAPABILITY, msg.stats, null));
		buf.writeUtf(msg.lore.getLoreKey());
	}

}
