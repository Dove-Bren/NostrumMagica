package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.client.gui.infoscreen.InfoScreen;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandInfoScreenGoto extends CommandBase {

	public static final String Command = "nostrumgoto";
	
	@Override
	public String getName() {
		return Command;
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumgoto [tag]";
	}
	
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof PlayerEntity && ((PlayerEntity) sender).world.isRemote) {
			PlayerEntity player = (PlayerEntity) sender;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null)
				return;
			Minecraft.getInstance().displayGuiScreen(new InfoScreen(attr, args[0]));
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a client player"));
		}
	}

}
