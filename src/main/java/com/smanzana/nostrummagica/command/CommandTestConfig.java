package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandTestConfig {

	public static int level = 0;
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("testconfig")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("level", IntegerArgumentType.integer())
							.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "level")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, int level) throws CommandSyntaxException {
		CommandTestConfig.level = level;
		return 0;
	}
}
