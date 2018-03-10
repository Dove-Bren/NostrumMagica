package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.items.SpellTome.EnhancementWrapper;
import com.smanzana.nostrummagica.spelltome.enhancement.SpellTomeEnhancement;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandEnhanceTome extends CommandBase {

	@Override
	public String getCommandName() {
		return "EnhanceTome";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/EnhanceTome EnhancementName level";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (args.length != 2)
			throw new CommandException("Not enough arguments", new Object[0]);
		
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			ItemStack tome = player.getHeldItemMainhand();
			if (tome == null || !(tome.getItem() instanceof SpellTome)) {
				sender.addChatMessage(new TextComponentString("Did not find a spelltome in your mainhand!"));
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
			
			SpellTome.addEnhancement(tome, new EnhancementWrapper(enhancement, level));
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
