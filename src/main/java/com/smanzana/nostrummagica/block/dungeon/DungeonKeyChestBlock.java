package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.world.NostrumWorldKey;
import com.smanzana.nostrummagica.world.dungeon.DungeonRecord;
import com.smanzana.nostrummagica.world.dungeon.NostrumDungeon.DungeonInstance;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class DungeonKeyChestBlock extends HorizontalBlock {
	
	public static DirectionProperty FACING = HorizontalBlock.HORIZONTAL_FACING;
	public static BooleanProperty OPEN = BooleanProperty.create("open");
	
	protected DungeonKeyChestBlock(Block.Properties props) {
		super(props);
		
		this.setDefaultState(this.getDefaultState().with(OPEN, false));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(FACING, OPEN);
	}
	
	@Override
	public abstract VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context);
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	protected void setOpen(BlockState state, World worldIn, BlockPos pos) {
		worldIn.setBlockState(pos, state.with(OPEN, true));
	}
	
	public abstract void makeDungeonChest(IWorld worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon);
	
	@Override
	public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (state.get(OPEN)) {
			return ActionResultType.PASS;
		}
		
		if (!worldIn.isRemote()) {
			DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getTileEntity(pos);
			if (player.isCreative() && player.isSneaking()) {
				DungeonRecord record = NostrumMagica.dungeonTracker.getDungeon(player);
				if (record != null) {
					NostrumWorldKey key = chest.isLarge() ? record.instance.getLargeKey() : record.instance.getSmallKey();
					chest.setWorldKey(key);
					player.sendMessage(new StringTextComponent("Set to dungeon key"), Util.DUMMY_UUID);
				} else {
					player.sendMessage(new StringTextComponent("Not in a dungeon, so no key to set"), Util.DUMMY_UUID);
				}
			} else {
				chest.open(player);
				setOpen(state, worldIn, pos);
			}
		}
		
		return ActionResultType.SUCCESS;
	}
	
	@Override
	public boolean hasTileEntity(BlockState state) {
		return true;
	}
	
	@Override
	public TileEntity createTileEntity(BlockState state, IBlockReader world) {
		return new DungeonKeyChestTileEntity();
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return getDefaultState().with(FACING, context.getPlacementHorizontalFacing().getOpposite());
	}
	
	@Override
	public boolean eventReceived(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		DungeonKeyChestTileEntity tileentity = (DungeonKeyChestTileEntity) worldIn.getTileEntity(pos);
		return tileentity == null ? false : tileentity.receiveClientEvent(id, param);
	}
	
	public static class Small extends DungeonKeyChestBlock {
		
		public static final String ID = "small_dungeon_key_chest";
		
		private static final VoxelShape SHAPE_SMALL_NS = Block.makeCuboidShape(0.5D, 0.0D, 3.0D, 15.5D, 12.0D, 13.0D);
		private static final VoxelShape SHAPE_SMALL_EW = Block.makeCuboidShape(3.0D, 0.0D, 0.5D, 13.0D, 12.0D, 15.5D);
		private static final VoxelShape SHAPE_SMALL_OPEN_NS = Block.makeCuboidShape(0.5D, 0.0D, 3.0D, 15.5D, 8.0D, 13.0D);
		private static final VoxelShape SHAPE_SMALL_OPEN_EW = Block.makeCuboidShape(3.0D, 0.0D, 0.5D, 13.0D, 8.0D, 15.5D);
		
		public Small() {
			super(Block.Properties.create(Material.WOOD)
					.sound(SoundType.WOOD)
					.hardnessAndResistance(-1.0F, 3600000.8F)
					.noDrops());
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			final boolean open = state.get(OPEN);
			
			switch (state.get(FACING)) {
			case EAST:
			case WEST:
				return open ? SHAPE_SMALL_OPEN_EW : SHAPE_SMALL_EW;
			case SOUTH:
			case NORTH:
				return open ? SHAPE_SMALL_OPEN_NS : SHAPE_SMALL_NS;
			case UP:
			case DOWN:
			default:
				return VoxelShapes.fullCube();
			}
		}

		@Override
		public void makeDungeonChest(IWorld worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon) {
			final boolean isWorldGen = WorldUtil.IsWorldGen(worldIn);
			// This is pretty dumb, but terrain gen will 'defer' tile entities under normal circumstances. By default,
			// our setBlockState below will during world gen, too.
			// BUT if something earlier in gen has done a 'getTileEntity' after something even earlier
			// caused a deferred one, it will cache it and not allow any TE changes during generation.
			// Specifically WorldGenRegion will push into the deferred, and then read it when getTileEntity is called.
			// DungeonChests run into an issue where LootUtil has already forced a chest TE to generate, and so our
			// blockstate change here doesn't cause a TE refresh.
			// So we're going to force it.
			if (isWorldGen && worldIn.getTileEntity(pos) != null && !(worldIn.getTileEntity(pos) instanceof DungeonKeyChestTileEntity)) {
				worldIn.removeBlock(pos, false);
			}
			
			worldIn.setBlockState(pos, this.getDefaultState().with(FACING, facing), 3);
			
			
			// During worldgen, sometimes blockstate sets don't immediately have all the same effects, like creating a tile entity.
			// If things do look good to proceed, stamp in key. Otherwise, wait and let autogenerated TileEntity generation handle it.
			if (!isWorldGen || (worldIn.getTileEntity(pos) != null && worldIn.getTileEntity(pos) instanceof DungeonKeyChestTileEntity)) {
				DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getTileEntity(pos);
				chest.setWorldKey(dungeon.getSmallKey(), isWorldGen);
			} else {
				NostrumMagica.logger.warn("Couldn't set key chest TE at " + pos);
			}
		}
	}
	
	public static class Large extends DungeonKeyChestBlock {
		
		public static final String ID = "large_dungeon_key_chest";
		
		public static BooleanProperty SLAVE = BooleanProperty.create("slave");
		
		protected static final VoxelShape SHAPE_LARGE_N = Block.makeCuboidShape(1.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_S = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 15.0D, 14.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_E = Block.makeCuboidShape(0.0D, 0.0D, 1.0D, 16.0D, 14.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_W = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 15.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_N = Block.makeCuboidShape(1.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_S = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 15.0D, 10.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_E = Block.makeCuboidShape(0.0D, 0.0D, 1.0D, 16.0D, 10.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_W = Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 15.0D);
		
		public Large() {
			super(Block.Properties.create(Material.WOOD)
					.sound(SoundType.WOOD)
					.hardnessAndResistance(-1.0F, 3600000.8F)
					.noDrops());
		
			this.setDefaultState(this.getDefaultState().with(SLAVE, false));
		}
		
		@Override
		protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
			super.fillStateContainer(builder);
			builder.add(SLAVE);
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			
			final boolean master = !state.get(SLAVE);
			final boolean open = state.get(OPEN);
			
			final VoxelShape E = !open ? SHAPE_LARGE_E : SHAPE_LARGE_OPEN_E;
			final VoxelShape W = !open ? SHAPE_LARGE_W : SHAPE_LARGE_OPEN_W;
			final VoxelShape N = !open ? SHAPE_LARGE_N : SHAPE_LARGE_OPEN_N;
			final VoxelShape S = !open ? SHAPE_LARGE_S : SHAPE_LARGE_OPEN_S;
			
			switch (state.get(FACING)) {
			case EAST:
				return master ? E : W;
			case WEST:
				return master ? W : E;
			case SOUTH:
				return master ? S : N;
			case NORTH:
				return master ? N : S;
			case UP:
			case DOWN:
			default:
				return VoxelShapes.fullCube();
			}
		}
		
		@Override
		public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			if (state.get(SLAVE)) {
				final BlockPos masterPos = getMasterPos(pos, state);
				final BlockState masterState = worldIn.getBlockState(masterPos);
				if (masterState.getBlock() != this) {
					return ActionResultType.FAIL;
				} else {
					return this.onBlockActivated(masterState, worldIn, masterPos, player, handIn, hit);
				}
			}
			
			// Else do normal super
			return super.onBlockActivated(state, worldIn, pos, player, handIn, hit);
		}
		
		@Override
		public boolean hasTileEntity(BlockState state) {
			// Only non-slave has a tile entity
			return !state.get(SLAVE);
		}
		
		protected BlockPos getSlavePos(BlockPos pos, BlockState state) {
			if (state.get(SLAVE)) {
				return pos;
			}
			
			switch (state.get(FACING)) {
			case UP:
			case DOWN:
			default:
			case EAST:
				return pos.south();
			case NORTH:
				return pos.east();
			case SOUTH:
				return pos.west();
			case WEST:
				return pos.north();
			}
		}
		
		protected BlockPos getMasterPos(BlockPos pos, BlockState state) {
			if (!state.get(SLAVE)) {
				return pos;
			}
			
			switch (state.get(FACING)) {
			case UP:
			case DOWN:
			default:
			case EAST:
				return pos.north();
			case NORTH:
				return pos.west();
			case SOUTH:
				return pos.east();
			case WEST:
				return pos.south();
			}
		}
		
		protected void makeChild(IWorld worldIn, BlockPos pos, BlockState state) {
			if (!state.get(SLAVE)) {
				worldIn.setBlockState(getSlavePos(pos, state), state.with(SLAVE, true), 3);
			}
		}
		
		@Override
		public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			makeChild(worldIn, pos, state);
		}
		
		@Override
		public boolean isValidPosition(BlockState state, IWorldReader world, BlockPos pos) {
			return world.isAirBlock(pos) && world.isAirBlock(getSlavePos(pos, state));
		}
		
		@Override
		@Nullable
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			final World world = context.getWorld();
			final BlockPos pos = context.getPos();
			final BlockState state = super.getStateForPlacement(context);
			if (!isValidPosition(state, world, pos)) {
				return null;
			}

			return state;
		}
		
		@Override
		protected void setOpen(BlockState state, World worldIn, BlockPos pos) {
			if (!state.get(SLAVE)) {
				final BlockPos slavePos = getSlavePos(pos, state);
				setOpen(worldIn.getBlockState(slavePos), worldIn, slavePos);
			}
			super.setOpen(state, worldIn, pos);
		}
		
		@Override
		public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos changedPos) {
			if (stateIn.get(SLAVE)) {
				// If it was master position that changed, react
				if (changedPos.equals(getMasterPos(currentPos, stateIn)) && facingState.getBlock() != this) {
					return Blocks.AIR.getDefaultState();
				}
			} else {
				if (changedPos.equals(getSlavePos(currentPos, stateIn)) && facingState.getBlock() != this) {
					return Blocks.AIR.getDefaultState();
				}
			}
			
			return stateIn;
		}

		@Override
		public void makeDungeonChest(IWorld worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon) {
			worldIn.setBlockState(pos, this.getDefaultState().with(FACING, facing), 3);
			makeChild(worldIn, pos, worldIn.getBlockState(pos));
			
			DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getTileEntity(pos);
			chest.setWorldKey(dungeon.getLargeKey(), WorldUtil.IsWorldGen(worldIn));
		}
	}
}
