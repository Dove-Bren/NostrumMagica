package com.smanzana.nostrummagica.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FireBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class CursedFireBlock extends FireBlock {
	
	public static final String ID = "cursed_fire";
	
	protected static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	
	protected final float fireDamage;

	public CursedFireBlock() {
		super(Block.Properties.create(Material.FIRE, MaterialColor.TNT)
				.doesNotBlockMovement()
				.zeroHardnessAndResistance()
				.setLightLevel((state) -> 15)
				.sound(SoundType.CLOTH)
			);
		this.setDefaultState(this.getDefaultState().with(LEVEL, 0));
		fireDamage = 1.5f;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		super.fillStateContainer(builder);
		builder.add(LEVEL);
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
		if (!state.isValidPosition(worldIn, pos)) {
			worldIn.removeBlock(pos, false);
			return;
		}
		worldIn.getPendingBlockTicks().scheduleTick(pos, this, 30 + worldIn.rand.nextInt(10));
		
		int age = state.get(AGE);
		
		// Add random age. Copied from base implementation.
		int j = Math.min(15, age + rand.nextInt(3) / 2);
		if (age != j) {
			state = state.with(AGE, Integer.valueOf(j));
			worldIn.setBlockState(pos, state, 4);
			age = j;
		}
		
		if (age == 15 && rand.nextInt(4) == 0) {
			worldIn.removeBlock(pos, false);
			return;
		}
	}
	
	protected float getDirectDamage(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		return this.fireDamage + (state.get(LEVEL) * .5f);
	}
	
	@Override
	public void onEntityCollision(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		//super.onEntityCollision(state, worldIn, pos, entityIn);
		if (!entityIn.isImmuneToFire()) {
			entityIn.setFire(8);
			
			entityIn.attackEntityFrom(DamageSource.IN_FIRE, getDirectDamage(state, worldIn, pos, entityIn));
		}
			
		if (!worldIn.isRemote()) {
			if (entityIn instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) entityIn;
				@Nullable EffectInstance instance = living.getActivePotionEffect(NostrumEffects.cursedFire);
				final int duration = 20 * 1200;
				if (instance == null || instance.getDuration() < (int) (duration * .8f)) {
					living.addPotionEffect(new EffectInstance(
							NostrumEffects.cursedFire,
							duration,
							0
							));
				}
			}
		}
	}
	
	@Override
	public BlockState getStateForPlacement(BlockItemUseContext context) {
		return this.getDefaultState();
	}
	
	@Override
	public BlockState updatePostPlacement(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return this.isValidPosition(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.getDefaultState();
	}
	
	public BlockState GetWithLevel(int level) {
		return this.getDefaultState().with(LEVEL, Math.max(0, Math.min(2, level)));
	}
	
	@Override
	public boolean isValidPosition(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return super.isValidPosition(state, worldIn, pos) || worldIn.getBlockState(pos.down()).getBlock() == this;
	}

}
