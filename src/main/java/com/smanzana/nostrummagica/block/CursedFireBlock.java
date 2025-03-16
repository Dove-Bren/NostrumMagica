package com.smanzana.nostrummagica.block;

import java.util.Random;

import javax.annotation.Nullable;

import com.smanzana.nostrummagica.effect.NostrumEffects;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

public class CursedFireBlock extends FireBlock {
	
	public static final String ID = "cursed_fire";
	
	protected static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	
	protected final float fireDamage;

	public CursedFireBlock() {
		super(Block.Properties.of(Material.FIRE, MaterialColor.FIRE)
				.noCollission()
				.instabreak()
				.lightLevel((state) -> 15)
				.sound(SoundType.WOOL)
			);
		this.registerDefaultState(this.defaultBlockState().setValue(LEVEL, 0));
		fireDamage = 1.5f;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LEVEL);
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		if (!state.canSurvive(worldIn, pos)) {
			worldIn.removeBlock(pos, false);
			return;
		}
		worldIn.getBlockTicks().scheduleTick(pos, this, 30 + worldIn.random.nextInt(10));
		
		int age = state.getValue(AGE);
		
		// Add random age. Copied from base implementation.
		int j = Math.min(15, age + rand.nextInt(3) / 2);
		if (age != j) {
			state = state.setValue(AGE, Integer.valueOf(j));
			worldIn.setBlock(pos, state, 4);
			age = j;
		}
		
		if (age == 15 && rand.nextInt(4) == 0) {
			worldIn.removeBlock(pos, false);
			return;
		}
	}
	
	protected float getDirectDamage(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		return this.fireDamage + (state.getValue(LEVEL) * .5f);
	}
	
	@Override
	public void entityInside(BlockState state, Level worldIn, BlockPos pos, Entity entityIn) {
		//super.onEntityCollision(state, worldIn, pos, entityIn);
		if (!entityIn.fireImmune()) {
			entityIn.setSecondsOnFire(8);
			
			entityIn.hurt(DamageSource.IN_FIRE, getDirectDamage(state, worldIn, pos, entityIn));
		}
			
		if (!worldIn.isClientSide()) {
			if (entityIn instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) entityIn;
				@Nullable MobEffectInstance instance = living.getEffect(NostrumEffects.cursedFire);
				final int duration = 20 * 1200;
				if (instance == null || instance.getDuration() < (int) (duration * .8f)) {
					living.addEffect(new MobEffectInstance(
							NostrumEffects.cursedFire,
							duration,
							0
							));
				}
			}
		}
	}
	
	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return this.defaultBlockState();
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, LevelAccessor worldIn, BlockPos currentPos, BlockPos facingPos) {
		return this.canSurvive(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
	}
	
	public BlockState GetWithLevel(int level) {
		return this.defaultBlockState().setValue(LEVEL, Math.max(0, Math.min(2, level)));
	}
	
	@Override
	public boolean canSurvive(BlockState state, LevelReader worldIn, BlockPos pos) {
		return super.canSurvive(state, worldIn, pos) || worldIn.getBlockState(pos.below()).getBlock() == this;
	}

}
