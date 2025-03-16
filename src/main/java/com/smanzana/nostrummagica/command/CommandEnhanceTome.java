package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CommandEnhanceTome {
	
	public static final SimpleCommandExceptionType ENHANCEMENT_NOT_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("argument.nostrummagica.enhancement.unknown"));
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumenhance")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("enhancement", StringArgumentType.string())
						.then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "enhancement"), IntegerArgumentType.getInteger(ctx, "level")))
								)
						)
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, String enhancementName, int level) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		ItemStack tome = player.getMainHandItem();
		if (tome.isEmpty() || !(tome.getItem() instanceof SpellTome)) {
			context.getSource().sendSuccess(new TextComponent("No tome found in your hands!"), true);
			return 1;
		}
		
		SpellTomeEnhancement enhancement = SpellTomeEnhancement.lookupEnhancement(enhancementName);
		if (enhancement == null) {
			throw ENHANCEMENT_NOT_FOUND.create();
		}
		
		SpellTome.addEnhancement(tome, new SpellTomeEnhancementWrapper(enhancement, level));
		
		return 0;
	}

}
