package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class CommandReloadQuests {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("qreload")
					.requires(s -> s.hasPermission(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		NostrumMagica.instance.reloadDefaultQuests();
		return 0;
	}
}
