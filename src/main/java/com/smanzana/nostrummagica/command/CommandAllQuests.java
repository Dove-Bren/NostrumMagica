package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.quests.NostrumQuest;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandAllQuests extends CommandBase {

	@Override
	public String getName() {
		return "nostrumquests";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumquests";
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
			
			for (NostrumQuest quest : NostrumQuest.allQuests()) {
				attr.addQuest(quest.getKey());
				attr.completeQuest(quest.getKey());
			}
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (ServerPlayerEntity) player);
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a player"));
		}
	}

}
