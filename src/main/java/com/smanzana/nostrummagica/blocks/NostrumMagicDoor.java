package com.smanzana.nostrummagica.blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.state.BooleanProperty;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class NostrumMagicDoor extends BlockHorizontal {
	
	protected static final BooleanProperty MASTER = BooleanProperty.create("master");
	protected static final AxisAlignedBB MIRROR_AABB_EW = new AxisAlignedBB(0.4D, 0.0D, 0D, 0.6D, 1.0D, 1D);
	protected static final AxisAlignedBB MIRROR_AABB_NS = new AxisAlignedBB(0D, 0.0D, 0.4D, 1D, 1D, 0.6D);

	public NostrumMagicDoor() {
		super(Material.ROCK, MapColor.NETHERRACK);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(MASTER, false).with(FACING, Direction.NORTH));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(MASTER, FACING);
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.with(MASTER, (meta & 1) == 1)
				.with(FACING, Direction.getHorizontal((meta >> 1) & 3));
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		int meta = (state.get(MASTER) ? 1 : 0)
				| (state.get(FACING).getHorizontalIndex() << 1);
			
		return meta;
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
				world.setBlockToAir(master);
			}
		}
		
		// Actually destroy
		walkDoor(world, pos, state, (checkPos, checkState) -> {
			world.setBlockToAir(checkPos);
			return false;
		});
		
		
		((WorldServer)world).spawnParticle(EnumParticleTypes.LAVA, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
				2, .01, 0, .01, 1, new int[0]);
		((WorldServer)world).spawnParticle(EnumParticleTypes.SUSPENDED_DEPTH, pos.getX() + .5, pos.getY() + .5, pos.getZ() + .5,
				100, .3, .3, .3, 1, new int[0]);
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
			if (state.get(FACING).getHorizontalIndex() % 2 != 0) {
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
		return this.getDefaultState().with(MASTER, false).with(FACING, facing);
	}
	
	public boolean isMaster(BlockState state) {
		return state.get(MASTER);
	}


	public BlockState getMaster(Direction facing) {
		return this.getDefaultState().with(MASTER, true).with(FACING, facing);
	}
	
	@Override
	public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) { broke();
		this.destroy(world, pos, state);
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public int getLightOpacity(BlockState state, IBlockAccess world, BlockPos pos) {
		return 16;
	}
	
	@Override
	public boolean isSideSolid(BlockState state, IBlockAccess worldIn, BlockPos pos, Direction side) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		if (blockState.get(FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		if (state.get(FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public BlockState getStateForPlacement(World world, BlockPos pos, Direction facing, float hitX, float hitY, float hitZ, int meta, LivingEntity placer) {
		Direction enumfacing = Direction.getHorizontal(MathHelper.floor((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
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
		if (masterState.get(FACING).getHorizontalIndex() % 2 != 0) {
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
			world.setBlockState(cur, getSlaveState(masterState.get(FACING)));
			
			next.add(cur.up());
			next.add(cur.down());
			if (masterState.get(FACING).getHorizontalIndex() % 2 != 0) {
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
