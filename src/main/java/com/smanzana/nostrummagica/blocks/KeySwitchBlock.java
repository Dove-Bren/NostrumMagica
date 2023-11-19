package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.tiles.KeySwitchBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.tileentity.TileEntity;
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
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote || !playerIn.isCreative()) {
			return false;
		}
		
		//ItemStack heldItem = playerIn.getHeldItem(hand);
		
//		if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
//			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
//			if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.getDimension().getType().getId()) {
//				TileEntity te = worldIn.getTileEntity(pos);
//				if (te != null) {
//					KeySwitchBlockTileEntity ent = (KeySwitchBlockTileEntity) te;
//					ent.offsetTo(heldPos);
//					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
//				}
//			}
//			return true;
//		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof EnderEyeItem) {
//			TileEntity te = worldIn.getTileEntity(pos);
//			if (te != null) {
//				KeySwitchBlockTileEntity ent = (KeySwitchBlockTileEntity) te;
//				BlockPos loc = ent.getOffset().toImmutable().add(pos);
//				BlockState atState = worldIn.getBlockState(loc);
//				if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
//					playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
//				} else {
//					playerIn.sendMessage(new StringTextComponent("Not pointed at valid triggered block!"));
//				}
//			}
//		} else if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
//			TileEntity te = worldIn.getTileEntity(pos);
//			if (te != null) {
//				KeySwitchBlockTileEntity ent = (KeySwitchBlockTileEntity) te;
//				ent.setType(ent.getSwitchType() == KeySwitchBlockTileEntity.SwitchType.ANY ? KeySwitchBlockTileEntity.SwitchType.MAGIC : KeySwitchBlockTileEntity.SwitchType.ANY);
//				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
//			}
//			return true;
//		}
		
		return false;
	}
	
}
