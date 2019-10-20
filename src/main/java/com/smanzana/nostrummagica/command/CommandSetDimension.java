package com.smanzana.nostrummagica.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.common.DimensionManager;

public class CommandSetDimension extends CommandBase {

	@Override
	public String getCommandName() {
		return "tpdm";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/tpdm [dimension]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		int dimension = 0;
		if (args.length >= 1) {
			dimension = Integer.parseInt(args[0]);
		}
		
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			
			if (player.isCreative()) {
				if (DimensionManager.isDimensionRegistered(dimension)) {
					player.setPortal(player.getPosition());
					player.changeDimension(dimension);
				} else {
					sender.addChatMessage(new TextComponentString("That dimension doesn't seem to exist!"));
				}
			} else {
				sender.addChatMessage(new TextComponentString("You must be in creative to execute this command!"));
			}
		}
	}

}
