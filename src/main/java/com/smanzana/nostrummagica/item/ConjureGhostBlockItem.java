package com.smanzana.nostrummagica.item;

import com.smanzana.nostrummagica.block.dungeon.ConjureGhostBlock;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Minor specialized BlockItem that allows wrapping other blocks in a ghost block
 */
public class ConjureGhostBlockItem extends BlockItem {

	public ConjureGhostBlockItem(Block block, Properties itemProperties) {
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
		
		final BlockState state = worldIn.getBlockState(pos);
		if (state.isAir()) {
			return InteractionResult.FAIL;
		}
		
		if (!worldIn.isClientSide()) {
			if (!ConjureGhostBlock.WrapBlock(worldIn, pos)) {
				playerIn.sendMessage(new TextComponent("Failed to wrap block"), Util.NIL_UUID);
			} else {
				playerIn.sendMessage(new TextComponent("Wrapped block"), Util.NIL_UUID); 
			}
		}
		return InteractionResult.SUCCESS;
	}

}
