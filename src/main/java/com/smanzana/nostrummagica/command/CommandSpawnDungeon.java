package com.smanzana.nostrummagica.command;

import java.util.Random;

import com.smanzana.nostrummagica.spells.EMagicElement;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;
import com.smanzana.nostrummagica.spells.components.SpellShape;
import com.smanzana.nostrummagica.spells.components.SpellTrigger;
import com.smanzana.nostrummagica.world.NostrumDungeonGenerator;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;

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
		
		if (args.length == 0 || args.length == 1) {
			SpellComponentWrapper type = null;
			if (args.length == 0) {
				int i = rand.nextInt(3);
				int len;
				switch (i) {
				case 0:
					// ELEMENT
					len = EMagicElement.values().length;
					type = new SpellComponentWrapper(EMagicElement.values()[rand.nextInt(len)]);
					break;
				case 1:
					// SHAPE
					len = SpellShape.getAllShapes().size();
					type = new SpellComponentWrapper((SpellShape) SpellShape.getAllShapes().toArray()[rand.nextInt(len)]);
					break;
				case 2:
					// TRIGGER
					len = SpellTrigger.getAllTriggers().size();
					type = new SpellComponentWrapper((SpellTrigger) SpellTrigger.getAllTriggers().toArray()[rand.nextInt(len)]);
				}
			} else if (args[0].equalsIgnoreCase("dragon")) {
				if (sender instanceof EntityPlayer) {
					EntityPlayer player = ((EntityPlayer) sender);
					NostrumDungeonGenerator.DRAGON_DUNGEON.spawn(player.worldObj,
							new NostrumDungeon.DungeonExitPoint(player.getPosition(), 
									EnumFacing.HORIZONTALS[rand.nextInt(4)]
	        				));
				} else {
					throw new CommandException("This command must be run as a player");
				}
				return;
			} else {
				type = SpellComponentWrapper.fromKeyString(args[0]);
			}
			
			if (null == type) {
				throw new CommandException("Unknown type: " + (args.length == 1 ? args[0] : "AUTOGEN"));
			}
			
			NostrumDungeonGenerator.enqueueShrineRequest(type);
			NostrumDungeonGenerator.forceSpawn(0);
		}
		else {
			throw new CommandException("Too many arguments!");
		}
	}

}
