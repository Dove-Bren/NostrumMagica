package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.items.PetSoulItem;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.Hand;

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
//		if (!(sender.getCommandSenderEntity() instanceof PlayerEntity)) {
//			throw new CommandException("Command must be run by a creative player");
//		}
//		
//		final PlayerEntity player = (PlayerEntity) sender.getCommandSenderEntity();
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
//			sender.sendMessage(new StringTextComponent("Player is not under that effect"));
//		} else {
//			String result = "Effect found with element {"
//					+ (data.getElement() == null ? "NULL" : data.getElement().getName())
//					+ "}, amount {"
//					+ data.getAmt()
//					+ "}, and count {"
//					+ data.getCount()
//					+ "}";
//			sender.sendMessage(new StringTextComponent(result));
//		}
		
		if (!(sender.getCommandSenderEntity() instanceof PlayerEntity)) {
			throw new CommandException("Command must be run by a creative player");
		}
		
		final PlayerEntity player = (PlayerEntity) sender.getCommandSenderEntity();
		if (!player.isCreative()) {
			throw new CommandException("Command must be run by a creative player");
		}
		
		ItemStack soulStack = player.getHeldItem(Hand.MAIN_HAND);
		if (soulStack.isEmpty() || !(soulStack.getItem() instanceof PetSoulItem)) {
			soulStack = player.getHeldItem(Hand.OFF_HAND);
		}
		
		if (soulStack.isEmpty() || !(soulStack.getItem() instanceof PetSoulItem)) {
			throw new CommandException("You must be holding a Pet Soul Item in one of your hands.");
		}
		
		if (null == PetSoulItem.SpawnPet(soulStack, player.world, player.getPositionVector())) {
			throw new CommandException("Failed to spawn entity");
		}
	}

}
