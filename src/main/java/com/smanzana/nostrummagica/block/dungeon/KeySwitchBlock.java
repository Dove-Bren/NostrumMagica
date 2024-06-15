package com.smanzana.nostrummagica.block.dungeon;

import com.smanzana.nostrummagica.tile.KeySwitchBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.DyeItem;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

/**
 * Houses a switch that has to be interacted with in order to acquire a world key
 * @author Skyler
 *
 */
public class KeySwitchBlock extends SwitchBlock {
	
	protected static final VoxelShape SWITCH_BLOCK_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16D, 3.2D, 16D);

	public static final String ID = "key_switch_block";
	
	public KeySwitchBlock() {
		super();
	}
	
//	@Override
//	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
//        return true;
//    }
	
//	@Override
//	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//		return SWITCH_BLOCK_AABB;
//	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new KeySwitchBlockTileEntity();
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote || !player.isCreative()) {
			return ActionResultType.FAIL;
		}

		if (player.isCreative() && !player.getHeldItemMainhand().isEmpty() && player.getHeldItemMainhand().getItem() instanceof DyeItem) {
			KeySwitchBlockTileEntity ent = (KeySwitchBlockTileEntity) worldIn.getTileEntity(pos);
			DyeItem dye = (DyeItem) player.getHeldItemMainhand().getItem();
			ent.setColor(dye.getDyeColor());
			return ActionResultType.SUCCESS;
		}
		
		return ActionResultType.FAIL;
	}
	
}
