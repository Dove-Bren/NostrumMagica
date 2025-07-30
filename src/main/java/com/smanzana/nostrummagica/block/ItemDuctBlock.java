package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.tile.ItemDuctTileEntity;
import com.smanzana.nostrummagica.tile.NostrumBlockEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraftforge.items.CapabilityItemHandler;

/**
 * It's an item pipe!
 * @author Skyler
 *
 */
@SuppressWarnings("deprecation")
public class ItemDuctBlock extends PipeBlock implements SimpleWaterloggedBlock, EntityBlock {
	
	public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
	
	private static final float INNER_RADIUS = (3.0f / 16.0f);
	
	
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
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(NORTH, SOUTH, EAST, WEST, UP, DOWN, WATERLOGGED);
	}
	
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
		super.triggerEvent(state, worldIn, pos, id, param);
		BlockEntity tileentity = worldIn.getBlockEntity(pos);
		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}

	@Nullable
	public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
		BlockEntity tileentity = worldIn.getBlockEntity(pos);
		return tileentity instanceof MenuProvider ? (MenuProvider)tileentity : null;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ItemDuctTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumBlockEntities.ItemDuct);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState()
				.setValue(NORTH, canConnect(context.getLevel(), context.getClickedPos(), Direction.NORTH))
				.setValue(SOUTH, canConnect(context.getLevel(), context.getClickedPos(), Direction.SOUTH))
				.setValue(EAST, canConnect(context.getLevel(), context.getClickedPos(), Direction.EAST))
				.setValue(WEST, canConnect(context.getLevel(), context.getClickedPos(), Direction.WEST))
				.setValue(UP, canConnect(context.getLevel(), context.getClickedPos(), Direction.UP))
				.setValue(DOWN, canConnect(context.getLevel(), context.getClickedPos(), Direction.DOWN));
	}
	
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) {
			worldIn.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(worldIn));
		}
		
		final boolean connect = canConnect(worldIn, currentPos, facing);
		return state.setValue(PROPERTY_BY_DIRECTION.get(facing), connect);
	}
	
	protected boolean canConnect(LevelReader world, BlockPos centerPos, Direction direction) {
		// Should be whether there's another pipe or anything else with an inventory?
		final BlockPos atPos = centerPos.relative(direction);
		@Nullable BlockEntity te = world.getBlockEntity(atPos);
		if (te != null) {
			if (te instanceof Container) {
				return true;
			}
			if (te.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, direction.getOpposite()).isPresent()) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return true;
	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		final BlockEntity tileentity = worldIn.getBlockEntity(pos);
		final int output;

		if (tileentity instanceof ItemDuctTileEntity) {
			// 16 slots but have to adapt to 0-15 (with 1-15 if there are ANY stacks)
			ItemDuctTileEntity ent = (ItemDuctTileEntity) tileentity;
			float frac = ent.getFilledPercent();
			if (frac <= 0.0f) {
				output = 0;
			} else {
				output = 1 + Mth.floor(frac * 14);
			}
		} else {
			output = 0;
		}
		
		return output;
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (newState.getBlock() != state.getBlock()) {
			BlockEntity tileentity = worldIn.getBlockEntity(pos);
	
			if (tileentity instanceof ItemDuctTileEntity) {
				for (ItemStack stack : ((ItemDuctTileEntity) tileentity).getAllItems()) {
					Containers.dropItemStack(worldIn, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, stack);
				}
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}
		}
		
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}
}
