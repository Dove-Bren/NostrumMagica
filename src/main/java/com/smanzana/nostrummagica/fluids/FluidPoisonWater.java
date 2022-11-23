package com.smanzana.nostrummagica.fluids;

import java.util.Random;

import javax.annotation.Nonnull;

import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.client.particles.NostrumParticles;
import com.smanzana.nostrummagica.client.particles.NostrumParticles.SpawnParams;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fluids.BlockFluidClassic;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class FluidPoisonWater extends Fluid {

	public static final String ID = "poison_water";
	public static final String ID_UNBREAKABLE = ID + "_unbreakable";
	
	public FluidPoisonWater(boolean bUnbreakable) {
		super(bUnbreakable ? ID_UNBREAKABLE : ID,
				new ResourceLocation(NostrumMagica.MODID, "blocks/" + ID + "_still"), // same visually whether unbreakable or not
				new ResourceLocation(NostrumMagica.MODID, "blocks/" + ID + "_flowing"),
				new ResourceLocation(NostrumMagica.MODID, "blocks/" + ID + "_overlay"),
				0xFF1D452F);
		
		this.setGaseous(false);
	}
	
	public static class FluidPoisonWaterBlock extends BlockFluidClassic {
		
		public final boolean bUnbreakable;
		
		public FluidPoisonWaterBlock(FluidPoisonWater fluid, Material material, boolean bUnbreakable) {
			super(fluid, material, MapColor.GREEN_STAINED_HARDENED_CLAY);
			this.bUnbreakable = bUnbreakable;
			this.setUnlocalizedName(fluid.unlocalizedName + "_block");
			this.setRegistryName(new ResourceLocation(NostrumMagica.MODID, fluid.unlocalizedName + "_block"));
		}
		
		@Override
		public boolean canDrain(World world, BlockPos pos) {
			return !bUnbreakable;
		}
		
		@Override
		public boolean isReplaceable(IBlockAccess worldIn, BlockPos pos) {
			return !bUnbreakable;
		}
		
		@Override
		public Vec3d modifyAcceleration(World worldIn, BlockPos pos, Entity entityIn, Vec3d motion) {
			return super.modifyAcceleration(worldIn, pos, entityIn, motion);
		}
		
		@Override
		public Boolean isEntityInsideMaterial(@Nonnull IBlockAccess world, @Nonnull BlockPos blockpos, @Nonnull IBlockState iblockstate, @Nonnull Entity entity,
				double yToTest, @Nonnull Material materialIn, boolean testingHead) {
			if (materialIn == this.blockMaterial || materialIn == Material.WATER) {
				return Boolean.TRUE;
			}
			return super.isEntityInsideMaterial(world, blockpos, iblockstate, entity, yToTest, materialIn, testingHead);
		}
		
		@Override
		public void onEntityCollidedWithBlock(@Nonnull World world, @Nonnull BlockPos pos, @Nonnull IBlockState state, @Nonnull Entity entity) {
			if (!world.isRemote
					&& entity instanceof EntityLivingBase) {
				if (entity.ticksExisted % 10 == 0) {
					EntityLivingBase living = (EntityLivingBase) entity;
					living.attackEntityFrom(PoisonWaterDamageSource, .25f);
				}
			}
			
			super.onEntityCollidedWithBlock(world, pos, state, entity);
		}
		
		@SideOnly(Side.CLIENT)
		@Override
		public void randomDisplayTick(IBlockState stateIn, World worldIn, BlockPos pos, Random rand) {
			super.randomDisplayTick(stateIn, worldIn, pos, rand);
			
			if (worldIn.isAirBlock(pos.up())) {
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
							new Vec3d(rand.nextFloat() * hMag - (hMag/2), rand.nextFloat() * vMag, rand.nextFloat() * hMag - (hMag/2)), null
							).color(color));
				}
			}
		}
		
		public static final DamageSource PoisonWaterDamageSource = (new DamageSource("nostrum_poison_water")).setDamageBypassesArmor();
	}
	
}
