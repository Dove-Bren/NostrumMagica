package com.smanzana.nostrummagica.command;

import java.util.Random;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator.DungeonGen;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;

public class CommandSpawnDungeon {

	private static Random rand = null;
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("NostrumSpawnDungeon")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("type", StringArgumentType.string())
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "type")))
							)
					.executes(ctx -> execute(ctx))
				);
	}
	
	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		DungeonGen[] types = DungeonGen.values();
		DungeonGen type = types[rand.nextInt(types.length)];
		
		return execute(context, type);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String typeName) throws CommandSyntaxException {
		DungeonGen type = null;
		try {
			type = DungeonGen.valueOf(typeName.toUpperCase());
		} catch (Exception e) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create(); 
		}
		
		return execute(context, type);
	}

	private static final int execute(CommandContext<CommandSource> context, final DungeonGen type) throws CommandSyntaxException {
		if (CommandSpawnDungeon.rand == null) {
			CommandSpawnDungeon.rand = new Random();
		}
		
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		type.getGenerator().generate(player.world, rand, player.getPosition());
		
		return 0;
	}
}
