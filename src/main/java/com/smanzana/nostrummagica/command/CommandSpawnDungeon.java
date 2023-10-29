package com.smanzana.nostrummagica.command;

import java.util.Random;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonExitPoint;
import com.smanzana.nostrummagica.world.gen.NostrumDungeonStructure;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ISuggestionProvider;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.Direction;

public class CommandSpawnDungeon {

	private static Random rand = null;
	
	private static final String[] names = {"dragon", "portal", "plantboss"};
	private static final NostrumDungeon[] dungeons = {NostrumDungeonStructure.DRAGON_DUNGEON, NostrumDungeonStructure.PORTAL_DUNGEON, NostrumDungeonStructure.PLANTBOSS_DUNGEON};
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("spawndungeon")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("type", StringArgumentType.string())
							.suggests((ctx, sb) -> ISuggestionProvider.suggest(names, sb))
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "type")))
							)
				);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String typeName) throws CommandSyntaxException {
		
		NostrumDungeon dungeon = null;
		for (int i = 0; i < names.length; i++) {
			final String name = names[i];
			if (name.equalsIgnoreCase(typeName)) {
				dungeon = dungeons[i];
				break;
			}
		}
		
		if (dungeon == null) {
			throw CommandSyntaxException.BUILT_IN_EXCEPTIONS.dispatcherUnknownArgument().create(); 
		} else {
			return execute(context, dungeon);
		}
	}

	private static final int execute(CommandContext<CommandSource> context, final NostrumDungeon dungeon) throws CommandSyntaxException {
		if (CommandSpawnDungeon.rand == null) {
			CommandSpawnDungeon.rand = new Random();
		}
		
		ServerPlayerEntity player = context.getSource().asPlayer();
		dungeon.spawn(player.world, new DungeonExitPoint(player.getPosition(), Direction.fromAngle(player.rotationYaw)));
		
		return 0;
	}
}
