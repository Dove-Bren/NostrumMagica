package com.smanzana.nostrummagica.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandTestConfig extends CommandBase {

	public static int level = 0;
	
	@Override
	public String getCommandName() {
		return "testconfig";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/testconfig [level]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		try {
			level = Integer.parseInt(args[0]);
		} catch (NumberFormatException e) {
			sender.addChatMessage(new TextComponentString("Failed to parse level"));
		}
	}

}
