package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.TutorialMessage;
import com.smanzana.nostrummagica.progression.tutorial.NostrumTutorial;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.server.command.EnumArgument;

public class CommandShowTutorial {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumtutorial")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("tutorial", EnumArgument.enumArgument(NostrumTutorial.class))
							.executes(ctx -> execute(ctx, ctx.getArgument("tutorial", NostrumTutorial.class)))
							)
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, NostrumTutorial tutorial) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		NetworkHandler.sendTo(new TutorialMessage(tutorial), player);
		
		return 0;
	}
}
