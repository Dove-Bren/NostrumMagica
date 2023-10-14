package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.ActiveHopperGui;
import com.smanzana.nostrummagica.tiles.ActiveHopperTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.inventory.container.Container;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.IHopper;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.IBooleanFunction;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

/**
 * Like a hopper except it only has 1 slot and always keeps 1 item.
 * It can also be placed sideways, and will still pull from inventories it's pointing to
 * @author Skyler
 *
 */
public class ActiveHopper extends ContainerBlock {
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", (facing) -> {
		return facing != null && facing != Direction.UP;
	});
	
	public static final BooleanProperty ENABLED = BooleanProperty.create("enabled");
	
	private static final VoxelShape INPUT_SHAPE = Block.makeCuboidShape(0.0D, 10.0D, 0.0D, 16.0D, 16.0D, 16.0D);
	private static final VoxelShape MIDDLE_SHAPE = Block.makeCuboidShape(4.0D, 4.0D, 4.0D, 12.0D, 10.0D, 12.0D);
	private static final VoxelShape INPUT_MIDDLE_SHAPE = VoxelShapes.or(MIDDLE_SHAPE, INPUT_SHAPE);
	private static final VoxelShape field_196326_A = VoxelShapes.combineAndSimplify(INPUT_MIDDLE_SHAPE, IHopper.INSIDE_BOWL_SHAPE, IBooleanFunction.ONLY_FIRST);
	private static final VoxelShape DOWN_SHAPE = VoxelShapes.or(field_196326_A, Block.makeCuboidShape(6.0D, 0.0D, 6.0D, 10.0D, 4.0D, 10.0D));
	private static final VoxelShape EAST_SHAPE = VoxelShapes.or(field_196326_A, Block.makeCuboidShape(12.0D, 4.0D, 6.0D, 16.0D, 8.0D, 10.0D));
	private static final VoxelShape NORTH_SHAPE = VoxelShapes.or(field_196326_A, Block.makeCuboidShape(6.0D, 4.0D, 0.0D, 10.0D, 8.0D, 4.0D));
	private static final VoxelShape SOUTH_SHAPE = VoxelShapes.or(field_196326_A, Block.makeCuboidShape(6.0D, 4.0D, 12.0D, 10.0D, 8.0D, 16.0D));
	private static final VoxelShape WEST_SHAPE = VoxelShapes.or(field_196326_A, Block.makeCuboidShape(0.0D, 4.0D, 6.0D, 4.0D, 8.0D, 10.0D));
	private static final VoxelShape DOWN_RAYTRACE_SHAPE = IHopper.INSIDE_BOWL_SHAPE;
	private static final VoxelShape EAST_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE_BOWL_SHAPE, Block.makeCuboidShape(12.0D, 8.0D, 6.0D, 16.0D, 10.0D, 10.0D));
	private static final VoxelShape NORTH_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE_BOWL_SHAPE, Block.makeCuboidShape(6.0D, 8.0D, 0.0D, 10.0D, 10.0D, 4.0D));
	private static final VoxelShape SOUTH_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE_BOWL_SHAPE, Block.makeCuboidShape(6.0D, 8.0D, 12.0D, 10.0D, 10.0D, 16.0D));
	private static final VoxelShape WEST_RAYTRACE_SHAPE = VoxelShapes.or(IHopper.INSIDE_BOWL_SHAPE, Block.makeCuboidShape(0.0D, 8.0D, 6.0D, 4.0D, 10.0D, 10.0D));
	
	public static final String ID = "active_hopper";
	
	public ActiveHopper() {
		super(Block.Properties.create(Material.IRON)
			.hardnessAndResistance(3f, 8f)
			.harvestTool(ToolType.PICKAXE).harvestLevel(2)
			.sound(SoundType.METAL)
		);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.DOWN).with(ENABLED, true));
	}
	
	public static Direction GetFacing(BlockState state) {
		return state.get(FACING);
	}
	
	public static boolean GetEnabled(BlockState state) {
		if (state != null && state.getBlock() instanceof ActiveHopper) {
			return state.get(ENABLED);
		}
		return false;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(ENABLED, FACING);
	}
	
	@Override
	public BlockState rotate(BlockState state, Rotation rot) {
		return state.with(FACING, rot.rotate(state.get(FACING)));
	}
	
	@Override
	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.with(FACING, mirrorIn.mirror(state.get(FACING)));
	}
	
	@Override
	public boolean hasTileEntity() {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return createNewTileEntity(world);
	}
	
	@Override
	public TileEntity createNewTileEntity(IBlockReader world) {
		return new ActiveHopperTileEntity();
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction facing = context.getFace();
		if (facing == Direction.UP) {
			facing = Direction.DOWN;
		}
		return this.getDefaultState().with(FACING, facing);
	}
	
	
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		  switch((Direction)state.get(FACING)) {
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
			 return field_196326_A;
		  }
	   }

	   public VoxelShape getRaytraceShape(BlockState state, IBlockReader worldIn, BlockPos pos) {
		  switch((Direction)state.get(FACING)) {
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
			 return IHopper.INSIDE_BOWL_SHAPE;
		  }
	   }
	
	private void updateState(World worldIn, BlockPos pos, BlockState state) {
		boolean flag = !worldIn.isBlockPowered(pos);

		if (flag != ((Boolean)state.get(ENABLED)).booleanValue()) {
			worldIn.setBlockState(pos, state.with(ENABLED, Boolean.valueOf(flag)), 3);
		}
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
		updateState(worldIn, pos, state);
	}
	
	@Override
	public boolean hasComparatorInputOverride(BlockState state) {
		return true;
	}
	
	@Override
	public int getComparatorInputOverride(BlockState blockState, World worldIn, BlockPos pos) {
		return Container.calcRedstone(worldIn.getTileEntity(pos));
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			TileEntity tileentity = worldIn.getTileEntity(pos);
	
			if (tileentity instanceof ActiveHopperTileEntity) {
				InventoryHelper.dropInventoryItems(worldIn, pos, (ActiveHopperTileEntity)tileentity);
				worldIn.updateComparatorOutputLevel(pos, this);
			}
		}
		
		super.onReplaced(state, worldIn, pos, newState, isMoving);
	}
	
	@Override
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		ActiveHopperTileEntity te = (ActiveHopperTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, ActiveHopperGui.ActiveHopperContainer.Make(te));
		
		return true;
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
//	@Override
//	public boolean isFullCube(BlockState state) {
//		return false;
//	}
//	
//	@Override
//	public boolean isOpaqueCube(BlockState state) {
//		return false;
//	}
	
	@Override
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.CUTOUT_MIPPED;
	}
}
