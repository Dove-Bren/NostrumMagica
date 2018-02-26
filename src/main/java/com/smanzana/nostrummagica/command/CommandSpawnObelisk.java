package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.blocks.NostrumObelisk;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandSpawnObelisk extends CommandBase {

	@Override
	public String getCommandName() {
		return "SpawnObelisk";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/SpawnObelisk";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			NostrumObelisk.spawnObelisk(sender.getEntityWorld(),
					player.getPosition().add(0, -1, 0));
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
