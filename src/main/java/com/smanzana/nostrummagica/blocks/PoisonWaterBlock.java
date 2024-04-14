package com.smanzana.nostrummagica.blocks;

import java.util.function.Supplier;

import com.smanzana.nostrummagica.fluids.FluidPoisonWater;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.FlowingFluidBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.FlowingFluid;
import net.minecraft.fluid.Fluid;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.pathfinding.PathType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;

public class PoisonWaterBlock extends FlowingFluidBlock {

	public static final String ID_BREAKABLE = "poison_water_block";
	public static final String ID_UNBREAKABLE = "poison_water_unbreakable_block";
	
	private final boolean unbreakable;

	public PoisonWaterBlock(Supplier<? extends FlowingFluid> supplier, boolean unbreakable) {
		super(supplier, Block.Properties.create(Material.WATER)
				.doesNotBlockMovement().hardnessAndResistance(unbreakable ? 3600000.8F : 100.0F).noDrops()
				);
		
		this.unbreakable = unbreakable;
	}
	
	@Override
	public boolean allowsMovement(BlockState state, IBlockReader worldIn, BlockPos pos, PathType type) {
		return false;
	}
	
	@Override
	public Fluid pickupFluid(IWorld worldIn, BlockPos pos, BlockState state) {
		// want to do this but we don't know the player
//		final boolean allowed;
//		if (this.unbreakable) {
//			allowed = false;
//		} else {
//			// always allow
//			allowed = true;
//		}
		
		final boolean allowed = !this.unbreakable;
		
		if (allowed) {
			return this.getFluid();
		} else {
			return Fluids.EMPTY;
		}
	}
	
	@Override
	public boolean isReplaceable(BlockState state, BlockItemUseContext useContext) {
		return !unbreakable;
	}
	
	@Override
	public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
		if (!world.isRemote
				&& entity instanceof LivingEntity) {
			if (entity.ticksExisted % 10 == 0) {
				LivingEntity living = (LivingEntity) entity;
				living.attackEntityFrom(FluidPoisonWater.PoisonWaterDamageSource, .25f);
			}
		}
		
		// TODO check this works
		super.onEntityCollision(state, world, pos, entity);
	}
}
