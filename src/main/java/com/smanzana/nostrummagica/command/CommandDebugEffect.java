package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listener.MagicEffectProxy.SpecialEffect;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.chat.TranslatableComponent;

public class CommandDebugEffect {
	
	public static final SimpleCommandExceptionType EFFECT_NOT_FOUND = new SimpleCommandExceptionType(new TranslatableComponent("argument.nostrummagica.effect.unknown"));
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumdebugeffect")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("effect", StringArgumentType.string())
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "effect")))
						)
					
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, final String effectName) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		SpecialEffect effect;
		try {
			effect = SpecialEffect.valueOf(effectName.toUpperCase());
		} catch (Exception e) {
			effect = null;
		}
		
		if (effect == null) {
			throw EFFECT_NOT_FOUND.create();
		}
		
		EffectData data = NostrumMagica.magicEffectProxy.getData(player, effect);
		if (data == null) {
			context.getSource().sendSuccess(new TextComponent("Player is not under that effect"), true);
		} else {
			String result = "Effect found with element {"
					+ (data.getElement() == null ? "NULL" : data.getElement().name())
					+ "}, amount {"
					+ data.getAmt()
					+ "}, and count {"
					+ data.getCount()
					+ "}";
			context.getSource().sendSuccess(new TextComponent(result), true);
		}
		
//		if (!(sender.getCommandSenderEntity() instanceof PlayerEntity)) {
//			throw new CommandException("Command must be run by a creative player");
//		}
//		
//		final PlayerEntity player = (PlayerEntity) sender.getCommandSenderEntity();
//		if (!player.isCreative()) {
//			throw new CommandException("Command must be run by a creative player");
//		}
//		
//		ItemStack soulStack = player.getHeldItem(Hand.MAIN_HAND);
//		if (soulStack.isEmpty() || !(soulStack.getItem() instanceof PetSoulItem)) {
//			soulStack = player.getHeldItem(Hand.OFF_HAND);
//		}
//		
//		if (soulStack.isEmpty() || !(soulStack.getItem() instanceof PetSoulItem)) {
//			throw new CommandException("You must be holding a Pet Soul Item in one of your hands.");
//		}
//		
//		if (null == PetSoulItem.SpawnPet(soulStack, player.world, player.getPositionVec())) {
//			throw new CommandException("Failed to spawn entity");
//		}
		
		return 0;
	}
}
