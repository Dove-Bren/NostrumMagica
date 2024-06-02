package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.block.NostrumObelisk;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandSpawnObelisk {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("spawnobelisk")
					.requires(s -> s.hasPermissionLevel(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		if (!NostrumObelisk.spawnObelisk(player.world,
				player.getPosition().add(0, -1, 0))) {
			context.getSource().sendFeedback(new StringTextComponent("Not enough space to spawn an obelisk"), true);
			return 1;
		}
		
		return 0;
	}

}
