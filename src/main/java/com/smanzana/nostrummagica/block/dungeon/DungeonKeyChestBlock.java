package com.smanzana.nostrummagica.block.dungeon;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.AutoDungeons;
import com.smanzana.autodungeons.api.block.ILargeKeyMarker;
import com.smanzana.autodungeons.world.WorldKey;
import com.smanzana.autodungeons.world.dungeon.DungeonInstance;
import com.smanzana.autodungeons.world.dungeon.DungeonRecord;
import com.smanzana.autodungeons.world.dungeon.DungeonRoomInstance;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.tile.DungeonKeyChestTileEntity;
import com.smanzana.nostrummagica.util.WorldUtil;

import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class DungeonKeyChestBlock extends HorizontalDirectionalBlock implements ILargeKeyMarker, EntityBlock {
	
	public static DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
	public static BooleanProperty OPEN = BooleanProperty.create("open");
	
	protected DungeonKeyChestBlock(Block.Properties props) {
		super(props);
		
		this.registerDefaultState(this.defaultBlockState().setValue(OPEN, false));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING, OPEN);
	}
	
	@Override
	public abstract VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context);
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return false;
	}
	
	protected void setOpen(BlockState state, Level worldIn, BlockPos pos) {
		worldIn.setBlockAndUpdate(pos, state.setValue(OPEN, true));
	}
	
	public abstract void makeDungeonChest(LevelAccessor worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon);
	
	@Override
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		if (state.getValue(OPEN)) {
			return InteractionResult.PASS;
		}
		
		if (!worldIn.isClientSide()) {
			DungeonKeyChestTileEntity chest = (DungeonKeyChestTileEntity) worldIn.getBlockEntity(pos);
			if (player.isCreative() && player.isShiftKeyDown()) {
				DungeonRecord record = AutoDungeons.GetDungeonTracker().getDungeon(player);
				if (record != null) {
					WorldKey key = chest.isLarge() ? record.instance.getLargeKey() : record.instance.getSmallKey();
					chest.setWorldKey(key);
					player.sendMessage(new TextComponent("Set to dungeon key"), Util.NIL_UUID);
				} else {
					player.sendMessage(new TextComponent("Not in a dungeon, so no key to set"), Util.NIL_UUID);
				}
			} else {
				chest.open(player);
				setOpen(state, worldIn, pos);
			}
		}
		
		return InteractionResult.SUCCESS;
	}
	
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new DungeonKeyChestTileEntity(pos, state);
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.MODEL;
	}
	
	@Override
	@Nullable
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
	}
	
	@Override
	public boolean triggerEvent(BlockState state, Level worldIn, BlockPos pos, int id, int param) {
		DungeonKeyChestTileEntity tileentity = (DungeonKeyChestTileEntity) worldIn.getBlockEntity(pos);
		return tileentity == null ? false : tileentity.triggerEvent(id, param);
	}

	@Override
	public void setKey(LevelAccessor world, BlockState state, BlockPos pos, WorldKey key, DungeonRoomInstance instance, BoundingBox bounds) {
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
		public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
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
				return Shapes.block();
			}
		}

		@Override
		public void makeDungeonChest(LevelAccessor worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon) {
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
		protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
			super.createBlockStateDefinition(builder);
			builder.add(SLAVE);
		}

		@Override
		public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
			
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
				return Shapes.block();
			}
		}
		
		public VoxelShape getWholeShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
			// Put slave one together too
			final boolean slave = state.getValue(SLAVE);
			final VoxelShape base = this.getShape(state, worldIn, pos, context);
			final Vec3i offset;
			if (slave) {
				offset = this.getMasterPos(pos, state).subtract(pos);
			} else {
				offset = this.getSlavePos(pos, state).subtract(pos);
			}
			final VoxelShape otherBase = this.getShape(state.setValue(SLAVE, !slave), worldIn, pos, context);
			return Shapes.or(base, otherBase.move(offset.getX(), offset.getY(), offset.getZ()));
		}
		
		@Override
		public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
			if (state.getValue(SLAVE)) {
				final BlockPos masterPos = getMasterPos(pos, state);
				final BlockState masterState = worldIn.getBlockState(masterPos);
				if (masterState.getBlock() != this) {
					return InteractionResult.FAIL;
				} else {
					return this.use(masterState, worldIn, masterPos, player, handIn, hit);
				}
			}
			
			// Else do normal super
			return super.use(state, worldIn, pos, player, handIn, hit);
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
		
		protected void makeChild(LevelAccessor worldIn, BlockPos pos, BlockState state) {
			if (!state.getValue(SLAVE)) {
				worldIn.setBlock(getSlavePos(pos, state), state.setValue(SLAVE, true), 3);
			}
		}
		
		@Override
		public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
			makeChild(worldIn, pos, state);
		}
		
		@Override
		public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
			return world.isEmptyBlock(pos) && world.isEmptyBlock(getSlavePos(pos, state));
		}
		
		@Override
		@Nullable
		public BlockState getStateForPlacement(BlockPlaceContext context) {
			final Level world = context.getLevel();
			final BlockPos pos = context.getClickedPos();
			final BlockState state = super.getStateForPlacement(context);
			if (!canSurvive(state, world, pos)) {
				return null;
			}

			return state;
		}
		
		@Override
		protected void setOpen(BlockState state, Level worldIn, BlockPos pos) {
			if (!state.getValue(SLAVE)) {
				final BlockPos slavePos = getSlavePos(pos, state);
				setOpen(worldIn.getBlockState(slavePos), worldIn, slavePos);
			}
			super.setOpen(state, worldIn, pos);
		}
		
		@Override
		public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos changedPos) {
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
		public void makeDungeonChest(LevelAccessor worldIn, BlockPos pos, Direction facing, DungeonInstance dungeon) {
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
