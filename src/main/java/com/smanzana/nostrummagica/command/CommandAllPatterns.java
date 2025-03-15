package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.ISpellCrafting;
import com.smanzana.nostrummagica.spellcraft.pattern.SpellCraftPattern;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandAllPatterns  {

	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumpatterns")
					.requires(s -> s.hasPermission(2))
					.executes(ctx -> execute(ctx))
				);
	}

	private static final int execute(CommandContext<CommandSource> context) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().getPlayerOrException();
		
		ISpellCrafting attr = NostrumMagica.getSpellCrafting(player);
		if (attr == null) {
			context.getSource().sendSuccess(new StringTextComponent("Could not find magic wrapper for player"), true);
			return 1;
		}
		
		for (SpellCraftPattern pattern : SpellCraftPattern.GetAll()) {
			attr.addPattern(pattern);
		}
		
		NostrumMagica.instance.proxy.syncPlayer(player);
		
		return 0;
	}
}
