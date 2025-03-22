package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.tile.KeySwitchBlockTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Houses a switch that has to be interacted with in order to acquire a world key
 * @author Skyler
 *
 */
public class KeySwitchBlock extends SwitchBlock {
	
	protected static final VoxelShape SWITCH_BLOCK_AABB = Block.box(0.0D, 0.0D, 0.0D, 16D, 3.2D, 16D);

	public static final String ID = "key_switch_block";
	
	public KeySwitchBlock() {
		super();
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeySwitchBlockTileEntity(pos, state);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
		if (worldIn.isClientSide || !player.isCreative()) {
			return InteractionResult.FAIL;
		}

		if (player.isCreative() && !player.getMainHandItem().isEmpty() && player.getMainHandItem().getItem() instanceof DyeItem) {
			KeySwitchBlockTileEntity ent = (KeySwitchBlockTileEntity) worldIn.getBlockEntity(pos);
			DyeItem dye = (DyeItem) player.getMainHandItem().getItem();
			ent.setColor(dye.getDyeColor());
			return InteractionResult.SUCCESS;
		}
		
		return InteractionResult.FAIL;
	}
	
}
