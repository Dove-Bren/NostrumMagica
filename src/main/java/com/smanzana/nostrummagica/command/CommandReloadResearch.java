package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandReloadResearch extends CommandBase {

	@Override
	public String getCommandName() {
		return "RReload";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/RReload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (sender instanceof EntityPlayer) {
			NostrumMagica.instance.reloadDefaultResearch();
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
