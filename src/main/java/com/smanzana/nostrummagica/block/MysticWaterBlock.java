package com.smanzana.nostrummagica.block;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.fluid.MysticWaterFluid;

import net.minecraft.block.Block;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.IBucketPickupHandler;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MysticWaterBlock extends Block implements IBucketPickupHandler {

	public static final String ID = "mystic_water_block";
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL_0_15;
	public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 2);
	
	private final Supplier<? extends MysticWaterFluid> fluidSupplier;
	private MysticWaterFluid fluidCache = null;
	
	public MysticWaterBlock(Supplier<? extends MysticWaterFluid> supplier) {
		super(Block.Properties.create(Material.WATER)
				.doesNotBlockMovement().hardnessAndResistance(100.0F).noDrops()
				);
		this.fluidSupplier = supplier;
		this.setDefaultState(this.stateContainer.getBaseState().with(LEVEL, 0).with(POWER, 0));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(LEVEL, POWER);
	}
	
	public BlockState getStateWithPower(int power) {
		return this.getDefaultState().with(POWER, Math.max(0, Math.min(2, power)));
	}
	
	protected MysticWaterFluid getFluid() {
		if (this.fluidCache == null) {
			this.fluidCache = fluidSupplier.get();
		}
		return fluidCache;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
	}
	
	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
		// Let the player pick it up regardless of level
		worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
		return this.getFluid();
	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		// TODO check this works
		super.onEntityCollision(state, world, pos, entity);
		if (entity instanceof AbstractArrowEntity) {
			final Vector3d orig = entity.getMotion();
			final float scale = .2f;
			entity.setMotion(orig.getX() * scale, orig.getY(), orig.getZ() * scale);
			entity.velocityChanged = true;
		}
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return VoxelShapes.empty();
	}
	
	@Override
	public boolean ticksRandomly(BlockState state) {
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockState state) {
		return this.getFluid().getFluidState(state);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getFluidState().getFluid().isEquivalentTo(this.getFluid());
	}
	
	@Override
	public BlockRenderType getRenderType(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public void onBlockAdded(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getPendingFluidTicks().scheduleTick(pos, state.getFluidState().getFluid(), this.getFluid().getTickRate(worldIn));
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		worldIn.getPendingFluidTicks().scheduleTick(currentPos, stateIn.getFluidState().getFluid(), this.getFluid().getTickRate(worldIn));
		return stateIn;
	}
}
