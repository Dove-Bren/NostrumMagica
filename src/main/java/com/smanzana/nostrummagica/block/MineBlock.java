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
	
	public MineBlock() {
		super(Block.Properties.create(Material.CARPET)
				.hardnessAndResistance(2f)
				.noDrops()
				.harvestTool(ToolType.PICKAXE)
				.harvestLevel(3)
				.notSolid()
				);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(FACING, Direction.UP));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(FACING);
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
			doHarvestEffect(worldIn, player, pos, state.get(FACING), stack);
		}
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		if (!worldIn.isRemote()) {
			if (entityIn instanceof LivingEntity) {
				doEntityEffect(worldIn, (LivingEntity) entityIn, pos);
				worldIn.destroyBlock(pos, false);
			}
		}
	}
	
	protected void doHarvestEffect(World world, PlayerEntity player, BlockPos pos, Direction face, ItemStack stack) {
		HarvestUtil.WalkVein(world, pos.offset(face), (walkWorld, walkPos, depth, state) -> {
			//world.destroyBlock(walkPos, true);
			((ServerPlayerEntity) player).interactionManager.tryHarvestBlock(walkPos);
			return true;
		});
	}

	protected void doEntityEffect(World world, LivingEntity entity, BlockPos pos) {
		entity.addPotionEffect(new EffectInstance(NostrumEffects.lootLuck, 20 * 10));
		EvokerFangsEntity fangs = new EvokerFangsEntity(world, pos.getX() + .5, pos.getY(), pos.getZ() + .5, 0, 0, null);
		world.addEntity(fangs);
	}
	
}
