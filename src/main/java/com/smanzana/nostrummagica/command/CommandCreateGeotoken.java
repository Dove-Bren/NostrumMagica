package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.items.PositionToken;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandCreateGeotoken extends CommandBase {

	@Override
	public String getCommandName() {
		return "creategeotoken";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/creategeotoken";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			ItemStack stack = new ItemStack(PositionToken.instance());
			PositionToken.setPosition(stack, player.dimension, player.getPosition());
			player.inventory.addItemStackToInventory(stack);
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
