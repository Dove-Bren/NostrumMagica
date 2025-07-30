package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.client.render.NostrumRenderTypes;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;

public class CommandReloadRenderTypes {

	public static final String Command = "rrendertypes";
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal(Command)
						.executes(ctx -> execute(ctx))
				);
		
		// Register client-handler, too
		ClientCommands.Register(Command, CommandReloadRenderTypes::HandleClient);
	}

	private static final int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
	
		; // Do nothing on server
		
		return 0;
	}
	
	public static final boolean HandleClient(Player player, String msg) {
		NostrumRenderTypes.InitRenderStates();
		return true;
	}

}
