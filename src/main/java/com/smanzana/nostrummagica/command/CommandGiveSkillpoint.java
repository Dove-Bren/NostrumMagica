package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.server.command.EnumArgument;

public class CommandGiveSkillpoint {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumskillpoint")
					.requires(s -> s.hasPermission(2))
						.then(Commands.argument("element", EnumArgument.enumArgument(EMagicElement.class))
								.executes(ctx -> execute(ctx, ctx.getArgument("element", EMagicElement.class)))
						)
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		attr.addSkillPoint();
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayerEntity) player);
		
		return 0;
	}
	
	private static final int execute(CommandContext<CommandSource> context, EMagicElement element) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendSuccess(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		attr.addElementalSkillPoint(element);
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayerEntity) player);
		
		return 0;
	}
	
}
