package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;

public class CommandSetManaArmor {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrummanaarmor")
					.requires(s -> s.hasPermission(2))
						.then(Commands.argument("engaged", BoolArgumentType.bool())
								.then(Commands.argument("cost", IntegerArgumentType.integer(0))
										.executes(ctx -> execute(ctx, BoolArgumentType.getBool(ctx, "engaged"), IntegerArgumentType.getInteger(ctx, "cost")))
										)
								)
					
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, boolean engaged, int cost) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		IManaArmor attr = NostrumMagica.getManaArmor(player);
		if (attr == null) {
			context.getSource().sendSuccess(new TextComponent("Could not find mana armor wrapper"), true);
			return 1;
		}
		
		attr.setHasArmor(engaged, cost);
		NostrumMagica.instance.proxy.sendManaArmorCapability(player);
		return 0;
	}
}
