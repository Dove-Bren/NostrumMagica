package com.smanzana.nostrummagica.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint.LoadContext;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.util.Direction;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class CommandReadRoom {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("readroom")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("direction", EnumArgument.enumArgument(Direction.class))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), ctx.getArgument("direction", Direction.class)))
								)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), Direction.EAST))
						)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, final String name, final Direction facing) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		if (!player.isCreative()) {
			context.getSource().sendFeedback(new StringTextComponent("This command must be run as a creative player"), true);
			return 1;
		}
		
		// Must be a position crystals in hand with low corner selected
		ItemStack main = player.getHeldItemMainhand();
		if ((main.isEmpty() || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)) {
			context.getSource().sendFeedback(new StringTextComponent("You must be holding a filled geogem in your main hand"), true);
		} else {
			
			File file = new File("./NostrumMagicaData/dungeon_room_captures/" + name + ".dat");
			if (!file.exists()) {
				file = new File("./NostrumMagicaData/dungeon_room_captures/" + name + ".gat");
			}
			if (file.exists()) {
				CompoundNBT nbt = null;
				try {
					if (file.getName().endsWith(".gat")) {
						nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
					} else {
						nbt = CompressedStreamTools.read(file);
					}
					context.getSource().sendFeedback(new StringTextComponent("Room read from " + file.getPath()), true);
				} catch (IOException e) {
					e.printStackTrace();
					
					System.out.println("Failed to read out serialized file " + file.toString());
					context.getSource().sendFeedback(new StringTextComponent("Failed to read room"), true);
				}
				
				if (nbt != null) {
					RoomBlueprint blueprint = RoomBlueprint.fromNBT(new LoadContext(file.getAbsolutePath()), (CompoundNBT) nbt.get("blueprint"));
					if (blueprint != null) {
						blueprint.spawn(player.world, PositionCrystal.getBlockPosition(main), facing, UUID.randomUUID());
					} else {
						context.getSource().sendFeedback(new StringTextComponent("Room failed to load"), true);
					}
				}
			} else {
				context.getSource().sendFeedback(new StringTextComponent("Room not found"), true);
			}
		}
		
		return 0;
	}

}
