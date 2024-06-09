package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.capabilities.INostrumMagic.EMagicTier;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class CommandSetTier {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumtier")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("tier", EnumArgument.enumArgument(EMagicTier.class))
							.executes(ctx -> execute(ctx, ctx.getArgument("tier", EMagicTier.class)))
							)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, EMagicTier tier) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendFeedback(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		attr.setTier(tier);
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayerEntity) player);
		
		return 0;
	}
}
