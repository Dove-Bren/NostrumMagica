package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandSetLevel extends CommandBase {

	@Override
	public String getCommandName() {
		return "SetLevel";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/nostrumlevel [level]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (args.length != 1)
			throw new CommandException("Invalid number of arguments. Expected a level");
		
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null) {
				sender.addChatMessage(new TextComponentString("Could not find magic wrapper"));
				return;
			}
			
			int level;
			try {
				level = Integer.parseInt(args[0]);
			} catch (NumberFormatException e) {
				throw new CommandException("Cannot parse " + args[1]);
			}
			
			attr.setLevel(level);
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (EntityPlayerMP) player);
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
