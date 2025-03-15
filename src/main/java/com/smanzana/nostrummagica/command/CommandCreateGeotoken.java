package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.item.PositionToken;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class CommandCreateGeotoken  {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("spawngeotoken")
					.requires(s -> s.hasPermission(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		
		ItemStack stack = new ItemStack(NostrumItems.positionToken);
		PositionToken.setPosition(stack, player.getCommandSenderWorld().dimension(), player.blockPosition());
		player.inventory.add(stack);
		
		return 0;
	}
}
