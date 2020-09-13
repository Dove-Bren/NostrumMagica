package com.smanzana.nostrummagica.blocks;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.sound.NostrumMagicaSounds;

import net.minecraft.block.BlockHorizontal;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public abstract class NostrumMagicDoor extends BlockHorizontal {
	
	protected static final PropertyBool MASTER = PropertyBool.create("master");
	protected static final AxisAlignedBB MIRROR_AABB_EW = new AxisAlignedBB(0.4D, 0.0D, 0D, 0.6D, 1.0D, 1D);
	protected static final AxisAlignedBB MIRROR_AABB_NS = new AxisAlignedBB(0D, 0.0D, 0.4D, 1D, 1D, 0.6D);

	public NostrumMagicDoor() {
		super(Material.ROCK, MapColor.NETHERRACK);
		this.setHardness(500.0f);
		this.setResistance(900.0f);
		this.setBlockUnbreakable();
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.STONE);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(MASTER, false).withProperty(FACING, EnumFacing.NORTH));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, MASTER, FACING);
	}
	
	@Override
	public IBlockState getStateFromMeta(int meta) {
		return getDefaultState()
				.withProperty(MASTER, (meta & 1) == 1)
				.withProperty(FACING, EnumFacing.getHorizontal((meta >> 1) & 3));
	}
	
	@Override
	public int getMetaFromState(IBlockState state) {
		int meta = (state.getValue(MASTER) ? 1 : 0)
				| (state.getValue(FACING).getHorizontalIndex() << 1);
			
		return meta;
	}
	
	private void destroy(World world, BlockPos pos, IBlockState state) {
		if (world.isRemote)
			return;
		
		if (state == null)
			state = world.getBlockState(pos);
		
		if (state == null)
			return;
		
		if (state.getValue(MASTER)) {
			
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
	
	protected BlockPos getMasterPos(World world, IBlockState state, BlockPos pos) {
		return walkDoor(world, pos, state, (checkPos, checkState) -> {
			return isMaster(checkState);
		});
	}
	
	private static interface IDoorWalker {
		// Called when visiting a door block. Return true to stop walking and return current pos.
		public boolean walk(BlockPos pos, IBlockState state);
	}
	
	private BlockPos walkDoor(World world, BlockPos start, IBlockState startState, IDoorWalker walkFunc) {
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		
		next.add(start);
		
		while (!next.isEmpty()) {
			BlockPos cur = next.remove(0);
			
			if (visited.contains(cur))
				continue;
			
			visited.add(cur);
			IBlockState state = world.getBlockState(cur);
			
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
			if (state.getValue(FACING).getHorizontalIndex() % 2 != 0) {
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
	
	public IBlockState getSlaveState(EnumFacing facing) {
		return this.getDefaultState().withProperty(MASTER, false).withProperty(FACING, facing);
	}
	
	public boolean isMaster(IBlockState state) {
		return state.getValue(MASTER);
	}


	public IBlockState getMaster(EnumFacing facing) {
		return this.getDefaultState().withProperty(MASTER, true).withProperty(FACING, facing);
	}
	
	@Override
	public void breakBlock(World world, BlockPos pos, IBlockState state) {
		this.destroy(world, pos, state);
		world.removeTileEntity(pos);
		super.breakBlock(world, pos, state);
	}
	
	@Override
	public boolean isPassable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public boolean isVisuallyOpaque() {
		return false;
	}
	
	@Override
	public boolean isOpaqueCube(IBlockState state) {
		return false;
	}
	
	@Override
	public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
        return false;
    }
	
	@Override
	public int getLightOpacity(IBlockState state, IBlockAccess world, BlockPos pos) {
		return 16;
	}
	
	@Override
	public boolean isBlockSolid(IBlockAccess worldIn, BlockPos pos, EnumFacing side) {
		return true;
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, World worldIn, BlockPos pos) {
		if (blockState.getValue(FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
		if (state.getValue(FACING).getHorizontalIndex() % 2 != 0)
			return MIRROR_AABB_EW;
		return MIRROR_AABB_NS;
	}
	
	@Override
	public IBlockState getStateForPlacement(World world, BlockPos pos, EnumFacing facing, float hitX, float hitY, float hitZ, int meta, EntityLivingBase placer, ItemStack stack) {
		EnumFacing enumfacing = EnumFacing.getHorizontal(MathHelper.floor_double((double)(placer.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3).getOpposite();
		return getMaster(enumfacing);
	}
	
	@Override
	public void onBlockPlacedBy(World worldIn, BlockPos pos, IBlockState state, EntityLivingBase placer, ItemStack stack) {
		this.spawnDoor(worldIn, pos, state);
	}
	
	private void spawnDoor(World world, BlockPos masterBlock, IBlockState masterState) {
		// Fill all air blocks around the master in all directions that are ortho to facing
		Set<BlockPos> visited = new HashSet<>();
		List<BlockPos> next = new LinkedList<>();
		int blocksLeft = 64;
		
		// Unwrap first iteration of loop for master
		visited.add(masterBlock);
		next.add(masterBlock.up());
		next.add(masterBlock.down());
		if (masterState.getValue(FACING).getHorizontalIndex() % 2 != 0) {
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
			world.setBlockState(cur, getSlaveState(masterState.getValue(FACING)));
			
			next.add(cur.up());
			next.add(cur.down());
			if (masterState.getValue(FACING).getHorizontalIndex() % 2 != 0) {
				next.add(cur.north());
				next.add(cur.south());
			} else {
				next.add(cur.east());
				next.add(cur.west());
			}
		}
	}
	
	public boolean onBlockActivated(World worldIn, BlockPos pos, IBlockState state, EntityPlayer playerIn, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {
		return false;
	}
	
	public void clearDoor(World world, BlockPos onePos, IBlockState state) {
		destroy(world, onePos, state);
		NostrumMagicaSounds.AMBIENT_WOOSH2.play(world, onePos.getX(), onePos.getY(), onePos.getZ());
	}
}
