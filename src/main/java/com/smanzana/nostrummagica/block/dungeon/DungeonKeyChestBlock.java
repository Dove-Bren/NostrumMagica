package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.block.ILargeKeyMarker;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;

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
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;

public abstract class DungeonKeyChestBlock extends HorizontalBlock implements ILargeKeyMarker {
	
	public static DirectionProperty FACING = HorizontalBlock.FACING;
	public static BooleanProperty OPEN = BooleanProperty.create("open");
	
	protected DungeonKeyChestBlock(Block.Properties props) {
		super(props);
		
		this.registerDefaultState(this.defaultBlockState().setValue(OPEN, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, OPEN);
	}
	
	@Override
	public abstract VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context);
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	protected void setOpen(BlockState state, World worldIn, BlockPos pos) {
		worldIn.setBlockAndUpdate(pos, state.setValue(OPEN, true));
	}
	
	public abstract void makeDungeonChest(IWorld worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon);
	
	@Override
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		if (state.getValue(OPEN)) {
			return ActionResultType.PASS;
		}
		
		if (!worldIn.isClientSide()) {
			DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getBlockEntity(pos);
			if (player.isCreative() && player.isShiftKeyDown()) {
				DungeonRecord record = AutoDungeons.GetDungeonTracker().getDungeon(player);
				if (record != null) {
					WorldKey key = chest.isLarge() ? record.instance.getLargeKey() : record.instance.getSmallKey();
					chest.setWorldKey(key);
					player.sendMessage(new StringTextComponent("Set to dungeon key"), Util.NIL_UUID);
				} else {
					player.sendMessage(new StringTextComponent("Not in a dungeon, so no key to set"), Util.NIL_UUID);
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
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.MODEL;
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public boolean triggerEvent(BlockState state, World worldIn, BlockPos pos, int id, int param) {
		DungeonKeyChestTileEntity tileentity = (DungeonKeyChestTileEntity) worldIn.getBlockEntity(pos);
		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}

	@Override
	public void setKey(IWorld world, BlockState state, BlockPos pos, WorldKey key, DungeonRoomInstance instance, MutableBoundingBox bounds) {
		DungeonKeyChestTileEntity tileentity = (DungeonKeyChestTileEntity) world.getBlockEntity(pos);
		tileentity.setWorldKey(key, WorldUtil.IsWorldGen(world));
	}
	
	public static class Small extends DungeonKeyChestBlock {
		
		public static final String ID = "small_dungeon_key_chest";
		
		private static final VoxelShape SHAPE_SMALL_N = Block.box(0.5D, 0.0D, 0.0D, 15.5D, 12.0D, 10.0D);
		private static final VoxelShape SHAPE_SMALL_S = Block.box(0.5D, 0.0D, 6.0D, 15.5D, 12.0D, 16.0D);
		private static final VoxelShape SHAPE_SMALL_E = Block.box(6.0D, 0.0D, 0.5D, 16.0D, 12.0D, 15.5D);
		private static final VoxelShape SHAPE_SMALL_W = Block.box(0.0D, 0.0D, 0.5D, 10.0D, 12.0D, 15.5D);
		private static final VoxelShape SHAPE_SMALL_OPEN_N = Block.box(0.5D, 0.0D, 0.0D, 15.5D, 8.0D, 10.0D);
		private static final VoxelShape SHAPE_SMALL_OPEN_S = Block.box(0.5D, 0.0D, 6.0D, 15.5D, 8.0D, 16.0D);
		private static final VoxelShape SHAPE_SMALL_OPEN_E = Block.box(6.0D, 0.0D, 0.5D, 16.0D, 8.0D, 15.5D);
		private static final VoxelShape SHAPE_SMALL_OPEN_W = Block.box(0.0D, 0.0D, 0.5D, 10.0D, 8.0D, 15.5D);
		
		public Small() {
			super(Block.Properties.of(Material.WOOD)
					.sound(SoundType.WOOD)
					.strength(-1.0F, 3600000.8F)
					.noDrops());
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			final boolean open = state.getValue(OPEN);
			
			switch (state.getValue(FACING)) {
			case EAST:
				return open ? SHAPE_SMALL_OPEN_E : SHAPE_SMALL_E;
			case WEST:
				return open ? SHAPE_SMALL_OPEN_W : SHAPE_SMALL_W;
			case SOUTH:
				return open ? SHAPE_SMALL_OPEN_S : SHAPE_SMALL_S;
			case NORTH:
				return open ? SHAPE_SMALL_OPEN_N : SHAPE_SMALL_N;
			case UP:
			case DOWN:
			default:
				return VoxelShapes.block();
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
			if (isWorldGen && worldIn.getBlockEntity(pos) != null && !(worldIn.getBlockEntity(pos) instanceof DungeonKeyChestTileEntity)) {
				worldIn.removeBlock(pos, false);
			}
			
			worldIn.setBlock(pos, this.defaultBlockState().setValue(FACING, facing), 3);
			
			
			// During worldgen, sometimes blockstate sets don't immediately have all the same effects, like creating a tile entity.
			// If things do look good to proceed, stamp in key. Otherwise, wait and let autogenerated TileEntity generation handle it.
			if (!isWorldGen || (worldIn.getBlockEntity(pos) != null && worldIn.getBlockEntity(pos) instanceof DungeonKeyChestTileEntity)) {
				DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getBlockEntity(pos);
				chest.setWorldKey(dungeon.getSmallKey(), isWorldGen);
			} else {
				NostrumMagica.logger.warn("Couldn't set key chest TE at " + pos);
			}
		}
	}
	
	public static class Large extends DungeonKeyChestBlock {
		
		public static final String ID = "large_dungeon_key_chest";
		
		public static BooleanProperty SLAVE = BooleanProperty.create("slave");
		
		protected static final VoxelShape SHAPE_LARGE_N = Block.box(1.0D, 0.0D, 0.0D, 16.0D, 14.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_S = Block.box(0.0D, 0.0D, 0.0D, 15.0D, 14.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_E = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 14.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_W = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 14.0D, 15.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_N = Block.box(1.0D, 0.0D, 0.0D, 16.0D, 10.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_S = Block.box(0.0D, 0.0D, 0.0D, 15.0D, 10.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_E = Block.box(0.0D, 0.0D, 1.0D, 16.0D, 10.0D, 16.0D);
		protected static final VoxelShape SHAPE_LARGE_OPEN_W = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 10.0D, 15.0D);
		
		public Large() {
			super(Block.Properties.of(Material.WOOD)
					.sound(SoundType.WOOD)
					.strength(-1.0F, 3600000.8F)
					.noDrops());
		
			this.registerDefaultState(this.defaultBlockState().setValue(SLAVE, false));
		}
		
		@Override
		protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
			super.createBlockStateDefinition(builder);
			builder.add(SLAVE);
		}

		@Override
		public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			
			final boolean master = !state.getValue(SLAVE);
			final boolean open = state.getValue(OPEN);
			
			final VoxelShape E = !open ? SHAPE_LARGE_E : SHAPE_LARGE_OPEN_E;
			final VoxelShape W = !open ? SHAPE_LARGE_W : SHAPE_LARGE_OPEN_W;
			final VoxelShape N = !open ? SHAPE_LARGE_N : SHAPE_LARGE_OPEN_N;
			final VoxelShape S = !open ? SHAPE_LARGE_S : SHAPE_LARGE_OPEN_S;
			
			switch (state.getValue(FACING)) {
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
				return VoxelShapes.block();
			}
		}
		
		public VoxelShape getWholeShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
			// Put slave one together too
			final boolean slave = state.getValue(SLAVE);
			final VoxelShape base = this.getShape(state, worldIn, pos, context);
			final Vector3i offset;
			if (slave) {
				offset = this.getMasterPos(pos, state).subtract(pos);
			} else {
				offset = this.getSlavePos(pos, state).subtract(pos);
			}
			final VoxelShape otherBase = this.getShape(state.setValue(SLAVE, !slave), worldIn, pos, context);
			return VoxelShapes.or(base, otherBase.move(offset.getX(), offset.getY(), offset.getZ()));
		}
		
		@Override
		public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
			if (state.getValue(SLAVE)) {
				final BlockPos masterPos = getMasterPos(pos, state);
				final BlockState masterState = worldIn.getBlockState(masterPos);
				if (masterState.getBlock() != this) {
					return ActionResultType.FAIL;
				} else {
					return this.use(masterState, worldIn, masterPos, player, handIn, hit);
				}
			}
			
			// Else do normal super
			return super.use(state, worldIn, pos, player, handIn, hit);
		}
		
		@Override
		public boolean hasTileEntity(BlockState state) {
			// Only non-slave has a tile entity
			return !state.getValue(SLAVE);
		}
		
		protected BlockPos getSlavePos(BlockPos pos, BlockState state) {
			if (state.getValue(SLAVE)) {
				return pos;
			}
			
			switch (state.getValue(FACING)) {
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
			if (!state.getValue(SLAVE)) {
				return pos;
			}
			
			switch (state.getValue(FACING)) {
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
			if (!state.getValue(SLAVE)) {
				worldIn.setBlock(getSlavePos(pos, state), state.setValue(SLAVE, true), 3);
			}
		}
		
		@Override
		public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			makeChild(worldIn, pos, state);
		}
		
		@Override
		public boolean canSurvive(BlockState state, IWorldReader world, BlockPos pos) {
			return world.isEmptyBlock(pos) && world.isEmptyBlock(getSlavePos(pos, state));
		}
		
		@Override
		@Nullable
		public BlockState getStateForPlacement(BlockItemUseContext context) {
			final World world = context.getLevel();
			final BlockPos pos = context.getClickedPos();
			final BlockState state = super.getStateForPlacement(context);
			if (!canSurvive(state, world, pos)) {
				return null;
			}

			return state;
		}
		
		@Override
		protected void setOpen(BlockState state, World worldIn, BlockPos pos) {
			if (!state.getValue(SLAVE)) {
				final BlockPos slavePos = getSlavePos(pos, state);
				setOpen(worldIn.getBlockState(slavePos), worldIn, slavePos);
			}
			super.setOpen(state, worldIn, pos);
		}
		
		@Override
		public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos changedPos) {
			if (stateIn.getValue(SLAVE)) {
				// If it was master position that changed, react
				if (changedPos.equals(getMasterPos(currentPos, stateIn)) && facingState.getBlock() != this) {
					return Blocks.AIR.defaultBlockState();
				}
			} else {
				if (changedPos.equals(getSlavePos(currentPos, stateIn)) && facingState.getBlock() != this) {
					return Blocks.AIR.defaultBlockState();
				}
			}
			
			return stateIn;
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
			if (isWorldGen && worldIn.getBlockEntity(pos) != null && !(worldIn.getBlockEntity(pos) instanceof DungeonKeyChestTileEntity)) {
				worldIn.removeBlock(pos, false);
			}
			
			worldIn.setBlock(pos, this.defaultBlockState().setValue(FACING, facing), 3);
			makeChild(worldIn, pos, worldIn.getBlockState(pos));
			
			DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getBlockEntity(pos);
			chest.setWorldKey(dungeon.getLargeKey(), WorldUtil.IsWorldGen(worldIn));
		}
		
		@Override
		public boolean isLargeKey(BlockState state) {
			return !state.getValue(SLAVE);
		}
	}
}
