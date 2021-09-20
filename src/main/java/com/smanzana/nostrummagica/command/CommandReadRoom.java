package com.smanzana.nostrummagica.command;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.world.blueprints.RoomBlueprint;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.TextComponentString;

public class CommandReadRoom extends CommandBase {

	@Override
	public String getName() {
		return "readroom";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/readroom [name]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (args.length != 1) {
			throw new CommandException("Invalid number of arguments. Expected a room name");
		}
		
		if (sender instanceof EntityPlayer && ((EntityPlayer) sender).isCreative()) {
			EntityPlayer player = (EntityPlayer) sender;
			
			// Must be a position crystals in hand with low corner selected
			ItemStack main = player.getHeldItemMainhand();
			if ((main.isEmpty() || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)) {
				sender.sendMessage(new TextComponentString("You must be holding a filled geogem in your main hand"));
			} else {
				
				File file = new File(ModConfig.config.base.getConfigFile().getParentFile(), "NostrumMagica/dungeon_room_captures/" + args[0] + ".dat");
				if (!file.exists()) {
					file = new File(ModConfig.config.base.getConfigFile().getParentFile(), "NostrumMagica/dungeon_room_captures/" + args[0] + ".gat");
				}
				if (file.exists()) {
					NBTTagCompound nbt = null;
					try {
						if (file.getName().endsWith(".gat")) {
							nbt = CompressedStreamTools.readCompressed(new FileInputStream(file));
						} else {
							nbt = CompressedStreamTools.read(file);
						}
						sender.sendMessage(new TextComponentString("Room read from " + file.getPath()));
					} catch (IOException e) {
						e.printStackTrace();
						
						System.out.println("Failed to read out serialized file " + file.toString());
						sender.sendMessage(new TextComponentString("Failed to read room"));
					}
					
					if (nbt != null) {
						RoomBlueprint blueprint = RoomBlueprint.fromNBT((NBTTagCompound) nbt.getTag("blueprint"));
						if (blueprint != null) {
							blueprint.spawn(player.world, PositionCrystal.getBlockPosition(main), EnumFacing.EAST);
						} else {
							sender.sendMessage(new TextComponentString("Room failed to load"));
						}
					}
				} else {
					sender.sendMessage(new TextComponentString("Room not found"));
				}
			}
		} else {
			sender.sendMessage(new TextComponentString("This command must be run as a creative player"));
		}
	}

}
