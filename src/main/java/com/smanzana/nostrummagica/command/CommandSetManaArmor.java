package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandSetManaArmor {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("NostrumManaArmor")
					.requires(s -> s.hasPermissionLevel(2))
						.then(Commands.argument("engaged", BoolArgumentType.bool())
								.then(Commands.argument("cost", IntegerArgumentType.integer(0))
										.executes(ctx -> execute(ctx, BoolArgumentType.getBool(ctx, "engaged"), IntegerArgumentType.getInteger(ctx, "cost")))
										)
								)
					
				);
	}

	private static final int execute(CommandContext<CommandSource> context, boolean engaged, int cost) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		IManaArmor attr = NostrumMagica.getManaArmor(player);
		if (attr == null) {
			context.getSource().sendFeedback(new StringTextComponent("Could not find mana armor wrapper"), true);
			return 1;
		}
		
		attr.setHasArmor(engaged, cost);
		NostrumMagica.instance.proxy.sendManaArmorCapability(player);
		return 0;
	}
}
