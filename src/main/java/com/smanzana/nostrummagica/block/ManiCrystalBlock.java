package com.smanzana.nostrummagica.block;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.entity.NostrumEntityTypes;
import com.smanzana.nostrummagica.entity.WispEntity;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ManiCrystalBlock extends Block {

	private static final String ID_PREFIX = "mani_crystal_";
	public static final String ID_MANI = ID_PREFIX + "mani";
	public static final String ID_KANI = ID_PREFIX + "kani";
	public static final String ID_VANI = ID_PREFIX + "vani";
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	
	protected static final VoxelShape STANDING_AABB = Block.box(16*(.5-(.16)), 16*0.1D, 16*(.5-.16), 16*(.5+.16), 16*0.8D, 16*(.5+.16));
	protected static final VoxelShape HANGING_AABB = Block.box(16*(.5-(.16)), 16*.55, 16*(.5-.16), 16*(.5+.16), 16*1.0, 16*(.5+.16));
	protected static final VoxelShape WALL_NORTH_AABB = Block.box(16*(.5-.16), 16*0.2D, 16*(1-.16), 16*(.5 + .16), 16*0.8D, 16*1);
	protected static final VoxelShape WALL_EAST_AABB = Block.box(16*0, 16*0.2D, 16*(.5-.16), 16*.16, 16*0.8D, 16*(.5+.16));
	protected static final VoxelShape WALL_SOUTH_AABB = Block.box(16*(.5-.16), 16*0.2D, 16*0, 16*(.5 + .16), 16*0.8D, 16*.16);
	protected static final VoxelShape WALL_WEST_AABB = Block.box(16*(1-.16), 16*0.2D, 16*(.5-.16), 16*1, 16*0.8D, 16*(.5+.16));
	
	private final int level;
	
	public ManiCrystalBlock(int level) {
		super(Block.Properties.of(Material.STONE)
				.strength(1.0f, 50.0f)
				.sound(SoundType.GLASS)
				.randomTicks()
				.lightLevel(ManiCrystalBlock::getLightValue)
				);
		this.level = level;
		//this.setLightOpacity(0);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
	}
	
	protected int getLevel() {
		return this.level;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(FACING);
	}
	
	private static int getLightValue(BlockState state) {
		switch (((ManiCrystalBlock) state.getBlock()).getLevel()) {
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
	public void randomTick(BlockState state, ServerLevel worldIn, BlockPos pos, Random random) {
		if (worldIn.isClientSide) {
			return;
		}
		
		if (DimensionUtils.IsSorceryDim(worldIn)) {
			return;
		}
		
		if (getLevel() > 0 && (getLevel() >= 2 || random.nextInt(2) <= getLevel())) {
			
			// Check if there are too many already
			if (worldIn.getEntitiesOfClass(WispEntity.class, Shapes.block().bounds().move(pos).inflate(20)).size() > 5) {
				return;
			}
			
			BlockPos spawnPos = null;
			
			// Try to find a safe place to spawn the wisp
			int attempts = 20;
			do {
				spawnPos = pos.offset(
						NostrumMagica.rand.nextInt(10) - 5,
						NostrumMagica.rand.nextInt(5),
						NostrumMagica.rand.nextInt(10) - 5);
			} while (!worldIn.isEmptyBlock(spawnPos) && attempts-- >= 0);
			
			if (worldIn.isEmptyBlock(spawnPos)) {
				WispEntity wisp = new WispEntity(NostrumEntityTypes.wisp, worldIn, pos);
				wisp.setPos(spawnPos.getX() + .5, spawnPos.getY(), spawnPos.getZ() + .5);
				worldIn.addFreshEntity(wisp);
			}
		}
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		switch (state.getValue(FACING)) {
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
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState().setValue(FACING, context.getClickedFace());
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor world, BlockPos pos, BlockPos facingPos) {
		if (!world.isClientSide()) {
			Direction myFacing = stateIn.getValue(FACING);
			if (myFacing == Direction.UP || myFacing == Direction.DOWN) {
				return stateIn;
			}
			
			if (myFacing == facing.getOpposite()) {
				if (world.isEmptyBlock(facingPos)) {
					return Blocks.AIR.defaultBlockState();
				}
			}
			
		}
		return stateIn;
	}
	
	/**
	 * Get a useful offset for this crystal for things like effects to go to to go 'to' the crystal
	 * @return
	 */
	public Vec3 getCrystalTipOffset(BlockState state) {
		Direction facing = state.getValue(FACING);
		Vec3 offset = Vec3.ZERO;
		if (facing != null) {
			switch (facing) {
			case DOWN:
				offset = new Vec3(.5, .55, .5);
				break;
			case UP:
				offset = new Vec3(.5, .8, .5);
				break;
			case EAST:
				offset = new Vec3(.16, .5, .5);
				break;
			case WEST:
				offset = new Vec3(1-.16, .5, .5);
				break;
			case NORTH:
				offset = new Vec3(.5, .5, 1-.16);
				break;
			case SOUTH:
				offset = new Vec3(.5, .5, .16);
				break;
			}
		}
		
		return offset;
	}
	
}
