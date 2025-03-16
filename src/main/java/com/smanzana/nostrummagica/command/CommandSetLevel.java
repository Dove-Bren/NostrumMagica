package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;

public class CommandSetLevel {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumlevel")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("level", IntegerArgumentType.integer(0))
							.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "level")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, int level) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new TextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		attr.setLevel(level);
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayer) player);
		
		return 0;
	}
}
