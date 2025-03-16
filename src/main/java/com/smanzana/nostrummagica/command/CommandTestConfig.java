package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandTestConfig {

	public static int level = 0;
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("testconfig")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("level", IntegerArgumentType.integer())
							.executes(ctx -> execute(ctx, IntegerArgumentType.getInteger(ctx, "level")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, int level) throws CommandSyntaxException {
		CommandTestConfig.level = level;
		return 0;
	}
}
