package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.spells.EAlteration;
import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandUnlockAll extends CommandBase {

	@Override
	public String getCommandName() {
		return "nostrumunlockall";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/nostrumunlockall";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null) {
				sender.addChatMessage(new TextComponentString("Could not find magic wrapper"));
				return;
			}
			
			attr.unlock();
			
			for (SpellShape shape : SpellShape.getAllShapes()) {
				attr.addShape(shape);
			}
			for (SpellTrigger trigger : SpellTrigger.getAllTriggers()) {
				attr.addTrigger(trigger);
			}
			for (EAlteration alt : EAlteration.values()) {
				attr.unlockAlteration(alt);
			}
			for (EMagicElement elem : EMagicElement.values()) {
				attr.learnElement(elem);
				attr.setElementMastery(elem, 3);
			}
			
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (EntityPlayerMP) player);
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
