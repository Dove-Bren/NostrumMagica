package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.research.NostrumResearch;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandAllResearch extends CommandBase {

	@Override
	public String getName() {
		return "nostrumresearch";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumresearch";
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
			
			for (NostrumResearch research : NostrumResearch.AllResearch()) {
				attr.completeResearch(research.getKey());
			}
			
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (ServerPlayerEntity) player);
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a player"));
		}
	}

}
