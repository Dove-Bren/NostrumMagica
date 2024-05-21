package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RuneLibraryGui;
import com.smanzana.nostrummagica.tiles.RuneLibraryTileEntity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class RuneLibraryBlock extends Block {
	
	public static enum Fill implements IStringSerializable {
		EMPTY,
		SOME,
		MOST,
		FULL;

		@Override
		public String getString() {
			return name().toLowerCase();
		}
	}
	
	public static final DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static final EnumProperty<Fill> FILL = EnumProperty.create("fill", Fill.class);
	
	private static final double BB_DEPTH = 6.0 / 16.0;
	private static final double BB_MARGIN = 0 / 16.0;
	private static final VoxelShape AABB_N = Block.makeCuboidShape(16 * BB_MARGIN, 16 * 0, 16 * (1 - BB_DEPTH), 16 * (1 - BB_MARGIN), 16 * 1, 16 * 1);
	private static final VoxelShape AABB_E = Block.makeCuboidShape(16 * 0, 16 * 0, 16 * BB_MARGIN, 16 * BB_DEPTH, 16 * 1, 16 * (1-BB_MARGIN));
	private static final VoxelShape AABB_S = Block.makeCuboidShape(16 * BB_MARGIN, 16 * 0, 16 * 0, 16 * (1-BB_MARGIN), 16 * 1, 16 * BB_DEPTH);
	private static final VoxelShape AABB_W = Block.makeCuboidShape(16 * (1 - BB_DEPTH), 16 * 0, 16 * BB_MARGIN, 16 * 1, 16 * 1, 16 * (1-BB_MARGIN));
	
	public static final String ID = "rune_library";
	
	public RuneLibraryBlock() {
		super(Block.Properties.create(Material.WOOD)
				.hardnessAndResistance(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.harvestTool(ToolType.AXE)
				.notSolid()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(FILL, Fill.EMPTY));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction side = context.getPlacementHorizontalFacing().getOpposite();
		if (!this.canPlaceAt(context.getWorld(), context.getPos(), side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.rotateY();
				if (this.canPlaceAt(context.getWorld(), context.getPos(), side)) {
					break;
				}
			}
		}
		
		return this.getDefaultState()
				.with(FACING, side);
	}
	
	protected boolean canPlaceAt(IWorldReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.offset(side.getOpposite()));
		if (state == null || !(state.isSolidSide(worldIn, pos.offset(side.getOpposite()), side.getOpposite()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
		for (Direction side : FACING.getAllowedValues()) {
			if (canPlaceAt(world, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState state, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction myFacing = state.get(FACING);
		if (!this.canPlaceAt(worldIn, currentPos, myFacing)) { // should check passed in facing and only re-check if wall we're on changed but I can't remember if facing is wall we're on or the opposite
			return Blocks.AIR.getDefaultState();
		}
		
		return state;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING).add(FILL);
	}
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		RuneLibraryTileEntity te = (RuneLibraryTileEntity) worldIn.getTileEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, RuneLibraryGui.RuneLibraryContainer.Make(te));
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new RuneLibraryTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	public void onReplaced(BlockState state, World world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeTileEntity(pos);
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
		case NORTH:
		case UP:
		case DOWN:
		default:
			return AABB_N;
		case EAST:
			return AABB_E;
		case SOUTH:
			return AABB_S;
		case WEST:
			return AABB_W;
		}
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		TileEntity ent = world.getTileEntity(pos);
		if (ent == null || !(ent instanceof RuneLibraryTileEntity))
			return;
		
		RuneLibraryTileEntity putter = (RuneLibraryTileEntity) ent;
		IInventory inv = putter.getInventory();
		for (int i = 0; i < inv.getSizeInventory(); i++) {
			ItemStack item = inv.getStackInSlot(i);
			if (!item.isEmpty()) {
				double x, y, z;
				x = pos.getX() + .5;
				y = pos.getY() + .5;
				z = pos.getZ() + .5;
				world.addEntity(new ItemEntity(world, x, y, z, item.copy()));
			}
		}
	}
}
