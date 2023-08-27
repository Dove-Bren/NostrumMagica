package com.smanzana.nostrummagica.command;

import java.util.LinkedList;

import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomRegistry;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.StringTextComponent;

public class CommandWriteRoom extends CommandBase {

	@Override
	public String getName() {
		return "writeroom";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/writeroom [name]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			throw new CommandException("Invalid number of arguments. Expected a room name");
		}
		
		if (sender instanceof PlayerEntity && ((PlayerEntity) sender).isCreative()) {
			PlayerEntity player = (PlayerEntity) sender;
			
			// Must be holding two position crystals in hands with corners selected
			ItemStack main = player.getHeldItemMainhand();
			ItemStack offhand = player.getHeldItemOffhand();
			if ((main.isEmpty() || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)
				|| (offhand.isEmpty() || !(offhand.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(offhand) == null)) {
				sender.sendMessage(new StringTextComponent("You must be holding a filled geogem in both of your hands"));
			} else {
				RoomBlueprint blueprint = new RoomBlueprint(player.world,
						PositionCrystal.getBlockPosition(main),
						PositionCrystal.getBlockPosition(offhand),
						true);
				
				if (DungeonRoomRegistry.instance().writeRoomAsFile(blueprint, args[0], 1, new LinkedList<>())) {
					sender.sendMessage(new StringTextComponent("Room written!"));
				} else {
					sender.sendMessage(new StringTextComponent("An error was encountered while writing the room"));
				}
			}
		} else {
			sender.sendMessage(new StringTextComponent("This command must be run as a creative player"));
		}
	}

}
