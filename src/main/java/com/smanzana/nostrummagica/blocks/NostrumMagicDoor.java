package com.smanzana.nostrummagica.blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

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
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public abstract class NostrumMagicDoor extends HorizontalBlock {
	
	protected static final BooleanProperty MASTER = BooleanProperty.create("master");
	protected static final VoxelShape MIRROR_AABB_EW = Block.makeCuboidShape(6.4D, 0.0D, 0D,09.6D, 16D, 16D);
	protected static final VoxelShape MIRROR_AABB_NS = Block.makeCuboidShape(0D, 0.0D, 6.4D, 1D, 1D, 9.6D);

	public NostrumMagicDoor() {
		this(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(-1.0F, 3600000.8F)
				.noDrops()
				.sound(SoundType.STONE)
				);
	}
	
	public NostrumMagicDoor(Block.Properties properties) {
		super(properties);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(MASTER, false).with(HORIZONTAL_FACING, Direction.NORTH));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, HORIZONTAL_FACING);
	}
	
	private void destroy(World world, BlockPos pos, BlockState state) {
		if (world.isRemote)
			return;
		
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.get(MASTER)) {
			
		} else {
			BlockPos master = getMasterPos(world, state, pos);
			if (master != null && world.getBlockState(master) != null && isMaster(world.getBlockState(master))) {
				world.destroyBlock(master, false);
			}
		}
		
		// Actually destroy
		walkDoor(world, pos, state, (checkPos, checkState) -> {
			world.destroyBlock(checkPos, false);
			return false;
		});
		
		
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
	
	private static interface IDoorWalker {
		// Called when visiting a door block. Return true to stop walking and return current pos.
		public boolean walk(BlockPos pos, BlockState state);
	}
	
	private BlockPos walkDoor(World world, BlockPos start, BlockState startState, IDoorWalker walkFunc) {
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		
		next.add(start);
		
		while (!next.isEmpty()) {
			BlockPos cur = next.remove(0);
			
			if (visited.contains(cur))
				continue;
			
			visited.add(cur);
			BlockState state = world.getBlockState(cur);
			
			if (start == cur) {
				// Block was already destroyed, so use saved blockstate
				state = startState;
			} else {
				if (state == null || !(state.getBlock() instanceof NostrumMagicDoor))
					continue;
			}
			
			if (walkFunc.walk(cur, state)) {
				return cur;
			}
			
			next.add(cur.up());
			next.add(cur.down());
			if (state.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0) {
				next.add(cur.north());
				next.add(cur.south());
			} else {
				next.add(cur.east());
				next.add(cur.west());
			}
//			next.add(cur.east());
//			next.add(cur.west());
//			next.add(cur.north());
//			next.add(cur.south());
		}
		
		return null;
	}
	
	public BlockState getSlaveState(Direction facing) {
		return this.getDefaultState().with(MASTER, false).with(HORIZONTAL_FACING, facing);
	}
	
	public boolean isMaster(BlockState state) {
		return state.get(MASTER);
	}


	public BlockState getMaster(Direction facing) {
		return this.getDefaultState().with(MASTER, true).with(HORIZONTAL_FACING, facing);
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			this.destroy(worldIn, pos, state);
			worldIn.removeTileEntity(pos);
		}
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
        return false;
    }
	
//	@Override
//	public int getLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
//		return 16;
//	}
	
	@Override
	public boolean isSolid(BlockState state) {
		return true;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		if (state.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		Direction enumfacing = context.getPlacementHorizontalFacing().getOpposite();
		return getMaster(enumfacing);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
		this.spawnDoor(worldIn, pos, state);
	}
	
	private void spawnDoor(World world, BlockPos masterBlock, BlockState masterState) {
		// Fill all air blocks around the master in all directions that are ortho to facing
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		int blocksLeft = 64;
		
		// Unwrap first iteration of loop for master
		visited.add(masterBlock);
		next.add(masterBlock.up());
		next.add(masterBlock.down());
		if (masterState.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0) {
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
			
			if (!world.isAirBlock(cur))
				continue;
			
			blocksLeft--;
			
			visited.add(cur);
			world.setBlockState(cur, getSlaveState(masterState.get(HORIZONTAL_FACING)));
			
			next.add(cur.up());
			next.add(cur.down());
			if (masterState.get(HORIZONTAL_FACING).getHorizontalIndex() % 2 != 0) {
				next.add(cur.north());
				next.add(cur.south());
			} else {
				next.add(cur.east());
				next.add(cur.west());
			}
		}
	}
	
	public boolean onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
		return false;
	}
	
	public void clearDoor(World world, BlockPos onePos, BlockState state) {
		destroy(world, onePos, state);
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, onePos.getX(), onePos.getY(), onePos.getZ());
	}
}
