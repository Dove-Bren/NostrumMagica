package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.command.arguments.DimensionArgument;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.server.ServerWorld;

public class CommandSetDimension {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("tpdm")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("dimension", DimensionArgument.dimension())
							.executes(ctx -> execute(ctx, DimensionArgument.getDimension(ctx, "dimension")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, ServerWorld world) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		if (player.isCreative()) {
			if (world != null) {
				System.out.println("Teleport Command!");
				
				if (DimensionUtils.IsSorceryDim(world)) {
					// Teleporting TO sorcery dimension.
					player.changeDimension(world, NostrumSorceryDimension.DimensionEntryTeleporter.INSTANCE);
				} else if (DimensionUtils.IsOverworld(world) && DimensionUtils.IsSorceryDim(player.level)) {
					// If coming FROM sorcery, use exit
					player.changeDimension(world, NostrumSorceryDimension.DimensionReturnTeleporter.INSTANCE);
				} else {
					//player.setPortal(player.getPosition());
					player.changeDimension(world);
				}
			} else {
				context.getSource().sendSuccess(new StringTextComponent("That dimension doesn't seem to exist!"), true);
			}
		} else {
			context.getSource().sendSuccess(new StringTextComponent("You must be in creative to execute this command!"), true);
		}
		
		return 0;
	}

}
