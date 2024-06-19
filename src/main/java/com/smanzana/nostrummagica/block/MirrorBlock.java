package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class MirrorBlock extends HorizontalBlock {
	
	public static final String ID = "mirror_block";
	protected static final VoxelShape MIRROR_AABB_EW = Block.makeCuboidShape(16 * 0.4D, 16 * 0.0D, 16 * 0.1D, 16 * 0.6D, 16 * 1.05D, 16 * 0.9D);
	protected static final VoxelShape MIRROR_AABB_NS = Block.makeCuboidShape(16 * 0.1D, 16 * 0.0D, 16 * 0.4D, 16 * 0.9D, 16 * 1.05D, 16 * 0.6D);
	
	public MirrorBlock() {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(4f, 20f)
				.sound(SoundType.STONE)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(0)
				.setLightLevel((state) -> 4)
				);
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction direction = context.getPlacementHorizontalFacing().getOpposite();
		BlockPos blockpos = context.getPos();
		BlockPos blockpos1 = blockpos.offset(direction);
		return context.getWorld().getBlockState(blockpos1).isReplaceable(context) ? this.getDefaultState().with(HORIZONTAL_FACING, direction) : null;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
//	@Override
//	public boolean isVisuallyOpaque() {
//		return false;
//	}
	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isFullCube(BlockState state) {
//        return false;
//    }
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(HORIZONTAL_FACING);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		NostrumMagica.instance.proxy.openMirrorScreen();
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos, ISelectionContext context) {
		if (state.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
}
