package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandEnhanceTome {
	
	public static final SimpleCommandExceptionType ENHANCEMENT_NOT_FOUND = new SimpleCommandExceptionType(new TranslationTextComponent("argument.nostrummagica.enhancement.unknown"));
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumenhance")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("enhancement", StringArgumentType.string())
						.then(Commands.argument("level", IntegerArgumentType.integer(0, 10))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "enhancement"), IntegerArgumentType.getInteger(ctx, "level")))
								)
						)
				);
	}

	private static final int execute(CommandContext<CommandSource> context, String enhancementName, int level) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		ItemStack tome = player.getHeldItemMainhand();
		if (tome.isEmpty() || !(tome.getItem() instanceof SpellTome)) {
			context.getSource().sendFeedback(new StringTextComponent("No tome found in your hands!"), true);
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
