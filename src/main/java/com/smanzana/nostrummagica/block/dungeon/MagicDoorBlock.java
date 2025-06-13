package com.smanzana.nostrummagica.block.dungeon;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import javax.annotation.Nullable;

import com.smanzana.autodungeons.util.WorldUtil;
import com.smanzana.autodungeons.util.WorldUtil.IBlockWalker;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public abstract class MagicDoorBlock extends HorizontalDirectionalBlock {
	
	protected static final BooleanProperty MASTER = BooleanProperty.create("master");
	protected static final VoxelShape MIRROR_AABB_EW = Block.box(6.4D, 0.0D, 0D,09.6D, 16D, 16D);
	protected static final VoxelShape MIRROR_AABB_NS = Block.box(0D, 0.0D, 6.4D, 16D, 16D, 9.6D);

	public MagicDoorBlock() {
		this(Block.Properties.of(Material.STONE)
				.strength(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE)
				);
	}
	
	protected MagicDoorBlock(Block.Properties properties) {
		super(properties);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(MASTER, false).setValue(FACING, Direction.NORTH));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	private void destroy(Level world, BlockPos pos, BlockState state) {
		if (world.isClientSide)
			return;
		
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.getValue(MASTER)) {
			// Cascade destroy to everything else
			walkDoor(world, pos, state, (checkPos, checkState) -> {
				world.destroyBlock(checkPos, false);
				return false;
			});
		} else {
			// Use whether master is still in place to tell if the whole door is already destructing.
			// If NOT the master, check if it's still there and destroy it. Master walks and destroys everything.
			BlockPos master = getMasterPos(world, state, pos);
			if (master != null && world.getBlockState(master) != null && world.getBlockState(master).getBlock() == this && isMaster(world.getBlockState(master))) {
				world.destroyBlock(master, false);
			}
		}
		
		((ServerLevel)world).addParticle(ParticleTypes.LAVA, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .01, 0, .01);
		// tODO this used to spawn 100 of them
//		((ServerWorld)world).addParticle(ParticleTypes., pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
//				.3, .3, .3);
	}
	
	protected BlockPos getMasterPos(Level world, BlockState state, BlockPos pos) {
		return walkDoor(world, pos, state, (checkPos, checkState) -> {
			return isMaster(checkState);
		});
	}
	
	protected static interface IDoorWalker {
		// Called when visiting a door block. Return true to stop walking and return current pos.
		public boolean walk(BlockPos pos, BlockState state);
	}
	
	protected BlockPos walkDoor(Level world, BlockPos start, BlockState startState, IDoorWalker walkFunc) {
		final BlockState origState = startState;
		return WorldUtil.WalkConnectedBlocks(world, start, new IBlockWalker() {

			@Override
			public boolean canVisit(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance) {
				if (state == null || !(state.getBlock() instanceof MagicDoorBlock))
					return false;
				
				// Also should be in line with door
				final Direction facing = origState.getValue(FACING);
				return (pos.getX() == startPos.getX() || facing.get2DDataValue() % 2 == 0) // E/W vary on X
						&& (pos.getZ() == startPos.getZ() || facing.get2DDataValue() % 2 != 0);
						
			}

			@Override
			public IBlockWalker.WalkResult walk(BlockGetter world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance, int walkCount, Consumer<BlockPos> addBlock) {
				if (startPos.equals(pos)) {
					// Block was already destroyed, so use saved blockstate
					state = origState;
				}
				return walkFunc.walk(pos, state) ? IBlockWalker.WalkResult.ABORT : IBlockWalker.WalkResult.CONTINUE;
			}
		}, 512);
		
		
//		Set<BlockPos> visited = new HashSet<>();
//		List<BlockPos> next = new LinkedList<>();
//		
//		next.add(start);
//		
//		while (!next.isEmpty()) {
//			BlockPos cur = next.remove(0);
//			
//			if (visited.contains(cur))
//				continue;
//			
//			visited.add(cur);
//			BlockState state = world.getBlockState(cur);
//			
//			if (start == cur) {
//				// Block was already destroyed, so use saved blockstate
//				state = startState;
//			} else {
//				if (state == null || !(state.getBlock() instanceof MagicDoorBlock))
//					continue;
//			}
//			
//			if (walkFunc.walk(cur, state)) {
//				return cur;
//			}
//			
//			next.add(cur.up());
//			next.add(cur.down());
//			if (state.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0) {
//				next.add(cur.north());
//				next.add(cur.south());
//			} else {
//				next.add(cur.east());
//				next.add(cur.west());
//			}
////			next.add(cur.east());
////			next.add(cur.west());
////			next.add(cur.north());
////			next.add(cur.south());
//		}
//		
//		return null;
	}
	
	public BlockState getSlaveState(Direction facing) {
		return this.defaultBlockState().setValue(MASTER, false).setValue(FACING, facing);
	}
	
	public boolean isMaster(BlockState state) {
		return state.getValue(MASTER);
	}


	public BlockState getMaster(Direction facing) {
		return this.defaultBlockState().setValue(MASTER, true).setValue(FACING, facing);
	}
	
	@Override
	public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
			worldIn.removeBlockEntity(pos);
		}
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
        return false;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		if (state.getValue(FACING).get2DDataValue() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		Direction enumfacing = context.getHorizontalDirection().getOpposite();
		return getMaster(enumfacing);
	}
	
	@Override
	public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		this.spawnDoor(worldIn, pos, state, null);
	}
	
	protected void spawnDoor(LevelAccessor world, BlockPos masterBlock, BlockState masterState, @Nullable BoundingBox bounds) {
		// Fill all air blocks around the master in all directions that are ortho to facing
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		int blocksLeft = 64;
		
		// Unwrap first iteration of loop for master
		visited.add(masterBlock);
		next.add(masterBlock.above());
		next.add(masterBlock.below());
		if (masterState.getValue(FACING).get2DDataValue() % 2 != 0) {
			next.add(masterBlock.north());
			next.add(masterBlock.south());
		} else {
			next.add(masterBlock.east());
			next.add(masterBlock.west());
		}
		
		while (!next.isEmpty() && blocksLeft > 0) {
			BlockPos cur = next.remove(0);
			
			if (visited.contains(cur))
				continue;
			
			visited.add(cur);
			
			if (bounds != null && !bounds.isInside(cur)) {
				continue;
			}
			
			if (!world.isEmptyBlock(cur))
				continue;
			
			blocksLeft--;
			
			world.setBlock(cur, getSlaveState(masterState.getValue(FACING)), 3);
			
			next.add(cur.above());
			next.add(cur.below());
			if (masterState.getValue(FACING).get2DDataValue() % 2 != 0) {
				next.add(cur.north());
				next.add(cur.south());
			} else {
				next.add(cur.east());
				next.add(cur.west());
			}
		}
	}
	
	public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
		return InteractionResult.PASS;
	}
	
	public void clearDoor(Level world, BlockPos onePos, BlockState state) {
		destroy(world, onePos, state);
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, onePos.getX(), onePos.getY(), onePos.getZ());
	}
	
	public static final BlockPos FindBottomCenterPos(Level world, BlockPos samplePos) {
		// Master is at TE's pos... but is it the bottom block? And is it in center?
		final BlockState startState = world.getBlockState(samplePos);
		final Direction face = startState.getValue(MagicDoorBlock.FACING);
		final Block matchBlock = startState.getBlock();
		
		// Find bottom
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(samplePos);
		cursor.move(Direction.DOWN, 1);
		
		while (cursor.getY() >= 0) {
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
			
			cursor.move(Direction.DOWN);
		}
		
		// Move back to last good position
		cursor.move(Direction.UP);
		BlockPos bottomPos = new BlockPos(cursor);
		
		// Now discover left and right
		// Right:
		while (true) {
			cursor.move(face.getClockWise());
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
		}
		
		// Move back
		cursor.move(face.getCounterClockWise());
		BlockPos rightPos = new BlockPos(cursor);
		cursor.set(bottomPos);
		
		// Left
		while (true) {
			cursor.move(face.getCounterClockWise());
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
		}
		
		// Move back
		cursor.move(face.getClockWise());
		BlockPos leftPos = new BlockPos(cursor);
		
		return new BlockPos(
				.5 * (rightPos.getX() + leftPos.getX()),
				bottomPos.getY(),
				.5 * (rightPos.getZ() + leftPos.getZ()));
	}
	
	public static final BoundingBox FindDisplayBounds(Level world, BlockPos samplePos) {
		// Master is at TE's pos... but is it the bottom block? And is it in center?
		final BlockState startState = world.getBlockState(samplePos);
		final Direction face = startState.getValue(MagicDoorBlock.FACING);
		final Block matchBlock = startState.getBlock();
		
		// Find bottom
		BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos().set(samplePos);
		cursor.move(Direction.DOWN, 1);
		
		while (cursor.getY() >= 0) {
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
			
			cursor.move(Direction.DOWN);
		}
		
		// Move back to last good position
		cursor.move(Direction.UP);
		BlockPos bottomPos = new BlockPos(cursor);
		
		// Now discover left and right
		// Right:
		while (true) {
			cursor.move(face.getClockWise());
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
		}
		
		// Move back and record
		cursor.move(face.getCounterClockWise());
		
		// Find highest at this position
		while (true) {
			cursor.move(Direction.UP);
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
		}
		
		// Move back and record
		cursor.move(Direction.DOWN);
		BlockPos rightTopPos = new BlockPos(cursor);
		
		
		// Left
		cursor.set(bottomPos);
		while (true) {
			cursor.move(face.getCounterClockWise());
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
		}
		
		// Move back and record
		cursor.move(face.getClockWise());
		
		// Find highest at this position
		while (true) {
			cursor.move(Direction.UP);
			BlockState state = world.getBlockState(cursor);
			if (state == null || state.getBlock() != matchBlock)
				break;
		}
		
		// Move back and record
		cursor.move(Direction.DOWN);
		BlockPos leftTopPos = new BlockPos(cursor);
		
		final int maxY = Math.min(leftTopPos.getY(), rightTopPos.getY());
		
		BlockPos bottomLeft = new BlockPos(leftTopPos.getX(), bottomPos.getY(), leftTopPos.getZ());
		BlockPos topRight = new BlockPos(rightTopPos.getX(), maxY, rightTopPos.getZ());
		
		return BoundingBox.fromCorners(bottomLeft, topRight); // constructor takes care of min/maxing x and z
	}
}
