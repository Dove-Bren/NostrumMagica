package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.EffectData;
import com.smanzana.nostrummagica.listeners.MagicEffectProxy.SpecialEffect;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class CommandDebugEffect {
	
	public static final SimpleCommandExceptionType EFFECT_NOT_FOUND = new SimpleCommandExceptionType(new TranslationTextComponent("argument.nostrummagica.effect.unknown"));
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumdebugeffect")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("effect", StringArgumentType.string())
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "effect")))
						)
					
				);
	}

	private static final int execute(CommandContext<CommandSource> context, final String effectName) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
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
			context.getSource().sendFeedback(new StringTextComponent("Player is not under that effect"), true);
		} else {
			String result = "Effect found with element {"
					+ (data.getElement() == null ? "NULL" : data.getElement().getName())
					+ "}, amount {"
					+ data.getAmt()
					+ "}, and count {"
					+ data.getCount()
					+ "}";
			context.getSource().sendFeedback(new StringTextComponent(result), true);
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
