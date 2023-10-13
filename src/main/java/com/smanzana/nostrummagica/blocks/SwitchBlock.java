package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.items.PositionCrystal;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.tiles.SwitchBlockTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.EnderEyeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * Houses a switch that has to be interacted iwth in order to proc other mechanisms
 * @author Skyler
 *
 */
public class SwitchBlock extends Block {
	
	protected static final VoxelShape SWITCH_BLOCK_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1D, 0.2D, 1D);

	public static final String ID = "switch_block";
	
	public SwitchBlock() {
		super(Block.Properties.create(Material.BARRIER)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.lightValue(8)
				);
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return true;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return SWITCH_BLOCK_AABB;
	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
	
//	@Override
//	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
//        return false;
//    }
	
	@Override
	public int getOpacity(BlockState state, IBlockReader world, BlockPos pos) {
		return 0;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new SwitchBlockTileEntity();
	}
	
//	@Override
//	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
//		super.breakBlock(world, pos, state);
//		world.removeTileEntity(pos);
//	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity playerIn, Hand hand, BlockRayTraceResult hit) {
		if (worldIn.isRemote || !playerIn.isCreative()) {
			return false;
		}
		
		ItemStack heldItem = playerIn.getHeldItem(hand);
		
		if (!heldItem.isEmpty() && heldItem.getItem() instanceof PositionCrystal) {
			BlockPos heldPos = PositionCrystal.getBlockPosition(heldItem);
			if (heldPos != null && PositionCrystal.getDimension(heldItem) == worldIn.getDimension().getType().getId()) {
				TileEntity te = worldIn.getTileEntity(pos);
				if (te != null) {
					SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
					ent.offsetTo(heldPos);
					NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
				}
			}
			return true;
		} else if (!heldItem.isEmpty() && heldItem.getItem() instanceof EnderEyeItem) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				BlockPos loc = ent.getOffset().toImmutable().add(pos);
				BlockState atState = worldIn.getBlockState(loc);
				if (atState != null && atState.getBlock() instanceof ITriggeredBlock) {
					playerIn.setPositionAndUpdate(loc.getX(), loc.getY(), loc.getZ());
				} else {
					playerIn.sendMessage(new StringTextComponent("Not pointed at valid triggered block!"));
				}
			}
		} else if (heldItem.isEmpty() && hand == Hand.MAIN_HAND) {
			TileEntity te = worldIn.getTileEntity(pos);
			if (te != null) {
				SwitchBlockTileEntity ent = (SwitchBlockTileEntity) te;
				ent.setType(ent.getSwitchType() == SwitchBlockTileEntity.SwitchType.ANY ? SwitchBlockTileEntity.SwitchType.MAGIC : SwitchBlockTileEntity.SwitchType.ANY);
				NostrumMagicaSounds.STATUS_BUFF1.play(worldIn, pos.getX(), pos.getY(), pos.getZ());
			}
			return true;
		}
		
		return false;
	}
	
}
