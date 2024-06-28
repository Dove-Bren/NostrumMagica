package com.smanzana.nostrummagica.command;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.item.SpellScroll;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.Spell;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.AIShape;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

public class CommandRandomSpell {
	
	public static final void register(CommandDispatcher<CommandSource> dispatcher) {
		dispatcher.register(
				Commands.literal("randomspell")
					.requires(s -> s.hasPermissionLevel(2))
					.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("cost", IntegerArgumentType.integer(0)).then(Commands.argument("weight", IntegerArgumentType.integer(0)))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "cost"), IntegerArgumentType.getInteger(ctx, "weight")))
							)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name")))
						)
					.executes(ctx -> execute(ctx, ""))
				);
	}

	private static final int execute(CommandContext<CommandSource> context, String name) throws CommandSyntaxException {
		return execute(context, name, 50, 1);
	}
	
	private static final int execute(CommandContext<CommandSource> context, String name, int cost, int weight) throws CommandSyntaxException {
		ServerPlayerEntity player = context.getSource().asPlayer();
		
		if (name == null || name.isEmpty()) {
			name = "Random Spell";
		}
		
		final Spell spell = CreateRandomSpell(name, null, cost, weight);
		ItemStack stack = SpellScroll.create(spell);
		player.inventory.addItemStackToInventory(stack);
		
		return 0;
	}

	public static Spell CreateRandomSpell(String name, @Nullable Random rand, int cost, int weight) {
		if (rand == null) {
			rand = NostrumMagica.rand;
		}
		
		Spell spell = new Spell(name, false, cost, weight);
		
		// Go ahead and do 1 shape
		final List<SpellShape> shapes = Lists.newArrayList(SpellShape.getAllShapes().stream().filter((t) -> {return !(t instanceof AIShape);}).iterator());
		spell.addPart(new SpellShapePart(
				shapes.get(rand.nextInt(shapes.size()))
				));
		
		final EMagicElement elem = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
		final int power = rand.nextInt(3) + 1;
		final EAlteration alt;
		if (rand.nextBoolean()) {
			alt = EAlteration.values()[rand.nextInt(EAlteration.values().length)];
		} else {
			alt = null;
		}
		spell.addPart(new SpellEffectPart(
				elem,
				power,
				alt
				));
		
		return spell;
	}

}
