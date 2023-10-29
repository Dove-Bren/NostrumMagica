package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.dimension.DimensionType;

public class CommandSetDimension {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("tpdm")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("dimension", DimensionArgument.getDimension())
							.executes(ctx -> execute(ctx, DimensionArgument.func_212592_a(ctx, "dimension")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, DimensionType dimension) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendFeedback(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		if (player.isCreative()) {
			if (dimension != null) {
				System.out.println("Teleport Command!");
				//player.setPortal(player.getPosition());
				player.changeDimension(dimension);
			} else {
				context.getSource().sendFeedback(new StringTextComponent("That dimension doesn't seem to exist!"), true);
			}
		} else {
			context.getSource().sendFeedback(new StringTextComponent("You must be in creative to execute this command!"), true);
		}
		
		return 0;
	}

}
