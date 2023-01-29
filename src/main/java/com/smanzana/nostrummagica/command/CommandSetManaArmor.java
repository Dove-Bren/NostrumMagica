package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.IManaArmor;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandSetManaArmor extends CommandBase {

	@Override
	public String getName() {
		return "NostrumManaArmor";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/NostrumManaArmor {on/off} {cost}";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (!(sender instanceof EntityPlayer)) {
			sender.sendMessage(new TextComponentString("This command must be run as a player"));
		} else if (args.length != 2) {
			sender.sendMessage(new TextComponentString("Two arguments are required"));
		} else {
			boolean on;
			if (args[0].equalsIgnoreCase("on") || args[0].equalsIgnoreCase("1")) {
				on = true;
			} else if (args[0].equalsIgnoreCase("off") || args[0].equalsIgnoreCase("0")) {
				on = false;
			} else {
				sender.sendMessage(new TextComponentString("Could not parse [" + args[0] + "]. Should be \"on\" or \"off\""));
				return;
			}
			
			int cost = 0;
			cost = Integer.parseInt(args[1]);
			
			EntityPlayer player = (EntityPlayer) sender;
			IManaArmor attr = NostrumMagica.getManaArmor(player);
			if (attr == null) {
				sender.sendMessage(new TextComponentString("Could not find mana armor wrapper"));
				return;
			}
			
			attr.setHasArmor(on, cost);
			NostrumMagica.proxy.sendManaArmorCapability(player);
		}
	}

}
