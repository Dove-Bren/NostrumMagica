package com.smanzana.nostrummagica.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandTestConfig extends CommandBase {

	public static int level = 0;
	
	@Override
	public String getName() {
		return "testconfig";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/testconfig [level]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			level = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.sendMessage(new StringTextComponent("Failed to parse level"));
		}
	}

}
