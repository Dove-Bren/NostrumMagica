package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;

public class CommandReloadSkills {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("sreload")
					.requires(s -> s.hasPermissionLevel(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		NostrumMagica.instance.reloadDefaultSkills();
		return 0;
	}
}
