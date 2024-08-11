package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;

public class CommandInfoScreenGoto {

	public static final String Command = "nostrumgoto";
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal(Command)
					//.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("tag", StringArgumentType.string())
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "tag")))
							)
				);
		
		// Register client-handler, too
		ClientCommands.Register(Command, CommandInfoScreenGoto::handleClient);
	}

	private static final int execute(CommandContext<CommandSource> context, final String tag) throws CommandSyntaxException {
	
		; // Do nothing on server
		
		return 0;
	}
	
	private static final boolean handleClient(PlayerEntity player, String msg) {
		// expect {/nostrumgoto "Lore::thing"}
		final String tag = msg.substring(Command.length() + 3, msg.length() - 1);
		NostrumMagica.instance.proxy.openLoreLink(tag);
		return true;
	}

}
