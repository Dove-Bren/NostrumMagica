package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class MirrorBlock extends HorizontalDirectionalBlock {
	
	public static final String ID = "mirror_block";
	protected static final VoxelShape MIRROR_AABB_EW = Block.box(16 * 0.4D, 16 * 0.0D, 16 * 0.1D, 16 * 0.6D, 16 * 1.05D, 16 * 0.9D);
	protected static final VoxelShape MIRROR_AABB_NS = Block.box(16 * 0.1D, 16 * 0.0D, 16 * 0.4D, 16 * 0.9D, 16 * 1.05D, 16 * 0.6D);
	
	public MirrorBlock() {
		super(Block.Properties.of(Material.STONE)
				.strength(4f, 20f)
				.sound(SoundType.STONE)
				.lightLevel((state) -> 4)
				);
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction direction = context.getHorizontalDirection().getOpposite();
		BlockPos blockpos = context.getClickedPos();
		BlockPos blockpos1 = blockpos.relative(direction);
		return context.getLevel().getBlockState(blockpos1).canBeReplaced(context) ? this.defaultBlockState().setValue(FACING, direction) : null;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (worldIn.isClientSide()) {
			NostrumMagica.instance.proxy.openMirrorScreen();
		}
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		if (state.getValue(FACING).get2DDataValue() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
}
