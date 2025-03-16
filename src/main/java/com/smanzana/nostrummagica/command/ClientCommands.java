package com.smanzana.nostrummagica.command;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class ClientCommands {

	public static Map<String, BiFunction<Player, String, Boolean>> COMMANDS = new HashMap<>();

	public static void Register(String command, BiFunction<Player, String, Boolean> handler) {
		COMMANDS.put(command.toLowerCase(), handler);
	}
	
	@SubscribeEvent
	public static void onClientChat(ClientChatEvent event) {
		final String message = event.getOriginalMessage();
		if (message.length() > 1 && message.charAt(0) == '/') {
			// Try to find command name
			int spaceIdx = message.indexOf(" ");
			final String cmd;
			if (spaceIdx > 0) {
				cmd = message.substring(1, spaceIdx).toLowerCase();
			} else {
				cmd = message.substring(1).toLowerCase();
			}
			
			if (COMMANDS.containsKey(cmd)) {
				final Minecraft mc = Minecraft.getInstance();
				final Player player = mc.player;
				if (COMMANDS.get(cmd).apply(player, message)) {
					event.setCanceled(true);
				}
			}
		}
	}
	
}
