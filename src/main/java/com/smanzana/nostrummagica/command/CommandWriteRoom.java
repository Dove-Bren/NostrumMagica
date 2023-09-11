package com.smanzana.nostrummagica.command;

import java.util.LinkedList;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class CommandWriteRoom {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("writeroom")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("name", StringArgumentType.greedyString())
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name")))
						)
				);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name, final Direction facing) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		if (!player.isCreative()) {
			context.getSource().sendFeedback(new StringTextComponent("This command must be run as a creative player"), true);
			return 1;
		}
		
		// Must be holding two position crystals in hands with corners selected
		ItemStack main = player.getHeldItemMainhand();
		ItemStack offhand = player.getHeldItemOffhand();
		if ((main.isEmpty() || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)
			|| (offhand.isEmpty() || !(offhand.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(offhand) == null)) {
			context.getSource().sendFeedback(new StringTextComponent("You must be holding a filled geogem in both of your hands"), true);
			return 1;
		}
		
		RoomBlueprint blueprint = new RoomBlueprint(player.world,
				PositionCrystal.getBlockPosition(main),
				PositionCrystal.getBlockPosition(offhand),
				true);
		
		if (DungeonRoomRegistry.instance().writeRoomAsFile(blueprint, name, 1, new LinkedList<>())) {
			context.getSource().sendFeedback(new StringTextComponent("Room written!"), true);
		} else {
			context.getSource().sendFeedback(new StringTextComponent("An error was encountered while writing the room"), true);
		}
		
		return 0;
	}
}
