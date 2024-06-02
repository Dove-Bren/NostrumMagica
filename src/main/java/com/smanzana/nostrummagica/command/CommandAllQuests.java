package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.progression.quest.NostrumQuest;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandAllQuests {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumquests")
					.requires(s -> s.hasPermissionLevel(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
		if (attr == null) {
			context.getSource().sendFeedback(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		for (NostrumQuest quest : NostrumQuest.allQuests()) {
			attr.addQuest(quest.getKey());
			attr.completeQuest(quest.getKey());
		}
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayerEntity) player);
		
		return 0;
	}

}
