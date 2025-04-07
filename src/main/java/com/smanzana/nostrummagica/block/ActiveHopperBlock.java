package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ActiveHopperGui;
import com.smanzana.nostrummagica.tile.ActiveHopperTileEntity;
import com.smanzana.nostrummagica.tile.NostrumTileEntities;
import com.smanzana.nostrummagica.tile.TickableBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.Hopper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Like a hopper except it only has 1 slot and always keeps 1 item.
 * It can also be placed sideways, and will still pull from inventories it's pointing to
 * @author Skyler
 *
 */
public class ActiveHopperBlock extends BaseEntityBlock {
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", (facing) -> {
		return facing != null && facing != Direction.UP;
	});
	
	public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
	
	private static final VoxelShape INPUT_SHAPE = Block.box(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape MIDDLE_SHAPE = Block.box(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
	private static final VoxelShape INPUT_MIDDLE_SHAPE = Shapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
	private static final VoxelShape BASE = Shapes.join(INPUT_MIDDLE_SHAPE, Hopper.INSIDE, BooleanOp.ONLY_FIRST);
	private static final VoxelShape DOWN_SHAPE = Shapes.or(BASE, Block.box(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
	private static final VoxelShape EAST_SHAPE = Shapes.or(BASE, Block.box(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
	private static final VoxelShape NORTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
	private static final VoxelShape SOUTH_SHAPE = Shapes.or(BASE, Block.box(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
	private static final VoxelShape WEST_SHAPE = Shapes.or(BASE, Block.box(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
	private static final VoxelShape DOWN_RAYTRACE_SHAPE = Hopper.INSIDE;
	private static final VoxelShape EAST_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
	private static final VoxelShape NORTH_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
	private static final VoxelShape SOUTH_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
	private static final VoxelShape WEST_RAYTRACE_SHAPE = Shapes.or(Hopper.INSIDE, Block.box(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));
	
	public static final String ID = "active_hopper";
	
	public ActiveHopperBlock() {
		super(Block.Properties.of(Material.METAL)
			.strength(3f, 8f)
			.sound(SoundType.METAL)
		);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.DOWN).setValue(ENABLED, true));
	}
	
	public static Direction GetFacing(BlockState state) {
		return state.getValue(FACING);
	}
	
	public static boolean GetEnabled(BlockState state) {
		if (state != null && state.getBlock() instanceof ActiveHopperBlock) {
			return state.getValue(ENABLED);
		}
		return false;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(ENABLED, FACING);
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.setValue(FACING, mirrorIn.mirror(state.getValue(FACING)));
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ActiveHopperTileEntity(pos, state);
	}
	
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
		return TickableBlockEntity.createTickerHelper(type, NostrumTileEntities.ActiveHopperTileEntityType);
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction facing = context.getClickedFace();
		if (facing == Direction.UP) {
			facing = Direction.DOWN;
		}
		return this.defaultBlockState().setValue(FACING, facing);
	}
	
	
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		  switch((Direction)state.getValue(FACING)) {
		  case DOWN:
			 return DOWN_SHAPE;
		  case NORTH:
			 return NORTH_SHAPE;
		  case SOUTH:
			 return SOUTH_SHAPE;
		  case WEST:
			 return WEST_SHAPE;
		  case EAST:
			 return EAST_SHAPE;
		  default:
			 return BASE;
		  }
	   }

	   public VoxelShape getInteractionShape(BlockState state, BlockGetter worldIn, BlockPos pos) {
		  switch((Direction)state.getValue(FACING)) {
		  case DOWN:
			 return DOWN_RAYTRACE_SHAPE;
		  case NORTH:
			 return NORTH_RAYTRACE_SHAPE;
		  case SOUTH:
			 return SOUTH_RAYTRACE_SHAPE;
		  case WEST:
			 return WEST_RAYTRACE_SHAPE;
		  case EAST:
			 return EAST_RAYTRACE_SHAPE;
		  default:
			 return Hopper.INSIDE;
		  }
	   }
	
	private void updateState(Level worldIn, BlockPos pos, BlockState state) {
		boolean flag = !worldIn.hasNeighborSignal(pos);

		if (flag != ((Boolean)state.getValue(ENABLED)).booleanValue()) {
			worldIn.setBlock(pos, state.setValue(ENABLED, Boolean.valueOf(flag)), 3);
		}
	}
	
	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}
	
	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level worldIn, BlockPos pos) {
		return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(worldIn.getBlockEntity(pos));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity tileentity = worldIn.getBlockEntity(pos);
	
			if (tileentity instanceof ActiveHopperTileEntity) {
				Containers.dropContents(worldIn, pos, (ActiveHopperTileEntity)tileentity);
				worldIn.updateNeighbourForOutputSignal(pos, this);
			}
		}
		
		super.onRemove(state, worldIn, pos, newState, isMoving);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (!worldIn.isClientSide) {
			ActiveHopperTileEntity te = (ActiveHopperTileEntity) worldIn.getBlockEntity(pos);
			NostrumMagica.instance.proxy.openContainer(player, ActiveHopperGui.ActiveHopperContainer.Make(te));
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
}
