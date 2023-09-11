package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandInfoScreenGoto {

	public static final String Command = "nostrumgoto";
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal(Command)
					//.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("tag", StringArgumentType.string())
							.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "tag")))
							)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, final String tag) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendFeedback(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		int unused; // TODO: Change this to not use a command, since that doesn't work anymore :(
		
		//Minecraft.getInstance().displayGuiScreen(new InfoScreen(attr, args[0]));
		
//		if (sender instanceof PlayerEntity && ((PlayerEntity) sender).world.isRemote) {
//			PlayerEntity player = (PlayerEntity) sender;
//			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
//			if (attr == null) {
//				context.getSource().sendFeedback(new StringTextComponent("Could not find magic wrapper for player"), true);
//				return 1;
//			}
//			
//		} else {
//			sender.sendMessage(new StringTextComponent("This command must be run as a client player"));
//		}
		
		return 0;
	}

}
