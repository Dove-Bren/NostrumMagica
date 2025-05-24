package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.block.dungeon.MagicBreakableContainerBlock;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

/**
 * Minor specialized BlockItem that allows wrapping chests up in a break block
 */
public class BreakBlockContainerItem extends BlockItem {

	public BreakBlockContainerItem(Block block, Properties itemProperties) {
		super(block, itemProperties);
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level worldIn = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player playerIn = context.getPlayer();
		
		if (pos == null) {
			return InteractionResult.PASS;
		}
		
		// if position clicked on is a chest, handle special. Otherwise just defer to parent
		BlockEntity te = worldIn.getBlockEntity(pos);
		if (te instanceof ChestBlockEntity) {
			if (!worldIn.isClientSide()) {
				if (!MagicBreakableContainerBlock.WrapChest(worldIn, pos)) {
					playerIn.sendMessage(new TextComponent("Failed to wrap chest"), Util.NIL_UUID);
				} else {
					playerIn.sendMessage(new TextComponent("Wrapped chest"), Util.NIL_UUID); 
				}
			}
			return InteractionResult.SUCCESS;
		}
		
		return super.useOn(context);
	}

}
