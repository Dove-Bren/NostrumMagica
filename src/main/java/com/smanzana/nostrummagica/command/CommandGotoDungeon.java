package com.smanzana.nostrummagica.command;

import java.util.List;

import com.smanzana.nostrummagica.items.SeekerIdol;
import com.smanzana.nostrummagica.spells.components.SpellComponentWrapper;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.event.ClickEvent;
import net.minecraft.util.text.event.HoverEvent;

public class CommandGotoDungeon extends CommandBase {

	@Override
	public String getCommandName() {
		return "tpdung";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/tpdung [type]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			
			if (args.length > 0) {
				// Get type
				SpellComponentWrapper wrapper = SpellComponentWrapper.fromKeyString(args[0]);
				
				BlockPos pos = SeekerIdol.findNearest(player.worldObj, player.getPosition(), wrapper);
				if (pos == null) {
					sender.addChatMessage(new TextComponentString("No dungeons of type " + wrapper.getKeyString() + " found"));
				} else {
					player.setPositionAndUpdate(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5);
				}
			} else {
				List<SpellComponentWrapper> types = SeekerIdol.getKnownDungeonTypes(player.worldObj);
				
				if (types.isEmpty()) {
					sender.addChatMessage(new TextComponentString("No dungeons have been generated yet! Explore to find more!"));
				} else {
					sender.addChatMessage(new TextComponentString("Select which type of dungeon to jump to:"));
					for (SpellComponentWrapper type : types) {
						sender.addChatMessage(new TextComponentString(type.toString())
								.setStyle(new Style()
										.setColor(TextFormatting.DARK_GREEN)
										.setUnderlined(true)
										.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponentString("Click to go!")))
										.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/" + this.getCommandName() + " " + type.getKeyString()))));
					}
				}
			}
			
			
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
