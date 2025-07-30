package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class CommandInfoScreenGoto {

	public static final String Command = "nostrumgoto";
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal(Command)
					//.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("tag", StringArgumentType.string())
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "tag")))
							)
				);
		
		// Register client-handler, too
		ClientCommands.Register(Command, CommandInfoScreenGoto::HandleClient);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, final String tag) throws CommandSyntaxException {
	
		; // Do nothing on server
		
		return 0;
	}
	
	public static final boolean HandleClient(Player player, String msg) {
		// expect {/nostrumgoto "Lore::thing"}
		final String tag = msg.substring(Command.length() + 3, msg.length() - 1);
		NostrumMagica.Proxy.openLoreLink(tag);
		return true;
	}

}
