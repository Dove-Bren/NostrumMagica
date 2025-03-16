package com.smanzana.nostrummagica.block;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.fluid.MysticWaterFluid;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class MysticWaterBlock extends Block implements BucketPickup {

	public static final String ID = "mystic_water_block";
	public static final IntegerProperty LEVEL = BlockStateProperties.LEVEL;
	public static final IntegerProperty POWER = IntegerProperty.create("power", 0, 2);
	
	private final Supplier<? extends MysticWaterFluid> fluidSupplier;
	private MysticWaterFluid fluidCache = null;
	
	public MysticWaterBlock(Supplier<? extends MysticWaterFluid> supplier) {
		super(Block.Properties.of(Material.WATER)
				.noCollission().strength(100.0F).noDrops()
				);
		this.fluidSupplier = supplier;
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0).setValue(POWER, 0));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LEVEL, POWER);
	}
	
	public BlockState getStateWithPower(int power) {
		return this.defaultBlockState().setValue(POWER, Math.max(0, Math.min(2, power)));
	}
	
	protected MysticWaterFluid getFluid() {
		if (this.fluidCache == null) {
			this.fluidCache = fluidSupplier.get();
		}
		return fluidCache;
	}
	
	@Override
	public boolean isPathfindable(BlockState state, BlockGetter worldIn, BlockPos pos, PathComputationType type) {
		return true;
	}
	
	@Override
	public Fluid takeLiquid(LevelAccessor worldIn, BlockPos pos, BlockState state) {
		// Let the player pick it up regardless of level
		worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
		return this.getFluid();
	}
	
	@Override
	public boolean canBeReplaced(BlockState state, BlockPlaceContext useContext) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void entityInside(BlockState state, Level world, BlockPos pos, Entity entity) {
		// TODO check this works
		super.entityInside(state, world, pos, entity);
		if (entity instanceof AbstractArrow) {
			final Vec3 orig = entity.getDeltaMovement();
			final float scale = .2f;
			entity.setDeltaMovement(orig.x() * scale, orig.y(), orig.z() * scale);
			entity.hurtMarked = true;
		}
	}
	
	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}
	
	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return false;
	}
	
	@Override
	public FluidState getFluidState(BlockState state) {
		return this.getFluid().getFluidState(state);
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		return adjacentBlockState.getFluidState().getType().isSame(this.getFluid());
	}
	
	@Override
	public RenderShape getRenderShape(BlockState state) {
		return RenderShape.INVISIBLE;
	}
	
	@Override
	public void onPlace(BlockState state, Level worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getLiquidTicks().scheduleTick(pos, state.getFluidState().getType(), this.getFluid().getTickDelay(worldIn));
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		worldIn.getLiquidTicks().scheduleTick(currentPos, stateIn.getFluidState().getType(), this.getFluid().getTickDelay(worldIn));
		return stateIn;
	}
}
