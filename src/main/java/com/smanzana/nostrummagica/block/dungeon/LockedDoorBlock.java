package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.item.WorldKeyItem;
import com.smanzana.nostrummagica.tile.LockedDoorTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class LockedDoorBlock extends MagicDoorBlock {

	public static final String ID = "locked_door";
	public static BooleanProperty UNLOCKABLE = LockedChestBlock.UNLOCKABLE;
	
	public LockedDoorBlock() {
		super();

		this.registerDefaultState(this.defaultBlockState().setValue(UNLOCKABLE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(UNLOCKABLE);
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return this.isMaster(state);
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		if (!this.isMaster(state))
			return null;
		
		return new LockedDoorTileEntity();
	}
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (!worldIn.isClientSide()) {
			BlockPos master = this.getMasterPos(worldIn, state, pos);
			if (master != null) {
				LockedDoorTileEntity door = (LockedDoorTileEntity) worldIn.getBlockEntity(master);
				final ItemStack heldItem = playerIn.getItemInHand(hand);
				
				if (playerIn.isCreative() && !heldItem.isEmpty() && heldItem.getItem() instanceof DyeItem) {
					DyeItem dye = (DyeItem) heldItem.getItem();
					door.setColor(dye.getDyeColor());
				} else if (playerIn.isCreative() && !heldItem.isEmpty() && heldItem.getItem() instanceof WorldKeyItem) {
					; // Do nothing and let item take care of it
					return ActionResultType.PASS;
				} else {
					door.attemptUnlock(playerIn);
				}
			}
		}
		
		return ActionResultType.SUCCESS;
	}
}
