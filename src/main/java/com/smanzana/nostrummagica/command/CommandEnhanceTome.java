package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancementWrapper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandEnhanceTome extends CommandBase {

	@Override
	public String getName() {
		return "enhancetome";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/enhancetome EnhancementName level";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (args.length != 2)
			throw new CommandException("Not enough arguments", new Object[0]);
		
		if (sender instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) sender;
			ItemStack tome = player.getHeldItemMainhand();
			if (tome.isEmpty() || !(tome.getItem() instanceof SpellTome)) {
				sender.sendMessage(new StringTextComponent("Did not find a spelltome in your mainhand!"));
				return;
			}
			
			SpellTomeEnhancement enhancement = SpellTomeEnhancement.lookupEnhancement(args[0]);
			if (enhancement == null)
				throw new CommandException("Unable to find enhancement by key " + args[0]);
			
			int level;
			try {
				level = Integer.parseInt(args[1]);
			} catch (NumberFormatException e) {
				throw new CommandException("Could not parse level " + args[1]);
			}
			
			SpellTome.addEnhancement(tome, new SpellTomeEnhancementWrapper(enhancement, level));
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a player"));
		}
	}

}
