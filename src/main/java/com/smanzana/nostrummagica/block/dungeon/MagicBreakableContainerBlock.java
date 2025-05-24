package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.tile.BreakContainerTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class MagicBreakableContainerBlock extends MagicBreakableBlock implements EntityBlock {

	public static final String ID = "mechblock_break_container";
	
	public MagicBreakableContainerBlock() {
		super();
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
		InteractionResult result = super.use(state, worldIn, pos, playerIn, hand, hit);
		if (result == InteractionResult.PASS) {
			if (!playerIn.isCreative()) {
				return InteractionResult.PASS;
			}
			
			if (hand != InteractionHand.MAIN_HAND) {
				return InteractionResult.PASS;
			}
			
			if (worldIn.isClientSide) {
				return InteractionResult.PASS;
			}
			
			final ItemStack heldItem = playerIn.getItemInHand(hand);
			if (!heldItem.isEmpty()) {
				addContainedItem(worldIn, state, pos, heldItem.copy());
				return InteractionResult.SUCCESS;
			}
			
			if (heldItem.isEmpty() && playerIn.isCrouching()) {
				
			}
		}
		
		return InteractionResult.PASS;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new BreakContainerTileEntity(pos, state);
	}
	
	protected void addContainedItem(Level level, BlockState state, BlockPos pos, ItemStack item) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te != null && te instanceof BreakContainerTileEntity entity) {
			entity.addItem(item);
		}
	}
	
	protected void clearContainedItems(Level level, BlockState state, BlockPos pos, ItemStack item) {
		BlockEntity te = level.getBlockEntity(pos);
		if (te != null && te instanceof BreakContainerTileEntity entity) {
			entity.clearContent();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void onRemove(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull BlockState newState, boolean isMoving) {
		boolean blockChanged = !state.is(newState.getBlock());
		if (blockChanged) {
			BlockEntity te = world.getBlockEntity(pos);
			if (te != null && te instanceof BreakContainerTileEntity entity) {
				Containers.dropContents(world, pos, entity);
			}
			
			super.onRemove(state, world, pos, newState, isMoving);
		}
	}
	
	@Override
	protected void triggerInternal(Level world, BlockPos blockPos, BlockState state) {
		// Allow block entity to handle triggering specially if it wants
		BlockEntity te = world.getBlockEntity(blockPos);
		if (te != null && te instanceof BreakContainerTileEntity entity) {
			if (entity.handleTrigger()) {
				return; // do nothing
			}
		}
		
		// Let super handle it, which will remove the block and drop items
		super.triggerInternal(world, blockPos, state);
	}

	public static boolean WrapChest(Level world, BlockPos pos) {
		BlockEntity te = world.getBlockEntity(pos);
		if (te instanceof ChestBlockEntity chest) {
			SimpleContainer invCopy = new SimpleContainer(chest.getContainerSize());
			
			for (int i = 0; i < invCopy.getContainerSize(); i++) {
				invCopy.setItem(i, chest.removeItemNoUpdate(i));
			}
			
			world.setBlock(pos, NostrumBlocks.breakContainerBlock.defaultBlockState(), 3);
			
			BreakContainerTileEntity container = (BreakContainerTileEntity) world.getBlockEntity(pos);
			container.setContents(invCopy);
			return true;
		} else {
			return false;
		}
	}
}
