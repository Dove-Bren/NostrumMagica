package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.items.NostrumResourceItem;
import com.smanzana.nostrummagica.items.NostrumResourceItem.ResourceType;
import com.smanzana.nostrummagica.world.dimension.NostrumEmptyDimension;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyDirection;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.BlockState;
import net.minecraft.item.Item;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class ManiCrystal extends Block {

	public static final String ID = "mani_crystal";
	public static final PropertyInteger LEVEL = PropertyInteger.create("level", 0, 1);
	public static final PropertyDirection FACING = PropertyDirection.create("facing");
	
	protected static final AxisAlignedBB STANDING_AABB = new AxisAlignedBB(.5-(.16), 0.1D, .5-.16, .5+.16, 0.8D, .5+.16);
	protected static final AxisAlignedBB HANGING_AABB = new AxisAlignedBB(.5-(.16), 0.1D, .5-.16, .5+.16, 0.8D, .5+.16);
	protected static final AxisAlignedBB WALL_NORTH_AABB = new AxisAlignedBB(.5-.16, 0.2D, 1-.16, .5 + .16, 0.8D, 1);
	protected static final AxisAlignedBB WALL_EAST_AABB = new AxisAlignedBB(0, 0.2D, .5-.16, .16, 0.8D, .5+.16);
	protected static final AxisAlignedBB WALL_SOUTH_AABB = new AxisAlignedBB(.5-.16, 0.2D, 0, .5 + .16, 0.8D, .16);
	protected static final AxisAlignedBB WALL_WEST_AABB = new AxisAlignedBB(1-.16, 0.2D, .5-.16, 1, 0.8D, .5+.16);
	
	private static ManiCrystal instance = null;
	public static ManiCrystal instance() {
		if (instance == null)
			instance = new ManiCrystal();
		
		return instance;
	}
	
	
	public ManiCrystal() {
		super(Material.ROCK, MapColor.DIAMOND);
		this.setUnlocalizedName(ID);
		this.setHardness(1.0f);
		this.setResistance(50.0f);
		this.setCreativeTab(NostrumMagica.creativeTab);
		this.setSoundType(SoundType.GLASS);
		this.setHarvestLevel("pickaxe", 1);
		this.setTickRandomly(true);
		this.setLightOpacity(0);
		
		this.setDefaultState(this.blockState.getBaseState().withProperty(LEVEL, 0).withProperty(FACING, Direction.UP));
	}
	
	@Override
	protected BlockStateContainer createBlockState() {
		return new BlockStateContainer(this, LEVEL, FACING);
	}
	
	@Override
	public int getLightValue(BlockState state) {
		switch (state.getValue(LEVEL)) {
		case 0:
		default:
			return 8;
		case 1:
			return 12;
		}
	}
	
	protected Direction facingFromMeta(int meta) {
		return Direction.getFront(meta & 0x7);
	}
	
	protected int metaFromFacing(Direction facing) {
		return facing.getIndex();
	}
	
	protected int levelFromMeta(int meta) {
		return (meta >> 3) & 1;
	}
	
	protected int metaFromLevel(int level) {
		return (level & 1) << 3;
	}
	
	@Override
	public BlockState getStateFromMeta(int meta) {
		return getDefaultState().withProperty(LEVEL, levelFromMeta(meta)).withProperty(FACING, facingFromMeta(meta));
	}
	
	@Override
	public int getMetaFromState(BlockState state) {
		return metaFromFacing(state.getValue(FACING)) | metaFromLevel(state.getValue(LEVEL));
	}
	
	@Override
	public int quantityDroppedWithBonus(int fortune, Random random) {
		return 1;
	}
	
	@Override
	public Item getItemDropped(BlockState state, Random rand, int fortune) {
		return NostrumResourceItem.instance();
	}
	
	@Override
	public int damageDropped(BlockState state) {
		switch (state.getValue(LEVEL)) {
		case 0:
		default:
			return NostrumResourceItem.getMetaFromType(ResourceType.CRYSTAL_SMALL);
		case 1:
			return NostrumResourceItem.getMetaFromType(ResourceType.CRYSTAL_MEDIUM);
		}
	}
	
	@Override
	public int getExpDrop(BlockState state, net.minecraft.world.IBlockAccess world, BlockPos pos, int fortune) {
		return 0;
	}
	
	@Override
	public void randomTick(World worldIn, BlockPos pos, BlockState state, Random random) {
		if (worldIn.isRemote) {
			return;
		}
		
		if (worldIn.provider instanceof NostrumEmptyDimension.EmptyDimensionProvider) {
			return;
		}
		
		if (random.nextInt(2) <= state.getValue(LEVEL)) {
			
			// Check if there are too many already
			if (worldIn.getEntitiesWithinAABB(EntityWisp.class, this.getBoundingBox(state, worldIn, pos).offset(pos).grow(20)).size() > 5) {
				return;
			}
			
			BlockPos spawnPos = null;
			
			// Try to find a safe place to spawn the wisp
			int attempts = 20;
			do {
				spawnPos = pos.add(
						NostrumMagica.rand.nextInt(10) - 5,
						NostrumMagica.rand.nextInt(5),
						NostrumMagica.rand.nextInt(10) - 5);
			} while (!worldIn.isAirBlock(spawnPos) && attempts-- >= 0);
			
			if (worldIn.isAirBlock(spawnPos)) {
				EntityWisp wisp = new EntityWisp(worldIn, pos);
				wisp.setPosition(spawnPos.getX() + .5, spawnPos.getY(), spawnPos.getZ() + .5);
				worldIn.spawnEntity(wisp);
			}
		}
	}
	
	@Override
	public AxisAlignedBB getBoundingBox(BlockState state, IBlockAccess source, BlockPos pos) {
		switch (state.getValue(FACING)) {
		case DOWN:
			return new AxisAlignedBB(.5-(.16), .55, .5-.16, .5+.16, 1.0, .5+.16);
			//return HANGING_AABB;
		case EAST:
			return WALL_EAST_AABB;
		case NORTH:
			return WALL_NORTH_AABB;
		case SOUTH:
			return WALL_SOUTH_AABB;
		case UP:
		default:
			return STANDING_AABB;
		case WEST:
			return WALL_WEST_AABB;
		}
	}
	
	@Override
	public AxisAlignedBB getCollisionBoundingBox(BlockState blockState, IBlockAccess worldIn, BlockPos pos) {
		Direction facing = blockState.getValue(FACING);
		if (facing == null) {
			facing = Direction.UP;
		}
		
		switch (facing) {
		case EAST:
		case NORTH:
		case SOUTH:
		case WEST:
			return null;
		case UP:
		default:
			return STANDING_AABB;
		case DOWN:
			return HANGING_AABB;
		}
	}
	
	@Override
	public boolean isOpaqueCube(BlockState state) {
		return false;
	}
	
	@Override
	public boolean isFullCube(BlockState state) {
		return false;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getBlockLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isTranslucent(BlockState state) {
		return true;
	}
	
	@Override
	public void onNeighborChange(IBlockAccess world, BlockPos pos, BlockPos neighbor) {
		super.onNeighborChange(world, pos, neighbor);
		
		if (world instanceof World && !((World) world).isRemote) {
			BlockState state = world.getBlockState(pos);
			Direction facing = state.getValue(FACING);
			if (facing == Direction.UP || facing == Direction.DOWN) {
				return;
			}
			
			if (world.isAirBlock(pos.offset(facing.getOpposite()))) {
				this.dropBlockAsItem((World) world, pos, state, 0);
				((World) world).setBlockToAir(pos);
			}
		}
	}
	
	/**
	 * Get a useful offset for this crystal for things like effects to go to to go 'to' the crystal
	 * @return
	 */
	public Vec3d getCrystalTipOffset(BlockState state) {
		Direction facing = state.getValue(FACING);
		Vec3d offset = Vec3d.ZERO;
		if (facing != null) {
			switch (facing) {
			case DOWN:
				offset = new Vec3d(.5, .55, .5);
				break;
			case UP:
				offset = new Vec3d(.5, .8, .5);
				break;
			case EAST:
				offset = new Vec3d(.16, .5, .5);
				break;
			case WEST:
				offset = new Vec3d(1-.16, .5, .5);
				break;
			case NORTH:
				offset = new Vec3d(.5, .5, 1-.16);
				break;
			case SOUTH:
				offset = new Vec3d(.5, .5, .16);
				break;
			}
		}
		
		return offset;
	}
	
}
