package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;

import baubles.api.cap.BaublesCapabilities;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.WorldServer;

public class CommandDebugEffect extends CommandBase {

	@Override
	public String getName() {
		return "nostrumdebugeffect";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumdebugeffect [effect]";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//		if (!(sender.getCommandSenderEntity() instanceof EntityPlayer)) {
//			throw new CommandException("Command must be run by a creative player");
//		}
//		
//		final EntityPlayer player = (EntityPlayer) sender.getCommandSenderEntity();
//		if (!player.isCreative()) {
//			throw new CommandException("Command must be run by a creative player");
//		}
//		
//		if (args.length != 1)
//			throw new CommandException("Invalid number of arguments. Only the effect name is expected.");
//		
//		SpecialEffect effect;
//		try {
//			effect = SpecialEffect.valueOf(args[0].toUpperCase());
//		} catch (Exception e) {
//			effect = null;
//		}
//		
//		if (effect == null) {
//			throw new CommandException("Could not lookup effect with name \"" + args[0] + "\"");
//		}
//		
//		EffectData data = NostrumMagica.magicEffectProxy.getData(player, effect);
//		if (data == null) {
//			sender.sendMessage(new TextComponentString("Player is not under that effect"));
//		} else {
//			String result = "Effect found with element {"
//					+ (data.getElement() == null ? "NULL" : data.getElement().getName())
//					+ "}, amount {"
//					+ data.getAmt()
//					+ "}, and count {"
//					+ data.getCount()
//					+ "}";
//			sender.sendMessage(new TextComponentString(result));
//		}
		
		NostrumMagica.logger.warn("\\/\\/\\/\\/\\/\\/ Iterating players");
		for (WorldServer world : server.worlds)
		for (EntityPlayer player : world.playerEntities) {
			if (player.equals(sender)) {
				continue;
			}
			
			final IInventory baubles = NostrumMagica.baubles.getBaubles(player);
			
			NostrumMagica.logger.warn("Player " + player);
			NostrumMagica.logger.warn("  Baubles present: " + (baubles == null ? "NO" : "YES"));
			if (baubles!=null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					final ItemStack stack = baubles.getStackInSlot(i);
					if (stack.isEmpty()) {
						NostrumMagica.logger.warn("   1> EMPTY");
					} else {
						NostrumMagica.logger.warn("   1> " + stack.toString());
						NostrumMagica.logger.warn("   2> " + (stack.hasCapability(BaublesCapabilities.CAPABILITY_ITEM_BAUBLE, null) ? "Bauble CAP" : "No Capability"));
					}
				}
			}
		}
		NostrumMagica.logger.warn("/\\/\\/\\/\\/\\/\\ Iterating players");
	}

}
