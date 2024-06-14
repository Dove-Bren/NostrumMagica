package com.smanzana.nostrummagica.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.MysticWaterBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;

public class FluidMysticWater extends Fluid {

	public static final String ID = "mystic_water";
	public static final IntegerProperty LEVEL_1_8 = BlockStateProperties.LEVEL_1_8;
	public static final IntegerProperty POWER = MysticWaterBlock.POWER;
	
	protected final FluidAttributes.Builder attributesBuilder;
	
	private final Map<FluidState, VoxelShape> shapes;
	
	public FluidMysticWater() {
		this(FluidAttributes.builder(
                NostrumMagica.Loc("block/" + ID),
                NostrumMagica.Loc("block/" + ID))
                .overlay(new ResourceLocation("minecraft:block/water_overlay"))
                .sound(SoundEvents.ITEM_BUCKET_FILL, SoundEvents.ITEM_BUCKET_EMPTY)
                );
		this.setDefaultState(this.getDefaultState().with(LEVEL_1_8, 8).with(POWER, 0));
	}
	
	protected FluidMysticWater(FluidAttributes.Builder builder) {
		super();
		this.attributesBuilder = builder;
		this.shapes = new HashMap<>();
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Fluid, FluidState> builder) {
		super.fillStateContainer(builder);
		builder.add(LEVEL_1_8, POWER);
	}
	
	@Override
	protected FluidAttributes createAttributes() {
		return attributesBuilder.build(this);
	}
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
	@Override
	public IParticleData getDripParticleData() {
		return ParticleTypes.DRIPPING_WATER;
	}
	
	@Override
	public Item getFilledBucket() {
		return Items.WATER_BUCKET;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(World worldIn, BlockPos pos, FluidState state, Random rand) {
		super.animateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public BlockState getBlockState(FluidState state) {
		MysticWaterBlock block = NostrumBlocks.mysticWaterBlock;
		int level = (8 - Math.min(state.getLevel(), 8)) * 2;
		int power = state.get(POWER);
		return block.getStateWithPower(power).with(MysticWaterBlock.LEVEL, Integer.valueOf(level));
	}
	
	public FluidState getFluidState(BlockState state) {
		int blockLevel = state.get(MysticWaterBlock.LEVEL);
		int power = state.get(MysticWaterBlock.POWER);
		return getFluidStateForLevel(power, blockLevel);
	}
	
	protected FluidState getFluidStateForLevel(int power, int blockLevel) {
		// Block level is the inverse of fluid level, * 2.
		// BlockLevel = f(x) = (8 - FluidLevel) * 2;
		// FluidLevel = 8 - (BlockLevel / 2)
		int fluidLevel = 8 - (blockLevel / 2);
		return this.getDefaultState().with(POWER, power).with(LEVEL_1_8, fluidLevel); 
	}
	
	@Override
	public boolean isEquivalentTo(Fluid fluidIn) {
		return fluidIn == this;
	}
	
	@Override
	public int getTickRate(IWorldReader p_205569_1_) {
		return (20 * 2);
	}
	
	protected boolean shouldDry(World worldIn, BlockPos pos, FluidState state) {
		// power 0 should dry up every tick
		// power 1 should dry up every OTHER tick
		// power 2 should never dry up
		final int power = state.get(POWER);
		if (power >= 2) {
			return false;
		} else if (power == 1) {
			// Figure out if it's the second tick by looking at worl tick time
			final int rate = getTickRate(worldIn);
			return (worldIn.getGameTime() % (rate * 2) >= rate); 
		} else {
			return true;
		}
	}
	
	@Override
	public void tick(World worldIn, BlockPos pos, FluidState state) {
		// Slowly dry up
		if (!blockAboveIsWater(state, worldIn, pos) && shouldDry(worldIn, pos, state)) {
			final int newLevel = this.getLevel(state) - 1;
			if (newLevel <= 0) {
				worldIn.setBlockState(pos, Blocks.AIR.getDefaultState(), 11);
				return;
			} else {
				worldIn.setBlockState(pos, state.with(LEVEL_1_8, newLevel).getBlockState(), 3);
			}
		}
		worldIn.getPendingFluidTicks().scheduleTick(pos, this, this.getTickRate(worldIn));
	}
	
	@Override // no clue what this is
	public boolean canDisplace(FluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
		return false; // ?return p_215665_5_ == Direction.DOWN && !p_215665_4_.isIn(FluidTags.WATER);
	}
	
	@Override
	public int getLevel(FluidState state) {
		return state.get(LEVEL_1_8);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0f;
	}

	@Override
	protected Vector3d getFlow(IBlockReader blockReader, BlockPos pos, FluidState fluidState) {
		return Vector3d.ZERO;
	}
	
	protected boolean blockAboveIsWater(FluidState state, IBlockReader blockReader, BlockPos pos) {
		return blockReader.getFluidState(pos.up()).isTagged(FluidTags.WATER);
	}

	public float getActualHeight(FluidState state, IBlockReader blockReader, BlockPos pos) {
		return blockAboveIsWater(state, blockReader, pos) ? 1.0F : state.getHeight();
	}

	public float getHeight(FluidState state) {
		// Fluid renderer will show us at 10(/16)'s shorter if this is < .8f... No real way to render between
		// Instead of quickly going pancake and then slowly dying, spread more of the scale
		// up above .8f
		final int level = state.getLevel();
		if (level == 4) {
			return .8f;
		}
		else if (level > 4) {
			return (float)(level + 10) / 18.0F;
		} else {
			return (float)level / 8.0F;
		}
	}

	@Override
	public boolean isSource(FluidState state) {
		return true;
	}

	public VoxelShape func_215664_b(FluidState state, IBlockReader blockReader, BlockPos pos) {
		return state.getLevel() == 9 && blockAboveIsWater(state, blockReader, pos) ? VoxelShapes.fullCube() : this.shapes.computeIfAbsent(state, (stateIn) -> {
			return VoxelShapes.create(0.0D, 0.0D, 0.0D, 1.0D, (double)stateIn.getActualHeight(blockReader, pos), 1.0D);
		});
	}
}
