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

public class CommandUnlock extends CommandBase {

	@Override
	public String getName() {
		return "nostrumunlock";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumunlock";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null) {
				sender.sendMessage(new TextComponentString("Could not find magic wrapper"));
				return;
			}
			
			attr.unlock();
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (EntityPlayerMP) player);
		} else {
			sender.sendMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
