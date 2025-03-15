package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.tile.ItemDuctTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.block.SixWayBlock;
import net.minecraft.block.material.Material;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * It's an item pipe!
 * @author Skyler
 *
 */
@SuppressWarnings("deprecation")
public class ItemDuctBlock extends SixWayBlock implements IWaterLoggable {
	
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	
//	public static final BooleanProperty NORTH = BooleanProperty.create("north");
//	public static final BooleanProperty SOUTH = BooleanProperty.create("south");
//	public static final BooleanProperty EAST = BooleanProperty.create("east");
//	public static final BooleanProperty WEST = BooleanProperty.create("west");
//	public static final BooleanProperty UP = BooleanProperty.create("up");
//	public static final BooleanProperty DOWN = BooleanProperty.create("down");
	
	private static final float INNER_RADIUS = (3.0f / 16.0f);
//	protected static final VoxelShape BASE_AABB = Block.makeCuboidShape(.5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS);
//	protected static final VoxelShape NORTH_AABB = Block.makeCuboidShape(.5 - INNER_RADIUS, .5 - INNER_RADIUS, 0, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 - INNER_RADIUS);
//	protected static final VoxelShape SOUTH_AABB = Block.makeCuboidShape(.5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .1);
//	protected static final VoxelShape WEST_AABB = Block.makeCuboidShape(0, .5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS);
//	protected static final VoxelShape EAST_AABB = Block.makeCuboidShape(.5 + INNER_RADIUS, .5 - INNER_RADIUS, .5 - INNER_RADIUS, 1, .5 + INNER_RADIUS, .5 + INNER_RADIUS);
//	protected static final VoxelShape UP_AABB = Block.makeCuboidShape(.5 - INNER_RADIUS, .5 - INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, .5 + INNER_RADIUS, 1);
//	protected static final VoxelShape DOWN_AABB = Block.makeCuboidShape(.5 + INNER_RADIUS, .5 - INNER_RADIUS, 0, 1, .5 + INNER_RADIUS, .5 - INNER_RADIUS);
//	protected static final VoxelShape SIDE_AABBs[] = {DOWN_AABB, UP_AABB, NORTH_AABB, SOUTH_AABB, WEST_AABB, EAST_AABB}; // Direction 'index' is index
	
	// Large static pre-made set of selection AABBs.
	// This is an array like fence has where index is bit field of different options.
	// these are:
	// D U N S W E (the Direction 'index's)
	// so the BB for a duct with north and east is [001001] (9)
//	protected static final VoxelShape SELECTION_AABS[] = {
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.3125, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.3125, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.3125, 0.0, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.3125, 0.0, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 0.6875, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 1.0, 0.6875, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 0.6875, 0.6875, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 1.0, 0.6875, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.3125, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.3125, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 0.6875, 1.0, 0.6875),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 1.0, 1.0, 0.6875),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.3125, 0.0, 0.0, 1.0, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 0.6875, 1.0, 1.0),
//			Block.makeCuboidShape(0.0, 0.0, 0.0, 1.0, 1.0, 1.0),
//	};
	
	
	public static final String ID = "item_duct";
	
	public ItemDuctBlock() {
		super(INNER_RADIUS, Block.Properties.of(Material.METAL));
		
		this.registerDefaultState(this.stateDefinition.any()
				.setValue(NORTH, false)
				.setValue(SOUTH, false)
				.setValue(EAST, false)
				.setValue(WEST, false)
				.setValue(UP, false)
				.setValue(DOWN, false)
				.setValue(WATERLOGGED, false));
	}
	
	public static boolean GetFacingActive(BlockState state, Direction face) {
		switch (face) {
		case DOWN:
			return state.getValue(DOWN);
		case EAST:
			return state.getValue(EAST);
		case NORTH:
		default:
			return state.getValue(NORTH);
		case SOUTH:
			return state.getValue(SOUTH);
		case UP:
			return state.getValue(UP);
		case WEST:
			return state.getValue(WEST);
		}
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED);
	}
	
	/**
	 * Called on server when World#addBlockEvent is called. If server returns true, then also called on the client. On
	 * the Server, this may perform additional changes to the world, like pistons replacing the block with an extended
	 * base. On the client, the update may involve replacing tile entities or effects such as sounds or particles
	 * @deprecated call via {@link IBlockState#onBlockEventReceived(World,BlockPos,int,int)} whenever possible.
	 * Implementing/overriding is fine.
	 */
	public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		super.triggerEvent(state, worldIn, pos, id, param);
		TileEntity tileentity = worldIn.getBlockEntity(pos);
		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}

	@Nullable
	public INamedContainerProvider getMenuProvider(BlockState state, World worldIn, BlockPos pos) {
		TileEntity tileentity = worldIn.getBlockEntity(pos);
		return tileentity instanceof INamedContainerProvider ? (INamedContainerProvider)tileentity : null;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new ItemDuctTileEntity();
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.defaultBlockState()
				.setValue(NORTH, canConnect(context.getLevel(), context.getClickedPos(), Direction.NORTH))
				.setValue(SOUTH, canConnect(context.getLevel(), context.getClickedPos(), Direction.SOUTH))
				.setValue(EAST, canConnect(context.getLevel(), context.getClickedPos(), Direction.EAST))
				.setValue(WEST, canConnect(context.getLevel(), context.getClickedPos(), Direction.WEST))
				.setValue(UP, canConnect(context.getLevel(), context.getClickedPos(), Direction.UP))
				.setValue(DOWN, canConnect(context.getLevel(), context.getClickedPos(), Direction.DOWN));
	}
	
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			worldIn.getLiquidTicks().scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}
		
		final boolean connect = canConnect(worldIn, currentPos, facing);
		return state.setValue(PROPERTY_BY_DIRECTION.get(facing), connect);
	}
	
	protected boolean canConnect(IWorldReader world, BlockPos centerPos, Direction direction) {
		// Should be whether there's another pipe or anything else with an inventory?
		final BlockPos atPos = centerPos.relative(direction);
		@Nullable TileEntity te = world.getBlockEntity(atPos);
		if (te != null) {
			if (te instanceof IInventory) {
				return true;
			}
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return true;
	}
	
//	@Override
//	public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
//		final BlockState actualState = state.getActualState(source, pos);
//		
//		int index = 0;
//		for (Direction face : Direction.VALUES) {
//			if (GetFacingActive(actualState, face)) {
//				index |= (1 << (5 - face.getIndex()));
//			}
//		}
//		
//		return SELECTION_AABS[index];
//	}
	
//	@Override
//	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
//		
//	}
	
//	@Override
//	public VoxelShape getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
//		final BlockState actualState = state.getActualState(source, pos);
//		
//		int index = 0;
//		for (Direction face : Direction.VALUES) {
//			if (GetFacingActive(actualState, face)) {
//				index |= (1 << (5 - face.getIndex()));
//			}
//		}
//		
//		return SELECTION_AABS[index];
//	}
//	
//	@Override
//	public void addCollisionBoxToList(BlockState state, World worldIn, BlockPos pos, VoxelShape entityBox, List<VoxelShape> collidingBoxes, @Nullable Entity entityIn, boolean isActualState) {
//		addCollisionBoxToList(pos, entityBox, collidingBoxes, BASE_AABB);
//		for (Direction dir : Direction.VALUES) {
//			if (GetFacingActive(state.getActualState(worldIn, pos), dir)) {
//				addCollisionBoxToList(pos, entityBox, collidingBoxes, SIDE_AABBs[dir.getIndex()]);
//			}
//		}
//	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState blockState, World worldIn, BlockPos pos) {
		final TileEntity tileentity = worldIn.getBlockEntity(pos);
		final int output;

		if (tileentity instanceof ItemDuctTileEntity) {
			// 16 slots but have to adapt to 0-15 (with 1-15 if there are ANY stacks)
			ItemDuctTileEntity ent = (ItemDuctTileEntity) tileentity;
			float frac = ent.getFilledPercent();
			if (frac <= 0.0f) {
				output = 0;
			} else {
				output = 1 + MathHelper.floor(frac * 14);
			}
		} else {
			output = 0;
		}
		
		return output;
	}
	
	@Override
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != state.getBlock()) {
			TileEntity tileentity = worldIn.getBlockEntity(pos);
	
			if (tileentity instanceof ItemDuctTileEntity) {
				for (ItemStack stack : ((ItemDuctTileEntity) tileentity).getAllItems()) {
					InventoryHelper.dropItemStack(worldIn, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
				}
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}
		}
		
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
	
	@Override
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}
