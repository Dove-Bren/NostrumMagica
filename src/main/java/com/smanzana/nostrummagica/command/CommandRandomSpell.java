package com.smanzana.nostrummagica.command;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class CommandRandomSpell {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("randomspell")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("name", StringArgumentType.greedyString())
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name")))
						)
					.executes(ctx -> execute(ctx, ""))
				);
	}

	private static final int execute(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		if (name == null || name.isEmpty()) {
			name = "Random Spell";
		}
		
		final Spell spell = CreateRandomSpell(name, null);
		ItemStack stack = SpellScroll.create(spell);
		player.inventory.addItemStackToInventory(stack);
		
		return 0;
	}

	public static Spell CreateRandomSpell(String name, @Nullable Random rand) {
		if (rand == null) {
			rand = NostrumMagica.rand;
		}
		
		Spell spell = new Spell(name, false);
		
		// Go ahead and do 1 trigger and 1 shape
		final List<SpellTrigger> triggers = Lists.newArrayList(SpellTrigger.getAllTriggers().stream().filter((t) -> {return !(t instanceof AITargetTrigger);}).iterator());
		spell.addPart(new SpellPart(
				triggers.get(rand.nextInt(triggers.size()))
				));
		
		final List<SpellShape> shapes = Lists.newArrayList(SpellShape.getAllShapes());
		final SpellShape shape = shapes.get(rand.nextInt(shapes.size()));
		final EMagicElement elem = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
		final int power = rand.nextInt(3) + 1;
		final EAlteration alt;
		if (rand.nextBoolean()) {
			alt = EAlteration.values()[rand.nextInt(EAlteration.values().length)];
		} else {
			alt = null;
		}
		spell.addPart(new SpellPart(
				shape,
				elem,
				power,
				alt
				));
		
		return spell;
	}

}
