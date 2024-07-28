package com.smanzana.nostrummagica.command;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NostrumMagica.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value=Dist.CLIENT)
public class ClientCommands {

	public static Map<String, BiFunction<PlayerEntity, String, Boolean>> COMMANDS = new HashMap<>();

	public static void Register(String command, BiFunction<PlayerEntity, String, Boolean> handler) {
		COMMANDS.put(command.toLowerCase(), handler);
	}
	
	@SubscribeEvent
	public void onClientChat(ClientChatEvent event) {
		final String message = event.getOriginalMessage();
		if (COMMANDS.containsKey(message.toLowerCase())) {
			final Minecraft mc = Minecraft.getInstance();
			final PlayerEntity player = mc.player;
			if (COMMANDS.get(message.toLowerCase()).apply(player, message)) {
				event.setCanceled(true);
			}
		}
	}
	
}
