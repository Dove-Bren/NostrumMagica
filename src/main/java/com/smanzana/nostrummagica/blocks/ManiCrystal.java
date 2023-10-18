package com.smanzana.nostrummagica.blocks;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.EntityWisp;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.world.dimension.NostrumDimensions;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ToolType;

public class ManiCrystal extends Block {

	private static final String ID_PREFIX = "mani_crystal_";
	public static final String ID_MANI = ID_PREFIX + "mani";
	public static final String ID_KANI = ID_PREFIX + "kani";
	public static final String ID_VANI = ID_PREFIX + "vani";
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	
	protected static final VoxelShape STANDING_AABB = Block.makeCuboidShape(.5-(.16), 0.1D, .5-.16, .5+.16, 0.8D, .5+.16);
	protected static final VoxelShape HANGING_AABB = Block.makeCuboidShape(.5-(.16), .55, .5-.16, .5+.16, 1.0, .5+.16);
	protected static final VoxelShape WALL_NORTH_AABB = Block.makeCuboidShape(.5-.16, 0.2D, 1-.16, .5 + .16, 0.8D, 1);
	protected static final VoxelShape WALL_EAST_AABB = Block.makeCuboidShape(0, 0.2D, .5-.16, .16, 0.8D, .5+.16);
	protected static final VoxelShape WALL_SOUTH_AABB = Block.makeCuboidShape(.5-.16, 0.2D, 0, .5 + .16, 0.8D, .16);
	protected static final VoxelShape WALL_WEST_AABB = Block.makeCuboidShape(1-.16, 0.2D, .5-.16, 1, 0.8D, .5+.16);
	
	private final int level;
	
	public ManiCrystal(int level) {
		super(Block.Properties.create(Material.ROCK)
				.hardnessAndResistance(1.0f, 50.0f)
				.sound(SoundType.GLASS)
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(1)
				.tickRandomly()
				);
		this.level = level;
		//this.setLightOpacity(0);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP));
	}
	
	protected int getLevel() {
		return this.level;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	@Override
	public int getLightValue(BlockState state) {
		switch (getLevel()) {
		case 0:
		default:
			return 4;
		case 1:
			return 8;
		case 2:
			return 12;
		}
	}
	
//	@Override
//	public int quantityDroppedWithBonus(int fortune, Random random) {
//		return 1;
//	}
//	
//	@Override
//	public Item getItemDropped(BlockState state, Random rand, int fortune) {
//		return NostrumResourceItem.instance();
//	}
//	
//	@Override
//	public int damageDropped(BlockState state) {
//		switch (state.get(LEVEL)) {
//		case 0:
//		default:
//			return NostrumResourceItem.getMetaFromType(ResourceType.CRYSTAL_SMALL);
//		case 1:
//			return NostrumResourceItem.getMetaFromType(ResourceType.CRYSTAL_MEDIUM);
//		}
//	}
	
	@Override
	public void randomTick(BlockState state, World worldIn, BlockPos pos, Random random) {
		if (worldIn.isRemote) {
			return;
		}
		
		if (worldIn.dimension.getType() == NostrumDimensions.EmptyDimension) {
			return;
		}
		
		if (getLevel() > 0 && (getLevel() >= 2 || random.nextInt(2) <= getLevel())) {
			
			// Check if there are too many already
			if (worldIn.getEntitiesWithinAABB(EntityWisp.class, VoxelShapes.fullCube().getBoundingBox().offset(pos).grow(20)).size() > 5) {
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
				EntityWisp wisp = new EntityWisp(NostrumEntityTypes.wisp, worldIn, pos);
				wisp.setPosition(spawnPos.getX() + .5, spawnPos.getY(), spawnPos.getZ() + .5);
				worldIn.addEntity(wisp);
			}
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
		case DOWN:
			return HANGING_AABB;
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
	@OnlyIn(Dist.CLIENT)
	public BlockRenderLayer getRenderLayer() {
		return BlockRenderLayer.TRANSLUCENT;
	}
	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public boolean isTranslucent(BlockState state) {
//		return true;
//	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld world, BlockPos pos, BlockPos facingPos) {
		if (!world.isRemote()) {
			Direction myFacing = stateIn.get(FACING);
			if (myFacing == Direction.UP || myFacing == Direction.DOWN) {
				return stateIn;
			}
			
			if (myFacing == facing.getOpposite()) {
				if (facingState.isAir(world, facingPos)) {
					return null;
				}
			}
			
		}
		return stateIn;
	}
	
	/**
	 * Get a useful offset for this crystal for things like effects to go to to go 'to' the crystal
	 * @return
	 */
	public Vec3d getCrystalTipOffset(BlockState state) {
		Direction facing = state.get(FACING);
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
