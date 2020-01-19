package com.smanzana.nostrummagica.command;

import java.util.Random;

import com.smanzana.nostrummagica.world.NostrumDungeonGenerator.DungeonGen;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;

public class CommandSpawnDungeon extends CommandBase {

	private static Random rand = null;
	
	@Override
	public String getCommandName() {
		return "NostrumSpawnDungeon";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/NostrumSpawnDungeon [type]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (CommandSpawnDungeon.rand == null) {
			CommandSpawnDungeon.rand = new Random();
		}
		
		if (!(sender instanceof EntityPlayer)) {
			throw new CommandException("This command must be run as a player");
		}
		
		if (args.length == 0 || args.length == 1) {
			DungeonGen type;
			if (args.length == 0) {
				DungeonGen[] types = DungeonGen.values();
				type = types[rand.nextInt(types.length)];
			} else {
				try {
					type = DungeonGen.valueOf(args[0].toUpperCase());
				} catch (Exception e) {
					type = null;
				}
			}
			
			if (null == type) {
				throw new CommandException("Unknown type: " + (args.length == 1 ? args[0] : "AUTOGEN"));
			}
			
			EntityPlayer player = ((EntityPlayer) sender);
			type.getGenerator().generate(player.worldObj, rand, player.getPosition());
		}
		else {
			throw new CommandException("Too many arguments!");
		}
	}

}
