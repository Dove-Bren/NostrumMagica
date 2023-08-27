package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandGiveResearchpoint extends CommandBase {

	@Override
	public String getName() {
		return "nostrumresearchpoint";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumresearchpoint";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (sender instanceof PlayerEntity) {
			PlayerEntity player = (PlayerEntity) sender;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null) {
				sender.sendMessage(new StringTextComponent("Could not find magic wrapper"));
				return;
			}
			
			attr.addResearchPoint();
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (ServerPlayerEntity) player);
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a player"));
		}
	}

}
