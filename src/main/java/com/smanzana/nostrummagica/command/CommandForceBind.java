package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.capabilities.INostrumMagic;
import com.smanzana.nostrummagica.items.SpellScroll;
import com.smanzana.nostrummagica.items.SpellTome;
import com.smanzana.nostrummagica.network.NetworkHandler;
import com.smanzana.nostrummagica.network.messages.StatSyncMessage;
import com.smanzana.nostrummagica.spells.Spell;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.text.TextComponentString;

public class CommandForceBind extends CommandBase {

	@Override
	public String getCommandName() {
		return "nostrumbind";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "/nostrumbind";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		
		if (args.length != 0)
			throw new CommandException("Invalid number of arguments. Expected no arguments");
		
		if (sender instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) sender;
			INostrumMagic attr = NostrumMagica.getMagicWrapper(player);
			if (attr == null) {
				sender.addChatMessage(new TextComponentString("Could not find magic wrapper"));
				return;
			}
			
			ItemStack stack = player.getHeldItemMainhand();
			if (stack == null || !(stack.getItem() instanceof SpellTome)) {
				sender.addChatMessage(new TextComponentString("To force a bind, hold the tome that's being binded to in your main hand"));
				return;
			}
			if (attr.isBinding()) {
				attr.completeBinding(stack);
			} else {
				ItemStack offhand = player.getHeldItemOffhand();
				if (offhand == null || !(offhand.getItem() instanceof SpellScroll)
						|| SpellScroll.getSpell(offhand) == null) {
					sender.addChatMessage(new TextComponentString("Either use while holding a tome that's currently binding OR hold a spell scroll in your offhand"));
				} else {
					Spell spell = SpellScroll.getSpell(offhand);
					attr.startBinding(spell, null, SpellTome.getTomeID(stack));
					attr.completeBinding(stack);
				}
			}
			NetworkHandler.getSyncChannel().sendTo(
					new StatSyncMessage(attr)
					, (EntityPlayerMP) player);
		} else {
			sender.addChatMessage(new TextComponentString("This command must be run as a player"));
		}
	}

}
