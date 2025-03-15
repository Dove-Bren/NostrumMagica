package com.smanzana.nostrummagica.block.dungeon;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;
import com.smanzana.nostrummagica.util.WorldUtil;
import com.smanzana.nostrummagica.util.WorldUtil.IBlockWalker;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.HorizontalBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.MutableBoundingBox;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class MagicDoorBlock extends HorizontalBlock {
	
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
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
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
		
		((ServerWorld)world).addParticle(ParticleTypes.LAVA, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5, .01, 0, .01);
		// tODO this used to spawn 100 of them
//		((ServerWorld)world).addParticle(ParticleTypes., pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
//				.3, .3, .3);
	}
	
	protected BlockPos getMasterPos(World world, BlockState state, BlockPos pos) {
		return walkDoor(world, pos, state, (checkPos, checkState) -> {
			return isMaster(checkState);
		});
	}
	
	protected static interface IDoorWalker {
		// Called when visiting a door block. Return true to stop walking and return current pos.
		public boolean walk(BlockPos pos, BlockState state);
	}
	
	protected BlockPos walkDoor(World world, BlockPos start, BlockState startState, IDoorWalker walkFunc) {
		final BlockState origState = startState;
		return WorldUtil.WalkConnectedBlocks(world, start, new IBlockWalker() {

			@Override
			public boolean canVisit(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance) {
				if (state == null || !(state.getBlock() instanceof MagicDoorBlock))
					return false;
				
				// Also should be in line with door
				final Direction facing = origState.getValue(FACING);
				return (pos.getX() == startPos.getX() || facing.get2DDataValue() % 2 == 0) // E/W vary on X
						&& (pos.getZ() == startPos.getZ() || facing.get2DDataValue() % 2 != 0);
						
			}

			@Override
			public boolean walk(IBlockReader world, BlockPos startPos, BlockState startState, BlockPos pos,
					BlockState state, int distance, int walkCount) {
				if (startPos.equals(pos)) {
					// Block was already destroyed, so use saved blockstate
					state = origState;
				}
				return walkFunc.walk(pos, state);
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
	public void onRemove(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
			worldIn.removeBlockEntity(pos);
		}
	}
	
	@Override
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.getValue(FACING).get2DDataValue() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction enumfacing = context.getHorizontalDirection().getOpposite();
		return getMaster(enumfacing);
	}
	
	@Override
	public void setPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		this.spawnDoor(worldIn, pos, state, null);
	}
	
	protected void spawnDoor(IWorld world, BlockPos masterBlock, BlockState masterState, @Nullable MutableBoundingBox bounds) {
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
	
	public ActionResultType use(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return ActionResultType.PASS;
	}
	
	public void clearDoor(World world, BlockPos onePos, BlockState state) {
		destroy(world, onePos, state);
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, onePos.getX(), onePos.getY(), onePos.getZ());
	}
	
	public static final BlockPos FindBottomCenterPos(World world, BlockPos samplePos) {
		// Master is at TE's pos... but is it the bottom block? And is it in center?
		final BlockState startState = world.getBlockState(samplePos);
		final Direction face = startState.getValue(MagicDoorBlock.FACING);
		final Block matchBlock = startState.getBlock();
		
		// Find bottom
		BlockPos.Mutable cursor = new BlockPos.Mutable().set(samplePos);
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
	
	public static final MutableBoundingBox FindDisplayBounds(World world, BlockPos samplePos) {
		// Master is at TE's pos... but is it the bottom block? And is it in center?
		final BlockState startState = world.getBlockState(samplePos);
		final Direction face = startState.getValue(MagicDoorBlock.FACING);
		final Block matchBlock = startState.getBlock();
		
		// Find bottom
		BlockPos.Mutable cursor = new BlockPos.Mutable().set(samplePos);
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
		
		return new MutableBoundingBox(bottomLeft, topRight); // constructor takes care of min/maxing x and z
	}
}
