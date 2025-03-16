package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;

public class CommandAllPatterns  {

	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumpatterns")
					.requires(s -> s.hasPermission(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		ISpellCrafting attr = NostrumMagica.getSpellCrafting(player);
		if (attr == null) {
			context.getSource().sendSuccess(new TextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		for (SpellCraftPattern pattern : SpellCraftPattern.GetAll()) {
			attr.addPattern(pattern);
		}
		
		NostrumMagica.instance.proxy.syncPlayer(player);
		
		return 0;
	}
}
