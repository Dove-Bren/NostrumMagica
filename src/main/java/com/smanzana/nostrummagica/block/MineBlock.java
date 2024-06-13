package com.smanzana.nostrummagica.block;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.item.equipment.AspectedEarthWeapon;
import com.smanzana.nostrummagica.util.HarvestUtil;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.EvokerFangsEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.common.ToolType;

public class MineBlock extends Block {

	public static final String ID = "mine_block";
	private static final double AABB_MIN = 0.0D;
	private static final double AABB_MAX = 16.0D;
	private static final double AABB_WIDTH = 0.01D;
	private static final VoxelShape MINE_AABB_UP = Block.makeCuboidShape(AABB_MIN, AABB_MAX - AABB_WIDTH, AABB_MIN, AABB_MAX, AABB_MAX, AABB_MAX);
	private static final VoxelShape MINE_AABB_DOWN = Block.makeCuboidShape(AABB_MIN, AABB_MIN, AABB_MIN, AABB_MAX, AABB_MIN + AABB_WIDTH, AABB_MAX);
	private static final VoxelShape MINE_AABB_NORTH = Block.makeCuboidShape(AABB_MIN, AABB_MIN, AABB_MIN, AABB_MAX, AABB_MAX, AABB_MIN + AABB_WIDTH);
	private static final VoxelShape MINE_AABB_SOUTH = Block.makeCuboidShape(AABB_MIN, AABB_MIN, AABB_MAX - AABB_WIDTH, AABB_MAX, AABB_MAX, AABB_MAX);
	private static final VoxelShape MINE_AABB_EAST = Block.makeCuboidShape(AABB_MAX - AABB_WIDTH, AABB_MIN, AABB_MIN, AABB_MAX, AABB_MAX, AABB_MAX);
	private static final VoxelShape MINE_AABB_WEST = Block.makeCuboidShape(AABB_MIN, AABB_MIN, AABB_MIN, AABB_MIN + AABB_WIDTH, AABB_MAX, AABB_MAX);
	
	public static final DirectionProperty FACING = DirectionProperty.create("facing", Direction.values());
	public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 3);
	
	public MineBlock() {
		super(Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(2f)
				.noDrops()
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(3)
				.notSolid()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP).with(LEVEL, 0));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING, LEVEL);
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		switch (state.get(FACING)) {
		case DOWN:
			return MINE_AABB_DOWN;
		case EAST:
			return MINE_AABB_EAST;
		case NORTH:
			return MINE_AABB_NORTH;
		case SOUTH:
			return MINE_AABB_SOUTH;
		case UP:
		default:
			return MINE_AABB_UP;
		case WEST:
			return MINE_AABB_WEST;
		}
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return hasEnoughSolidSide(worldIn, pos.offset(state.get(FACING)), state.get(FACING).getOpposite());
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return facing == stateIn.get(FACING) && !this.isValidPosition(stateIn, worldIn, currentPos) ? Blocks.AIR.getDefaultState() : super.updatePostPlacement(stateIn, facing, facingState, worldIn, currentPos, facingPos);
	}
	
	@Override
	public void harvestBlock(World worldIn, PlayerEntity player, BlockPos pos, BlockState state, @Nullable TileEntity te, ItemStack stack) {
		super.harvestBlock(worldIn, player, pos, state, te, stack);
		
		// If player mined with Earth Pike, do full mine effect
		if (!stack.isEmpty() && stack.getItem() instanceof AspectedEarthWeapon && !worldIn.isRemote()) {
			doHarvestEffect(worldIn, player, pos, state.get(FACING), state.get(LEVEL), stack);
		}
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!worldIn.isRemote()) {
			if (entityIn instanceof LivingEntity) {
				doEntityEffect(worldIn, (LivingEntity) entityIn, state.get(LEVEL), pos);
				worldIn.destroyBlock(pos, false);
			}
		}
	}
	
	protected void doHarvestEffect(World world, PlayerEntity player, BlockPos pos, Direction face, int level, ItemStack stack) {
		if (level == 0) {
			HarvestUtil.WalkVein(world, pos.offset(face), (walkWorld, walkPos, depth, state) -> {
				//world.destroyBlock(walkPos, true);
				((ServerPlayerEntity) player).interactionManager.tryHarvestBlock(walkPos);
				return true;
			});
		} else {
			// Do a set shape instead
			final int hRange; // left/right of face 
			final int vRange; // up/down of face (offset by 1)
			final int dRange; // near/far of face
			if (level == 1) {
				// Small box
				hRange = 3;
				vRange = 3;
				dRange = 3;
			} else if (level == 2) {
				// Large box
				hRange = 5;
				vRange = 5;
				dRange = 5;
			} else /*if (level == 3)*/ {
				// Deep box
				hRange = 3;
				vRange = 3;
				dRange = 10;
			}
			
			int xMin = -1;
			int xMax = 1;
			int yMin = -1;
			int yMax = 1;
			int zMin = 0;
			int zMax = 10;
			switch (face) {
			case NORTH:
			default:
				xMin = -(hRange/2);
				xMax = hRange/2;
				yMin = -1;
				yMax = -1 + (vRange-1);
				zMin = -(dRange-1);
				zMax = 0;
				break;
			case EAST:
				zMin = -(hRange/2);
				zMax = hRange/2;
				yMin = -1;
				yMax = -1 + (vRange-1);
				xMin = 0;
				xMax = (dRange-1);
				break;
			case SOUTH:
				xMin = -(hRange/2);
				xMax = hRange/2;
				yMin = -1;
				yMax = -1 + (vRange-1);
				zMin = 0;
				zMax = (dRange-1);
				break;
			case WEST:
				zMin = -(hRange/2);
				zMax = hRange/2;
				yMin = -1;
				yMax = -1 + (vRange-1);
				xMin = -(dRange-1);
				xMax = 0;
				break;
			case DOWN:
			case UP:
				// depth is +/- y
				// x and z are arbitrary
				yMin = (face == Direction.DOWN ? -(dRange-1) : 0);
				yMax = (face == Direction.UP ? 0 : (dRange-1));
				xMin = -(hRange/2);
				xMax = hRange/2;
				zMin = -(vRange/2);
				zMax = (vRange/2);
				break;
			}
			
			final BlockPos start = pos.toImmutable().offset(face);
			for (int x = xMin; x <= xMax; x++)
			for (int y = yMin; y <= yMax; y++)
			for (int z = zMin; z <= zMax; z++) {
				((ServerPlayerEntity) player).interactionManager.tryHarvestBlock(start.add(x, y, z));
			}
		}
	}

	protected void doEntityEffect(World world, LivingEntity entity, int level, BlockPos pos) {
		entity.addPotionEffect(new EffectInstance(NostrumEffects.lootLuck, 20 * 10, level / 2));
		EvokerFangsEntity fangs = new EvokerFangsEntity(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0, null);
		world.addEntity(fangs);
	}
	
}
