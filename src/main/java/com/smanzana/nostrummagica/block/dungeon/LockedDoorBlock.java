package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.item.WorldKeyItem;
import com.smanzana.nostrummagica.tile.LockedDoorTileEntity;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class LockedDoorBlock extends MagicDoorBlock implements EntityBlock {

	public static final String ID = "locked_door";
	public static BooleanProperty UNLOCKABLE = LockedChestBlock.UNLOCKABLE;
	
	public LockedDoorBlock() {
		super();

		this.registerDefaultState(this.defaultBlockState().setValue(UNLOCKABLE, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(UNLOCKABLE);
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		if (!this.isMaster(state))
			return null;
		
		return new LockedDoorTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.LockedDoorType);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player playerIn, InteractionHand hand, BlockHitResult hit) {
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
					return InteractionResult.PASS;
				} else {
					door.attemptUnlock(playerIn);
				}
			}
		}
		
		return InteractionResult.SUCCESS;
	}
}
