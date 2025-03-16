package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.util.DimensionUtils;
import com.smanzana.nostrummagica.world.dimension.NostrumSorceryDimension;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

public class CommandSetDimension {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("tpdm")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("dimension", DimensionArgument.dimension())
							.executes(ctx -> execute(ctx, DimensionArgument.getDimension(ctx, "dimension")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, ServerLevel world) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new TextComponent("Could not find magic wrapper for player"), true);
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
				context.getSource().sendSuccess(new TextComponent("That dimension doesn't seem to exist!"), true);
			}
		} else {
			context.getSource().sendSuccess(new TextComponent("You must be in creative to execute this command!"), true);
		}
		
		return 0;
	}

}
