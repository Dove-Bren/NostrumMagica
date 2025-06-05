package com.smanzana.nostrummagica.fluid;

import java.util.Random;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.fluids.FluidAttributes;
import net.minecraftforge.fluids.ForgeFlowingFluid;

public abstract class PureWaterFluid {
	
	public static final String ID = "pure_water";
	public static final String ID_FLOWING = ID + "_flowing";
	
	private static final ForgeFlowingFluid.Properties PROPS = new ForgeFlowingFluid.Properties(() -> NostrumFluids.pureWater, () -> NostrumFluids.pureWaterFlowing,
			FluidAttributes.builder(NostrumMagica.Loc("block/" + ID), NostrumMagica.Loc("block/" + ID_FLOWING))
				.color(0xCF39C0F9).density(15).luminosity(3).viscosity(2).overlay(new ResourceLocation("minecraft:block/water_overlay")).sound(SoundEvents.BUCKET_FILL, SoundEvents.BUCKET_EMPTY))
			.slopeFindDistance(4).levelDecreasePerBlock(1)
			.block(() -> NostrumBlocks.pureWater).bucket(() -> NostrumItems.pureWaterBucket)
			;

	public static class Source extends ForgeFlowingFluid.Source {

		protected Source(Properties properties) {
			super(properties);
		}
		
		public Source() {
			this(PROPS);
		}
		
		@Override
		public void animateTick(Level worldIn, BlockPos pos, FluidState state, Random rand) {
			super.animateTick(worldIn, pos, state, rand);
			
			if (worldIn.isEmptyBlock(pos.above())) {
				worldIn.addParticle(ParticleTypes.BUBBLE,
						pos.getX() + rand.nextFloat(), pos.getY() + .75f + rand.nextFloat() * .1f, pos.getZ() + rand.nextFloat(),
						0, rand.nextFloat() * .05, 0
						);
				
				if (rand.nextInt(200) == 0) {
					worldIn.playLocalSound((double)pos.getX(), (double)pos.getY(), (double)pos.getZ(), SoundEvents.BUBBLE_COLUMN_UPWARDS_AMBIENT, SoundSource.BLOCKS, 0.2F + rand.nextFloat() * 0.2F, 0.9F + rand.nextFloat() * 0.15F, false);
		         }
			}
		}
	}
	
	public static class Flowing extends ForgeFlowingFluid.Flowing {

		protected Flowing(Properties properties) {
			super(properties);
		}
		
		public Flowing() {
			this(PROPS);
		}
		
	}
	
}
