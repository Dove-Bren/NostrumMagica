package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;

public class CommandDebugClientEffect extends CommandDebugEffect {

	@Override
	public String getName() {
		return "nostrumdebugeffectclient";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/nostrumdebugeffectclient [effect]";
	}
	
	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
//		// Not real. Remove!
//		BlockPos pos = new BlockPos(990, 80, 116);
//		ItemStack centerF = new ItemStack(Items.DIAMOND);
//		ItemStack outputF = new ItemStack(Items.ARROW);
//		NonNullList<ItemStack> extrasF = NonNullList.create();
//			extrasF.add(new ItemStack(Items.ACACIA_BOAT));
//			extrasF.add(new ItemStack(Items.APPLE));
//			extrasF.add(new ItemStack(Items.POTATO));
//			extrasF.add(new ItemStack(Items.GOLD_INGOT));
//		ReagentType types[] = new ReagentType[] {
//				ReagentType.MANDRAKE_ROOT,
//				ReagentType.MANDRAKE_ROOT,
//				ReagentType.MANDRAKE_ROOT,
//				ReagentType.MANDRAKE_ROOT,
//		};
//		
//		ClientEffectRenderer.instance().addEffect(ClientEffectRitual.Create(
//				new Vec3d(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5),
//				EMagicElement.FIRE, centerF, null /*extrasF*/, types, outputF
//				));
		
		NostrumMagica.logger.warn("\\/\\\\/\\\\/\\\\/\\\\/\\\\/ Iterating players");
		for (EntityPlayer player : Minecraft.getMinecraft().player.world.playerEntities) {
			if (player.equals(sender)) {
				continue;
			}
			
			final IInventory baubles = NostrumMagica.baubles.getBaubles(player);
			
			NostrumMagica.logger.warn("Player " + player);
			NostrumMagica.logger.warn("  Baubles present: " + (baubles == null ? "NO" : "YES"));
			if (baubles!=null) {
				for (int i = 0; i < baubles.getSizeInventory(); i++) {
					final ItemStack stack = baubles.getStackInSlot(i);
					NostrumMagica.logger.warn("   1> " + (stack.isEmpty() ? "EMPTY" : stack.toString()));
				}
			}
		}
		NostrumMagica.logger.warn("/\\\\/\\\\/\\\\/\\\\/\\\\/\\\\ Iterating players");
		
	}

}
