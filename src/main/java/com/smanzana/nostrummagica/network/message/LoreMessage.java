package com.smanzana.nostrummagica.network.message;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.NostrumMagic;
import com.smanzana.nostrummagica.command.CommandInfoScreenGoto;
import com.smanzana.nostrummagica.loretag.ILoreTagged;
import com.smanzana.nostrummagica.loretag.LoreRegistry;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import io.netty.handler.codec.DecoderException;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.fmllegacy.network.NetworkEvent;

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
			
			TextComponent loreName = new TextComponent("[" + message.lore.getLoreDisplayName() + "]");
			Style style = Style.EMPTY
					.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TranslatableComponent("info.screen.goto")))
					.withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + CommandInfoScreenGoto.Command + " \"" + ILoreTagged.GetInfoKey(message.lore) + "\""))
					.withColor(TextColor.fromLegacyFormat(ChatFormatting.LIGHT_PURPLE))
				;
					
			loreName.setStyle(style);
			
			Player player = NostrumMagica.instance.proxy.getPlayer();
			MutableComponent comp = new TranslatableComponent("info.lore.get", loreName);
			
			player.sendMessage(comp, Util.NIL_UUID);
			NostrumMagicaSounds.LORE.play(player, player.level, player.getX(), player.getY(), player.getZ());
		});
	}
	
	private final ILoreTagged lore;
	private final INostrumMagic stats;
	
	public LoreMessage(ILoreTagged lore, INostrumMagic stats) {
		this.lore = lore;
		this.stats = stats;
	}

	public static LoreMessage decode(FriendlyByteBuf buf) {
		NostrumMagic stats = new NostrumMagic(null);
		stats.deserializeNBT(buf.readNbt());
		
		final String loreID = buf.readUtf(32767);
		ILoreTagged lore = LoreRegistry.instance().lookup(loreID);
		if (lore == null) {
			throw new DecoderException("Failed to find lore based on " + loreID);
		}
		
		return new LoreMessage(lore, stats);
	}

	public static void encode(LoreMessage msg, FriendlyByteBuf buf) {
		buf.writeNbt(msg.stats.serializeNBT());
		buf.writeUtf(msg.lore.getLoreKey());
	}

}
