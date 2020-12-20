package com.smanzana.nostrummagica.command;

import java.util.List;
import java.util.Random;

import javax.annotation.Nullable;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.Spell;
import com.smanzana.nostrummagica.spells.Spell.SpellPart;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.spells.components.triggers.AITargetTrigger;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandRandomSpell extends CommandBase {

	@Override
	public String getCommandName() {
		return "randomspell";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/randomspell [name]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			final String name;
			if (args.length > 0) {
				name = args[0];
			} else {
				name = "Random Spell";
			}
			
			final Spell spell = CreateRandomSpell(name, null);
			EntityPlayer player = (EntityPlayer) sender;
			ItemStack stack = SpellScroll.create(spell, (byte) 0);
			player.inventory.addItemStackToInventory(stack);
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
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
		final int power = rand.nextInt(3);
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
