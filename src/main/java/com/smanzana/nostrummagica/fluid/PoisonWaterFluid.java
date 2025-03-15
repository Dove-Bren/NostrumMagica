package com.smanzana.nostrummagica.fluid;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.block.NostrumBlocks;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;
import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.Item;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.FluidTags;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
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
	public void animateTick(World worldIn, BlockPos pos, FluidState state, Random rand) {
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
						new Vector3d(rand.nextFloat() * hMag - (hMag/2), rand.nextFloat() * vMag, rand.nextFloat() * hMag - (hMag/2)), null
						).color(color));
			}
		}
	}
	
	@Nullable
	@OnlyIn(Dist.CLIENT)
	public IParticleData getDripParticle() {
		return ParticleTypes.DRIPPING_WATER; // Would be cool to have custom one
	}
	
	@Override
	protected boolean canConvertToSource() {
		return !this.bUnbreakable;
	}
	
	@Override
	protected void beforeDestroyingBlock(IWorld worldIn, BlockPos pos, BlockState state) {
		TileEntity tileentity = state.hasTileEntity() ? worldIn.getBlockEntity(pos) : null;
		Block.dropResources(state, worldIn, pos, tileentity);
	}
	
	@Override
	public int getSlopeFindDistance(IWorldReader worldIn) {
		return 4; // Same as water
	}
	
	@Override
	public BlockState createLegacyBlock(FluidState state) {
		Block block = this.bUnbreakable ? NostrumBlocks.unbreakablePoisonWaterBlock : NostrumBlocks.poisonWaterBlock;
		return block.defaultBlockState().setValue(FlowingFluidBlock.LEVEL, Integer.valueOf(getLegacyLevel(state)));
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
	public int getDropOff(IWorldReader worldIn) {
		return 1;
	}
	
	@Override
	public int getTickDelay(IWorldReader p_205569_1_) {
		return 5; // Same as water?
	}
	
	@Override // no clue what this is
	public boolean canBeReplacedWith(FluidState p_215665_1_, IBlockReader p_215665_2_, BlockPos p_215665_3_, Fluid p_215665_4_, Direction p_215665_5_) {
		return p_215665_5_ == Direction.DOWN && !p_215665_4_.is(FluidTags.WATER);
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
		protected void createFluidStateDefinition(StateContainer.Builder<Fluid, FluidState> builder) {
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
