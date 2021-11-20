package com.smanzana.nostrummagica.command;

import com.smanzana.nostrummagica.client.effects.ClientEffectRenderer;
import com.smanzana.nostrummagica.client.effects.ClientEffectRitual;
import com.smanzana.nostrummagica.items.ReagentItem.ReagentType;
import com.smanzana.nostrummagica.spells.EMagicElement;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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
		// Not real. Remove!
		BlockPos pos = new BlockPos(990, 80, 116);
		ItemStack centerF = new ItemStack(Items.DIAMOND);
		ItemStack outputF = new ItemStack(Items.ARROW);
		NonNullList<ItemStack> extrasF = NonNullList.create();
			extrasF.add(new ItemStack(Items.ACACIA_BOAT));
			extrasF.add(new ItemStack(Items.APPLE));
			extrasF.add(new ItemStack(Items.POTATO));
			extrasF.add(new ItemStack(Items.GOLD_INGOT));
		ReagentType types[] = new ReagentType[] {
				ReagentType.MANDRAKE_ROOT,
				ReagentType.MANDRAKE_ROOT,
				ReagentType.MANDRAKE_ROOT,
				ReagentType.MANDRAKE_ROOT,
		};
		
		ClientEffectRenderer.instance().addEffect(ClientEffectRitual.Create(
				new Vec3d(pos.getX() + .5, pos.getY() + 1, pos.getZ() + .5),
				EMagicElement.FIRE, centerF, null /*extrasF*/, types, outputF
				));
	}

}
