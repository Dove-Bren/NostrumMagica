package com.smanzana.nostrummagica.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.common.DimensionManager;

public class CommandSetDimension extends CommandBase {

	@Override
	public String getName() {
		return "tpdm";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/tpdm [dimension]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		int dimension = 0;
		if (args.length >= 1) {
			dimension = Integer.parseInt(args[0]);
		}
		
		if (sender instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) sender;
			
			if (player.isCreative()) {
				if (DimensionManager.isDimensionRegistered(dimension)) {
					System.out.println("Teleport Command!");
					player.setPortal(player.getPosition());
					player.changeDimension(dimension);
				} else {
					sender.sendMessage(new StringTextComponent("That dimension doesn't seem to exist!"));
				}
			} else {
				sender.sendMessage(new StringTextComponent("You must be in creative to execute this command!"));
			}
		}
	}

}
