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
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
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
	public boolean isPathfindable(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return true;
	}
	
	@Override
	public Fluid takeLiquid(IWorld worldIn, BlockPos pos, BlockState state) {
		// Let the player pick it up regardless of level
		worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
		return this.getFluid();
	}
	
	@Override
	public boolean canBeReplaced(BlockState state, BlockItemUseContext useContext) {
		return true;
	}
	
	@SuppressWarnings("deprecation")
	@Override
	public void entityInside(BlockState state, World world, BlockPos pos, Entity entity) {
		// TODO check this works
		super.entityInside(state, world, pos, entity);
		if (entity instanceof AbstractArrowEntity) {
			final Vector3d orig = entity.getDeltaMovement();
			final float scale = .2f;
			entity.setDeltaMovement(orig.x() * scale, orig.y(), orig.z() * scale);
			entity.hurtMarked = true;
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
	public BlockRenderType getRenderShape(BlockState state) {
		return BlockRenderType.INVISIBLE;
	}
	
	@Override
	public void onPlace(BlockState state, World worldIn, BlockPos pos, BlockState oldState, boolean isMoving) {
		worldIn.getLiquidTicks().scheduleTick(pos, state.getFluidState().getType(), this.getFluid().getTickDelay(worldIn));
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		worldIn.getLiquidTicks().scheduleTick(currentPos, stateIn.getFluidState().getType(), this.getFluid().getTickDelay(worldIn));
		return stateIn;
	}
}
