package com.smanzana.nostrummagica.command;

import java.util.LinkedList;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.item.PositionCrystal;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.world.blueprints.Blueprint;
import com.smanzana.nostrummagica.world.blueprints.BlueprintLocation;
import com.smanzana.nostrummagica.world.dungeon.room.BlueprintDungeonRoom;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomLoader;

import net.minecraft.block.BlockState;
import net.minecraft.block.ComparatorBlock;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.StringTextComponent;

public class CommandWriteRoom {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("writeroom")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("name", StringArgumentType.greedyString())
						.then(Commands.argument("weight", IntegerArgumentType.integer(1))
							.then(Commands.argument("cost", IntegerArgumentType.integer(1))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight"), IntegerArgumentType.getInteger(ctx, "cost")))
								)
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "weight")))
							)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name")))
						)
				);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name) throws CommandSyntaxException {
		return execute(context, name, 1);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name, final int weight) throws CommandSyntaxException {
		return execute(context, name, weight, 1);
	}
	
	private static final int execute(CommandContext<CommandSource> context, final String name, final int weight, final int cost) throws CommandSyntaxException {
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
		
		final BlockPos pos1 = PositionCrystal.getBlockPosition(main);
		final BlockPos pos2 = PositionCrystal.getBlockPosition(offhand);
		final BlockPos minPos = new BlockPos(Math.min(pos1.getX(), pos2.getX()),
				Math.min(pos1.getY(), pos2.getY()),
				Math.min(pos1.getZ(), pos2.getZ()));
		final BlockPos maxPos = new BlockPos(Math.max(pos1.getX(), pos2.getX()),
				Math.max(pos1.getY(), pos2.getY()),
				Math.max(pos1.getZ(), pos2.getZ()));
		BlueprintLocation[] foundEntry = {null};
		
		// Look for entry marker
		WorldUtil.ScanBlocks(player.world, minPos, maxPos, (world, pos) -> {
			BlockState state = world.getBlockState(pos);
			if (BlueprintDungeonRoom.IsEntry(state)) {
				foundEntry[0] = new BlueprintLocation(pos.toImmutable().subtract(minPos), state.get(ComparatorBlock.HORIZONTAL_FACING).getOpposite());
				return false;
			}
			
			return true;
		});
		
		Blueprint blueprint = Blueprint.Capture(player.world,
				minPos, maxPos,
				foundEntry[0]);
		
		if (DungeonRoomLoader.instance().writeRoomAsFile(blueprint, name, weight, cost, new LinkedList<>())) {
			context.getSource().sendFeedback(new StringTextComponent("Room written!"), true);
		} else {
			context.getSource().sendFeedback(new StringTextComponent("An error was encountered while writing the room"), true);
		}
		
		return 0;
	}
}
