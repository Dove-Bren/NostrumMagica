package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.item.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spell.RegisteredSpell;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CommandForceBind {

	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumbind")
					.requires(s -> s.hasPermission(2))
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
		
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTome)) {
			context.getSource().sendSuccess(new TextComponent("To force a bind, hold the tome that's being binded to in your main hand"), true);
			return 1;
		}
		
		ItemStack offhand = player.getOffhandItem();
		if (offhand.isEmpty() || !(offhand.getItem() instanceof SpellScroll)
				|| SpellScroll.GetSpell(offhand) == null) {
			context.getSource().sendSuccess(new TextComponent("Either use while holding a tome that's currently binding OR hold a spell scroll in your offhand"), true);
		} else {
			RegisteredSpell spell = SpellScroll.GetSpell(offhand);
			if (SpellTome.hasRoom(stack, spell)) {
				SpellTome.addSpell(stack, spell);
			} else {
				context.getSource().sendSuccess(new TextComponent("The tome is full"), true);
			}
		}
			
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, player);
		
		return 0;
	}
	
}
