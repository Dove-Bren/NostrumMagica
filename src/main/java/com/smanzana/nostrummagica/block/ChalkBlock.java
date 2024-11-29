package com.smanzana.nostrummagica.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FourWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public class ChalkBlock extends FourWayBlock {

	public static final String ID = "nostrum_chalk_block";
	protected static final VoxelShape CHALK_AABB = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16 * 0.03125D, 16.0D);
	
	public ChalkBlock() {
		super(0, 0, 16, 16, 16, Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(.01f)
				.setLightLevel((state) -> 1)
				.noDrops()
				.notSolid()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(NORTH, false).with(EAST, false).with(SOUTH, false).with(WEST, false).with(WATERLOGGED, false));;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(NORTH, EAST, WEST, SOUTH, WATERLOGGED);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return CHALK_AABB;
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return Block.hasSolidSideOnTop(worldIn, pos.down());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (stateIn.get(WATERLOGGED)) {
			worldIn.getPendingFluidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickRate(worldIn));
		}
		
		// If below changed, possibly break
		if (facing == Direction.DOWN) {
			if (!this.isValidPosition(stateIn, worldIn, currentPos)) {
				return Blocks.AIR.getDefaultState();
			}
		}
		
		if (facing.getAxis().getPlane() == Direction.Plane.HORIZONTAL) {
			return stateIn.with(FACING_TO_PROPERTY_MAP.get(facing), Boolean.valueOf(this.canConnect(facingState)));
		}
		
		return super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	protected boolean canConnect(BlockState state) {
		return state.getBlock() == this;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		final World world = context.getWorld();
		final BlockPos pos = context.getPos();
		final FluidState fluidstate = world.getFluidState(pos);
		return super.getStateForPlacement(context)
				.with(NORTH, canConnect(world.getBlockState(pos.north())))
				.with(EAST, canConnect(world.getBlockState(pos.east())))
				.with(SOUTH, canConnect(world.getBlockState(pos.south())))
				.with(WEST, canConnect(world.getBlockState(pos.west())))
				.with(WATERLOGGED, Boolean.valueOf(fluidstate.getFluid() == Fluids.WATER))
				;
	}
	
//	@Override
//	public void onNeighborChange(BlockState state, IBlockReader world, BlockPos pos, BlockPos neighbor) {
//		if (world instanceof World && !((World) world).isRemote) {
//			if (!isValidPosition((World) world, pos)) {
//				((World) world).setBlockToAir(pos);
//			}
//		}
//	}
}
