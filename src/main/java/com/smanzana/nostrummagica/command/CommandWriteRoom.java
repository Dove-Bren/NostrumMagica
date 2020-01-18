package com.smanzana.nostrummagica.command;

import java.util.LinkedList;

import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandWriteRoom extends CommandBase {

	@Override
	public String getCommandName() {
		return "writeroom";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/writeroom [name]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			throw new CommandException("Invalid number of arguments. Expected a room name");
		}
		
		if (sender instanceof EntityPlayer && ((EntityPlayer) sender).isCreative()) {
			EntityPlayer player = (EntityPlayer) sender;
			
			// Must be holding two position crystals in hands with corners selected
			ItemStack main = player.getHeldItemMainhand();
			ItemStack offhand = player.getHeldItemOffhand();
			if ((main == null || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)
				|| (offhand == null || !(offhand.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(offhand) == null)) {
				sender.addChatMessage(new TextComponentString("You must be holding a filled geogem in both of your hands"));
			} else {
				RoomBlueprint blueprint = new RoomBlueprint(player.worldObj,
						PositionCrystal.getBlockPosition(main),
						PositionCrystal.getBlockPosition(offhand));
				
				if (DungeonRoomRegistry.instance().writeRoomAsFile(blueprint, args[0], 1, new LinkedList<>())) {
					sender.addChatMessage(new TextComponentString("Room written!"));
				} else {
					sender.addChatMessage(new TextComponentString("An error was encountered while writing the room"));
				}
			}
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a creative player"));
		}
	}

}
