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
import com.smanzana.nostrummagica.item.equipment.SpellScroll;
import com.smanzana.nostrummagica.spell.EAlteration;
import com.smanzana.nostrummagica.spell.EMagicElement;
import com.smanzana.nostrummagica.spell.RegisteredSpell;
import com.smanzana.nostrummagica.spell.SpellType;
import com.smanzana.nostrummagica.spell.component.SpellEffectPart;
import com.smanzana.nostrummagica.spell.component.SpellShapePart;
import com.smanzana.nostrummagica.spell.component.shapes.AIShape;
import com.smanzana.nostrummagica.spell.component.shapes.SpellShape;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class CommandRandomSpell {
	
	public static final void register(CommandDispatcher<CommandSourceStack> dispatcher) {
		dispatcher.register(
				Commands.literal("randomspell")
					.requires(s -> s.hasPermission(2))
					.then(Commands.argument("name", StringArgumentType.string())
						.then(Commands.argument("cost", IntegerArgumentType.integer(0)).then(Commands.argument("weight", IntegerArgumentType.integer(0)))
								.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name"), IntegerArgumentType.getInteger(ctx, "cost"), IntegerArgumentType.getInteger(ctx, "weight")))
							)
						.executes(ctx -> execute(ctx, StringArgumentType.getString(ctx, "name")))
						)
					.executes(ctx -> execute(ctx, ""))
				);
	}

	private static final int execute(CommandContext<CommandSourceStack> context, String name) throws CommandSyntaxException {
		return execute(context, name, 50, 1);
	}
	
	private static final int execute(CommandContext<CommandSourceStack> context, String name, int cost, int weight) throws CommandSyntaxException {
		ServerPlayer player = context.getSource().getPlayerOrException();
		
		if (name == null || name.isEmpty()) {
			name = "Random Spell";
		}
		
		final RegisteredSpell spell = CreateRandomSpell(name, null, cost, weight);
		ItemStack stack = SpellScroll.create(spell);
		player.getInventory().add(stack);
		
		return 0;
	}

	public static RegisteredSpell CreateRandomSpell(String name, @Nullable Random rand, int cost, int weight) {
		if (rand == null) {
			rand = NostrumMagica.rand;
		}
		
		RegisteredSpell spell = RegisteredSpell.MakeAndRegister(name, SpellType.Crafted, cost, weight);
		
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
		
//		int unused;
//		spell.addPart(new SpellShapePart(NostrumSpellShapes.Touch));
//		for (EMagicElement element : EMagicElement.values()) {
//			spell.addPart(new SpellEffectPart(element, 1, null));
//			for (EAlteration alteration : EAlteration.values()) {
//				spell.addPart(new SpellEffectPart(element, 1, alteration));
//			}
//		}
		
		return spell;
	}

}
