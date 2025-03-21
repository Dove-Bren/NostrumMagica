package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionToken;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CommandCreateGeotoken  {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("spawngeotoken")
					.requires(s -> s.hasPermission(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		ItemStack stack = new ItemStack(NostrumItems.positionToken);
		PositionToken.setPosition(stack, player.getCommandSenderWorld().dimension(), player.blockPosition());
		player.getInventory().add(stack);
		
		return 0;
	}
}
