package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.gui.container.RuneLibraryGui;
import com.smanzana.nostrummagica.tile.RuneLibraryTileEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class RuneLibraryBlock extends BaseEntityBlock {
	
	public static enum Fill implements StringRepresentable {
		EMPTY,
		SOME,
		MOST,
		FULL;

		@Override
		public String getSerializedName() {
			return name().toLowerCase();
		}
	}
	
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static final EnumProperty<Fill> FILL = EnumProperty.create("fill", Fill.class);
	
	private static final double BB_DEPTH = 6.0 / 16.0;
	private static final double BB_MARGIN = 0 / 16.0;
	private static final VoxelShape AABB_N = Block.box(16 * BB_MARGIN, 16 * 0, 16 * (1 - BB_DEPTH), 16 * (1 - BB_MARGIN), 16 * 1, 16 * 1);
	private static final VoxelShape AABB_E = Block.box(16 * 0, 16 * 0, 16 * BB_MARGIN, 16 * BB_DEPTH, 16 * 1, 16 * (1-BB_MARGIN));
	private static final VoxelShape AABB_S = Block.box(16 * BB_MARGIN, 16 * 0, 16 * 0, 16 * (1-BB_MARGIN), 16 * 1, 16 * BB_DEPTH);
	private static final VoxelShape AABB_W = Block.box(16 * (1 - BB_DEPTH), 16 * 0, 16 * BB_MARGIN, 16 * 1, 16 * 1, 16 * (1-BB_MARGIN));
	
	public static final String ID = "rune_library";
	
	public RuneLibraryBlock() {
		super(Block.Properties.of(Material.WOOD)
				.strength(2.5f, 2.5f)
				.sound(SoundType.WOOD)
				.noOcclusion()
				);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(FILL, Fill.EMPTY));
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction side = context.getHorizontalDirection().getOpposite();
		if (!this.canPlaceAt(context.getLevel(), context.getClickedPos(), side)) {
			// Rotate and find it
			for (int i = 0; i < 3; i++) {
				side = side.getClockWise();
				if (this.canPlaceAt(context.getLevel(), context.getClickedPos(), side)) {
					break;
				}
			}
		}
		
		return this.defaultBlockState()
				.setValue(FACING, side);
	}
	
	protected boolean canPlaceAt(LevelReader worldIn, BlockPos pos, Direction side) {
		BlockState state = worldIn.getBlockState(pos.relative(side.getOpposite()));
		if (state == null || !(state.isFaceSturdy(worldIn, pos.relative(side.getOpposite()), side.getOpposite()))) {
			return false;
		}
		
		return true;
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
		for (Direction side : FACING.getPossibleValues()) {
			if (canPlaceAt(world, pos, side)) {
				return true;
			}
		}
		
		return false;
	}
	
	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		Direction myFacing = state.getValue(FACING);
		if (!this.canPlaceAt(worldIn, currentPos, myFacing)) { // should check passed in facing and only re-check if wall we're on changed but I can't remember if facing is wall we're on or the opposite
			return Blocks.AIR.defaultBlockState();
		}
		
		return state;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING).add(FILL);
	}
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		RuneLibraryTileEntity te = (RuneLibraryTileEntity) worldIn.getBlockEntity(pos);
		NostrumMagica.instance.proxy.openContainer(player, RuneLibraryGui.RuneLibraryContainer.Make(te));
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new RuneLibraryTileEntity();
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			destroy(world, pos, state);
			world.removeBlockEntity(pos);
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
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
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		BlockEntity ent = world.getBlockEntity(pos);
		if (ent == null || !(ent instanceof RuneLibraryTileEntity))
			return;
		
		RuneLibraryTileEntity putter = (RuneLibraryTileEntity) ent;
		Container inv = putter.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack item = inv.getItem(i);
			if (!item.isEmpty()) {
				double x, y, z;
				x = pos.getX() + .5;
				y = pos.getY() + .5;
				z = pos.getZ() + .5;
				world.addFreshEntity(new ItemEntity(world, x, y, z, item.copy()));
			}
		}
	}
}
