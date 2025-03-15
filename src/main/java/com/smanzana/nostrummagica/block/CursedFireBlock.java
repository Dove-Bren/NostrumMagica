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
	protected void createBlockStateDefinition(StateContainer.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(LEVEL);
	}
	
	@Override
	public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
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
	
	protected float getDirectDamage(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		return this.fireDamage + (state.getValue(LEVEL) * .5f);
	}
	
	@Override
	public void entityInside(BlockState state, World worldIn, BlockPos pos, Entity entityIn) {
		//super.onEntityCollision(state, worldIn, pos, entityIn);
		if (!entityIn.fireImmune()) {
			entityIn.setSecondsOnFire(8);
			
			entityIn.hurt(DamageSource.IN_FIRE, getDirectDamage(state, worldIn, pos, entityIn));
		}
			
		if (!worldIn.isClientSide()) {
			if (entityIn instanceof LivingEntity) {
				LivingEntity living = (LivingEntity) entityIn;
				@Nullable EffectInstance instance = living.getEffect(NostrumEffects.cursedFire);
				final int duration = 20 * 1200;
				if (instance == null || instance.getDuration() < (int) (duration * .8f)) {
					living.addEffect(new EffectInstance(
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
		return this.defaultBlockState();
	}
	
	@Override
	public BlockState updateShape(BlockState stateIn, Direction facing, BlockState facingState, IWorld worldIn, BlockPos currentPos, BlockPos facingPos) {
		return this.canSurvive(stateIn, worldIn, currentPos) ? stateIn : Blocks.AIR.defaultBlockState();
	}
	
	public BlockState GetWithLevel(int level) {
		return this.defaultBlockState().setValue(LEVEL, Math.max(0, Math.min(2, level)));
	}
	
	@Override
	public boolean canSurvive(BlockState state, IWorldReader worldIn, BlockPos pos) {
		return super.canSurvive(state, worldIn, pos) || worldIn.getBlockState(pos.below()).getBlock() == this;
	}

}
