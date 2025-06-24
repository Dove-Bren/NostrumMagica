package com.smanzana.nostrummagica.item.mapmaking;

import java.util.function.BiConsumer;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Map-making tool. Fills air with another block, optionally only going horizontally and down.
 * @author Skyler
 *
 */
public class ConnectedFillItem extends FillItem {

	protected final BiConsumer<Level, BlockPos> fillTransformer;
	
	public ConnectedFillItem(BiConsumer<Level, BlockPos> fillTransformer) {
		super(null, false);
		this.fillTransformer = fillTransformer;
	}
	
	@Override
	public InteractionResult useOn(UseOnContext context) {
		final Level world = context.getLevel();
		final BlockPos pos = context.getClickedPos();
		final Player player = context.getPlayer();
		
		if (world.isClientSide)
			return InteractionResult.SUCCESS;
		
		if (pos == null)
			return InteractionResult.PASS;
		
		if (player == null || !player.isCreative()) {
			if (player != null) {
				player.sendMessage(new TextComponent("You must be in creative to use this item"), Util.NIL_UUID);
			}
			return InteractionResult.SUCCESS;
		}
		
		final BlockPos startPos = pos; // unliek regular fill, we wnat the clicked block.relative(context.getClickedFace());
		
		// Verify it's not air
		if (world.isEmptyBlock(startPos)) {
			player.sendMessage(new TextComponent("You cannot use the item on air"), Util.NIL_UUID);
			return InteractionResult.SUCCESS;
		}
		
		fill(player, world, startPos);
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	protected BlockState getFillState() {
		return Blocks.AIR.defaultBlockState(); // should never be called
	}
	
	@Override
	protected void setState(FillContext contextIn, Level world, BlockPos pos) {
		this.fillTransformer.accept(world, pos);
	}
	
	@Override
	protected boolean shouldFill(FillContext contextIn, Level world, BlockPos pos) {
		ConnectedFillContext context = (ConnectedFillContext) contextIn;
		return world.getBlockState(pos) == context.startState;
	}
	
	@Override
	protected ConnectedFillContext makeContext(Player player, Level world, BlockPos start) {
		return new ConnectedFillContext(this, world, start);
	}
	
	protected static class ConnectedFillContext extends FillContext {
		public final BlockState startState;
		
		public ConnectedFillContext(ConnectedFillItem item, Level level, BlockPos start) {
			super(item, start);
			startState = level.getBlockState(start);
		}
	}
}
