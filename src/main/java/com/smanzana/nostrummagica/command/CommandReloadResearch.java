package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandReloadResearch extends CommandBase {

	@Override
	public String getName() {
		return "RReload";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/RReload";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (sender instanceof PlayerEntity) {
			NostrumMagica.instance.reloadDefaultResearch();
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a player"));
		}
	}

}
