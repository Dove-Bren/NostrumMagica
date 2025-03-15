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
import com.smanzana.nostrummagica.spell.Spell;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class CommandForceBind {

	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumbind")
					.requires(s -> s.hasPermission(2))
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
		
		ItemStack stack = player.getMainHandItem();
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTome)) {
			context.getSource().sendSuccess(new StringTextComponent("To force a bind, hold the tome that's being binded to in your main hand"), true);
			return 1;
		}
		
		ItemStack offhand = player.getOffhandItem();
		if (offhand.isEmpty() || !(offhand.getItem() instanceof SpellScroll)
				|| SpellScroll.GetSpell(offhand) == null) {
			context.getSource().sendSuccess(new StringTextComponent("Either use while holding a tome that's currently binding OR hold a spell scroll in your offhand"), true);
		} else {
			Spell spell = SpellScroll.GetSpell(offhand);
			if (SpellTome.hasRoom(stack, spell)) {
				SpellTome.addSpell(stack, spell);
			} else {
				context.getSource().sendSuccess(new StringTextComponent("The tome is full"), true);
			}
		}
			
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, player);
		
		return 0;
	}
	
}
