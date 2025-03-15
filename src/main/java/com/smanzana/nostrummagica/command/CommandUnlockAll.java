package com.smanzana.nostrummagica.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.message.StatSyncMessage;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.EElementalMastery;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.text.StringTextComponent;

public class CommandUnlockAll {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("nostrumunlockall")
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
		
		attr.unlock();
		
		for (SpellShape shape : SpellShape.getAllShapes()) {
			attr.addShape(shape);
		}
		for (EAlteration alt : EAlteration.values()) {
			attr.unlockAlteration(alt);
		}
		for (EMagicElement elem : EMagicElement.values()) {
			attr.setElementalMastery(elem, EElementalMastery.MASTER);
		}
		NetworkHandler.sendTo(
				new StatSyncMessage(attr)
				, (ServerPlayerEntity) player);
		
		return 0;
	}
}
