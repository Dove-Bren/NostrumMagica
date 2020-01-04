package com.smanzana.nostrummagica.command;

import java.io.File;
import java.io.IOException;

import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.world.dungeon.room.DungeonRoomSerializationHelper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandReadRoom extends CommandBase {

	@Override
	public String getCommandName() {
		return "readroom";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
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
			if ((main == null || !(main.getItem() instanceof PositionCrystal) || PositionCrystal.getBlockPosition(main) == null)) {
				sender.addChatMessage(new TextComponentString("You must be holding a filled geogem in your main hand"));
			} else {
				
//				NBTTagCompound nbt = new NBTTagCompound();
//				nbt.setString("name", args[0]);
//				nbt.setTag("blocks", DungeonRoomSerializationHelper.serialize(player.worldObj,
//						PositionCrystal.getBlockPosition(main),
//						PositionCrystal.getBlockPosition(offhand)));
				
				File file = new File(ModConfig.config.base.getConfigFile().getParentFile(), "NostrumMagica/dungeon_rooms/captures/capture_" + args[0] + ".dat");
				if (file.exists()) {
					NBTTagCompound nbt = null;
					try {
						nbt = CompressedStreamTools.read(file);
						sender.addChatMessage(new TextComponentString("Room read from " + file.getPath()));
					} catch (IOException e) {
						e.printStackTrace();
						
						System.out.println("Failed to read out serialized file " + file.toString());
						sender.addChatMessage(new TextComponentString("Failed to read room"));
					}
					
					if (nbt != null) {
						if (!DungeonRoomSerializationHelper.loadFromNBT(player.worldObj, PositionCrystal.getBlockPosition(main), nbt.getTag("blocks"))) {
							sender.addChatMessage(new TextComponentString("Room failed to load"));
						}
					}
				} else {
					sender.addChatMessage(new TextComponentString("Room not found"));
				}
			}
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a creative player"));
		}
	}

}
