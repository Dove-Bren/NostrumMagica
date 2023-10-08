package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.text.StringTextComponent;

public class CommandForceBind {

	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumbind")
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
		
		ItemStack stack = player.getHeldItemMainhand();
		if (stack.isEmpty() || !(stack.getItem() instanceof SpellTome)) {
			context.getSource().sendFeedback(new StringTextComponent("To force a bind, hold the tome that's being binded to in your main hand"), true);
			return 1;
		}
		if (attr.isBinding()) {
			attr.completeBinding(stack);
		} else {
			ItemStack offhand = player.getHeldItemOffhand();
			if (offhand.isEmpty() || !(offhand.getItem() instanceof SpellScroll)
					|| SpellScroll.getSpell(offhand) == null) {
				context.getSource().sendFeedback(new StringTextComponent("Either use while holding a tome that's currently binding OR hold a spell scroll in your offhand"), true);
			} else {
				Spell spell = SpellScroll.getSpell(offhand);
				attr.startBinding(spell, null, SpellTome.getTomeID(stack));
				attr.completeBinding(stack);
			}
		}
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, player);
		
		return 0;
	}
	
}
