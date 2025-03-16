package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class CommandGiveSkillpoint {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumskillpoint")
					.requires(s -> s.hasPermission(2))
						.then(Commands.argument("element", EnumArgument.enumArgument(EMagicElement.class))
								.executes(ctx -> execute(ctx, ctx.getArgument("element", EMagicElement.class)))
						)
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new TextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		attr.addSkillPoint();
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayer) player);
		
		return 0;
	}
	
	private static final int execute(CommandContext<CommandSourceStack> context, EMagicElement element) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new TextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		attr.addElementalSkillPoint(element);
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayer) player);
		
		return 0;
	}
	
}
