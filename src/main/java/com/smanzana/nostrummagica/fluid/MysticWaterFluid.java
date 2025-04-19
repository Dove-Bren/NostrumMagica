package com.smanzana.nostrummagica.fluid;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.MysticWaterBlock;
import com.smanzana.nostrummagica.block.NostrumBlocks;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.tags.FluidTags;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;

public class MysticWaterFluid extends Fluid {

	public static final String ID = "mystic_water";
	public static final IntegerProperty LEVEL_1_8 = BlockStateProperties.LEVEL_FLOWING;
	public static final IntegerProperty POWER = MysticWaterBlock.POWER;
	
	protected final FluidAttributes.Builder attributesBuilder;
	
	private final Map<FluidState, VoxelShape> shapes;
	
	public MysticWaterFluid() {
		this(FluidAttributes.builder(
                NostrumMagica.Loc("block/" + ID),
                NostrumMagica.Loc("block/" + ID))
                .overlay(new ResourceLocation("minecraft:block/water_overlay"))
                .sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY)
                );
		this.registerDefaultState(this.defaultFluidState().setValue(LEVEL_1_8, 8).setValue(POWER, 0));
	}
	
	protected MysticWaterFluid(FluidAttributes.Builder builder) {
		super();
		this.attributesBuilder = builder;
		this.shapes = new HashMap<>();
	}
	
	@Override
	protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
		super.createFluidStateDefinition(builder);
		builder.add(LEVEL_1_8, POWER);
	}
	
	@Override
	protected FluidAttributes createAttributes() {
		return attributesBuilder.build(this);
	}
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
	@Override
	public ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_WATER;
	}
	
	@Override
	public Item getBucket() {
		return Items.WATER_BUCKET;
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(Level worldIn, BlockPos pos, FluidState state, Random rand) {
		super.animateTick(worldIn, pos, state, rand);
	}
	
	@Override
	public BlockState createLegacyBlock(FluidState state) {
		MysticWaterBlock block = NostrumBlocks.mysticWaterBlock;
		int level = (8 - Math.min(state.getAmount(), 8)) * 2;
		int power = state.getValue(POWER);
		return block.getStateWithPower(power).setValue(MysticWaterBlock.LEVEL, Integer.valueOf(level));
	}
	
	public FluidState getFluidState(BlockState state) {
		int blockLevel = state.getValue(MysticWaterBlock.LEVEL);
		int power = state.getValue(MysticWaterBlock.POWER);
		return getFluidStateForLevel(power, blockLevel);
	}
	
	protected FluidState getFluidStateForLevel(int power, int blockLevel) {
		// Block level is the inverse of fluid level, * 2.
		// BlockLevel = f(x) = (8 - FluidLevel) * 2;
		// FluidLevel = 8 - (BlockLevel / 2)
		int fluidLevel = 8 - (blockLevel / 2);
		return this.defaultFluidState().setValue(POWER, power).setValue(LEVEL_1_8, fluidLevel); 
	}
	
	@Override
	public boolean isSame(Fluid fluidIn) {
		return fluidIn == this;
	}
	
	@Override
	public int getTickDelay(LevelReader p_205569_1_) {
		return (20 * 2);
	}
	
	protected boolean shouldDry(Level worldIn, BlockPos pos, FluidState state) {
		// power 0 should dry up every tick
		// power 1 should dry up every OTHER tick
		// power 2 should never dry up
		final int power = state.getValue(POWER);
		if (power >= 2) {
			return false;
		} else if (power == 1) {
			// Figure out if it's the second tick by looking at worl tick time
			final int rate = getTickDelay(worldIn);
			return (worldIn.getGameTime() % (rate * 2) >= rate); 
		} else {
			return true;
		}
	}
	
	@Override
	public void tick(Level worldIn, BlockPos pos, FluidState state) {
		// Slowly dry up
		if (!blockAboveIsWater(state, worldIn, pos) && shouldDry(worldIn, pos, state)) {
			final int newLevel = this.getAmount(state) - 1;
			if (newLevel <= 0) {
				worldIn.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
				return;
			} else {
				worldIn.setBlock(pos, state.setValue(LEVEL_1_8, newLevel).createLegacyBlock(), 3);
			}
		}
		worldIn.scheduleTick(pos, this, this.getTickDelay(worldIn));
	}
	
	@Override // no clue what this is
	public boolean canBeReplacedWith(FluidState p_215665_1_, BlockGetter p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
		return false; // ?return p_215665_5_ == Direction.DOWN && !p_215665_4_.isIn(FluidTags.WATER);
	}
	
	@Override
	public int getAmount(FluidState state) {
		return state.getValue(LEVEL_1_8);
	}

	@Override
	protected float getExplosionResistance() {
		return 100.0f;
	}

	@Override
	protected Vec3 getFlow(BlockGetter blockReader, BlockPos pos, FluidState fluidState) {
		return Vec3.ZERO;
	}
	
	protected boolean blockAboveIsWater(FluidState state, BlockGetter blockReader, BlockPos pos) {
		return blockReader.getFluidState(pos.above()).is(FluidTags.WATER);
	}

	public float getHeight(FluidState state, BlockGetter blockReader, BlockPos pos) {
		return blockAboveIsWater(state, blockReader, pos) ? 1.0F : state.getOwnHeight();
	}

	public float getOwnHeight(FluidState state) {
		// Fluid renderer will show us at 10(/16)'s shorter if this is < .8f... No real way to render between
		// Instead of quickly going pancake and then slowly dying, spread more of the scale
		// up above .8f
		final int level = state.getAmount();
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

	public VoxelShape getShape(FluidState state, BlockGetter blockReader, BlockPos pos) {
		return state.getAmount() == 9 && blockAboveIsWater(state, blockReader, pos) ? Shapes.block() : this.shapes.computeIfAbsent(state, (stateIn) -> {
			return Shapes.box(0.0D, 0.0D, 0.0D, 1.0D, (double)stateIn.getHeight(blockReader, pos), 1.0D);
		});
	}
}
