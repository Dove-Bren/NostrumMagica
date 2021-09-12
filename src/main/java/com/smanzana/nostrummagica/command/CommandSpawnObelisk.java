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
	public String getName() {
		return "spawnobelisk";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/spawnobelisk";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			if (!NostrumObelisk.spawnObelisk(sender.getEntityWorld(),
					player.getPosition().add(0, -1, 0))) {
				sender.sendMessage(new TextComponentString("Not enough space to spawn an obelisk"));
			}
		} else {
			sender.sendMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
