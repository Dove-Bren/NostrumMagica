package com.smanzana.nostrummagica.fluid;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.item.Item;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class PoisonWaterFluid extends ForgeFlowingFluid {

	private static final String ID_BASE = "poison_water";
	
	public static final String ID_BREAKABLE = ID_BASE;
	public static final String ID_BREAKABLE_FLOWING = ID_BREAKABLE + "_flowing";
	public static final String ID_UNBREAKABLE = ID_BASE + "_unbreakable";
	public static final String ID_UNBREAKABLE_FLOWING = ID_UNBREAKABLE + "_flowing";
	
	public final boolean bUnbreakable;
	
	public PoisonWaterFluid(boolean bUnbreakable) {
		super(new ForgeFlowingFluid.Properties(() -> new Source(bUnbreakable), () -> new Flowing(bUnbreakable), FluidAttributes.builder(
				new ResourceLocation(NostrumMagica.MODID, "block/" + ID_BASE + "_still"), // same visually whether unbreakable or not
				new ResourceLocation(NostrumMagica.MODID, "block/" + ID_BASE + "_flowing")
				).overlay(new ResourceLocation(NostrumMagica.MODID, "block/" + ID_BASE + "_overlay"))
				.color(0xCF1D452F)
			));
		this.bUnbreakable = bUnbreakable;
	}
	
//	@Override
//	@OnlyIn(Dist.CLIENT)
//	public BlockRenderLayer getRenderLayer() {
//		return BlockRenderLayer.TRANSLUCENT;
//	}
	
	@Override
	public Item getBucket() {
		if (this.bUnbreakable) {
			return NostrumItems.unbreakablePoisonWaterBucket;
		} else {
			return NostrumItems.poisonWaterBucket;
		}
	}
	
	@OnlyIn(Dist.CLIENT)
	@Override
	public void animateTick(Level worldIn, BlockPos pos, FluidState state, Random rand) {
		super.animateTick(worldIn, pos, state, rand);
		
		if (worldIn.isEmptyBlock(pos.above())) {
			if (rand.nextBoolean() && rand.nextBoolean()
					&& rand.nextBoolean() && rand.nextBoolean()) {
				final float brightness = rand.nextFloat();
				final float alpha = rand.nextFloat();
				final int color = 0x40003005
						+ (((int) (alpha * 40f) & 0xFF) << 24)
						+ (((int) (brightness * 60f) & 0xFF) << 16)
						+ (((int) (brightness * 60f) & 0xFF) << 8)
						+ (((int) (brightness * 60f) & 0xFF) << 0);
				
				double hMag = .01;
				double vMag = .025;
				NostrumParticles.GLOW_ORB.spawn(worldIn, new SpawnParams(
						1,
						pos.getX() + .5, pos.getY() + 1.25, pos.getZ() + .5, .5, 30, 10,
						new Vec3(rand.nextFloat() * hMag - (hMag/2), rand.nextFloat() * vMag, rand.nextFloat() * hMag - (hMag/2)), null
						).color(color));
			}
		}
	}
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public ParticleOptions getDripParticle() {
		return ParticleTypes.DRIPPING_WATER; // Would be cool to have custom one
	}
	
	@Override
	protected boolean canConvertToSource() {
		return !this.bUnbreakable;
	}
	
	@Override
	protected void beforeDestroyingBlock(LevelAccessor worldIn, BlockPos pos, BlockState state) {
		BlockEntity tileentity = state.hasBlockEntity() ? worldIn.getBlockEntity(pos) : null;
		Block.dropResources(state, worldIn, pos, tileentity);
	}
	
	@Override
	public int getSlopeFindDistance(LevelReader worldIn) {
		return 4; // Same as water
	}
	
	@Override
	public BlockState createLegacyBlock(FluidState state) {
		Block block = this.bUnbreakable ? NostrumBlocks.unbreakablePoisonWaterBlock : NostrumBlocks.poisonWaterBlock;
		return block.defaultBlockState().setValue(LiquidBlock.LEVEL, Integer.valueOf(getLegacyLevel(state)));
	}
	
	@Override
	public boolean isSame(Fluid fluidIn) {
		if (this.bUnbreakable) {
			return fluidIn == NostrumFluids.unbreakablePoisonWater || fluidIn == NostrumFluids.unbreakablePoisonWaterFlowing;
		} else {
			return fluidIn == NostrumFluids.poisonWater || fluidIn == NostrumFluids.poisonWaterFlowing;
		}
	}
	
	@Override
	public int getDropOff(LevelReader worldIn) {
		return 1;
	}
	
	@Override
	public int getTickDelay(LevelReader p_205569_1_) {
		return 5; // Same as water?
	}
	
	@Override // no clue what this is
	public boolean canBeReplacedWith(FluidState p_215665_1_, BlockGetter p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
		return p_215665_5_ == Direction.DOWN && !p_215665_1_.is(FluidTags.WATER);
	}

	@Override
	protected float getExplosionResistance() {
		return this.bUnbreakable ? 3600000.8F : 100.0f;
	}
	
	@Override
	public Fluid getFlowing() {
		if (this.bUnbreakable) {
			return NostrumFluids.unbreakablePoisonWaterFlowing;
		} else {
			return NostrumFluids.poisonWaterFlowing;
		}
	}

	@Override
	public Fluid getSource() {
		if (this.bUnbreakable) {
			return NostrumFluids.unbreakablePoisonWater;
		} else {
			return NostrumFluids.poisonWater;
		}
	}
	
	public static final DamageSource PoisonWaterDamageSource = (new DamageSource("nostrum_poison_water")).bypassArmor();
	
	public static class Source extends PoisonWaterFluid {
		
		public Source(boolean unbreakable) {
			super(unbreakable);
		}

		@Override
		public int getAmount(FluidState p_207192_1_) {
			return 8;
		}

		@Override
		public boolean isSource(FluidState state) {
			return true;
		}
	}
	
	public static class Flowing extends PoisonWaterFluid {
		
		public Flowing(boolean unbreakable) {
			super(unbreakable);
		}
		
		@Override
		protected void createFluidStateDefinition(StateDefinition.Builder<Fluid, FluidState> builder) {
			super.createFluidStateDefinition(builder);
			builder.add(FlowingFluid.LEVEL);
		}

		@Override
		public int getAmount(FluidState p_207192_1_) {
			return p_207192_1_.getValue(FlowingFluid.LEVEL);
		}

		@Override
		public boolean isSource(FluidState state) {
			return false;
		}
	}
	
}
