package com.smanzana.nostrummagica.block;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.effect.NostrumEffects;
import com.smanzana.nostrummagica.util.DimensionUtils;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

public class CursedIceBlock extends HalfTransparentBlock {

	public static final String ID = "cursed_ice";
	private static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	
	public CursedIceBlock() {
		super(Block.Properties.of(Material.ICE)
				.strength(3.0f)
				.friction(0.68F)
				.sound(SoundType.GLASS)
				.randomTicks()
				.noDrops()
				.noOcclusion()
				);
		//this.setLightOpacity(14);
		
		this.registerDefaultState(this.stateDefinition.any().setValue(LEVEL, 0));
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}
	
	/**
	 * Returns a state that's 0 decay but at the appropriate level
	 * @param level
	 * @return
	 */
	public BlockState getState(int level) {
		return defaultBlockState().setValue(LEVEL, Math.max(Math.min(2, level - 1), 0));
	}
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean skipRendering(BlockState state, BlockState adjacentBlockState, Direction side) {
		final Block adjacentBlock = adjacentBlockState.getBlock();
		
		return (Tags.Blocks.GLASS.contains(adjacentBlock) || Tags.Blocks.STAINED_GLASS.contains(adjacentBlock)
				|| BlockTags.ICE.contains(adjacentBlock) || adjacentBlock == this);
	}
	
	@Override
	public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
		int level = state.getValue(LEVEL);
		
		// Don't grow is in Sorcery dim
		if (DimensionUtils.IsSorceryDim(worldIn)) {
			return;
		}
		
		if (NostrumMagica.rand.nextFloat() <= 0.2f * (float) (level + 1)) {
			List<BlockPos> targets = Lists.newArrayList(pos.offset(1, 0, 0),
									pos.offset(0, 0, 1),
									pos.offset(-1, 0, 0),
									pos.offset(0, 0, -1),
									pos.offset(0, 1, 0),
									pos.offset(0, -1, 0));
			Collections.shuffle(targets);
			
			for (BlockPos target : targets)
			if (!worldIn.isEmptyBlock(target)) {
				BlockState bs = worldIn.getBlockState(target);
				Block b = bs.getBlock();
				if (!BlockTags.ICE.contains(b) && !(b == this)) {
					if (bs.getDestroySpeed(worldIn, target) >= 0.0f &&
							bs.getDestroySpeed(worldIn, target) <= Math.pow(2.0f, level)) {
						worldIn.setBlockAndUpdate(target, Blocks.ICE.defaultBlockState());
						return;
					}
					
				} else if (BlockTags.ICE.contains(b)) {
					// It's ice. Convert to cursed ice
					worldIn.setBlockAndUpdate(target, defaultBlockState());
					return;
				}
			}
		}
    }
	
	public void stepOn(Level worldIn, BlockPos pos, Entity entityIn) {
		
		if (!worldIn.isClientSide) {
			int amp = 0;
			if (worldIn.getBlockState(pos).getValue(LEVEL) == 2)
				amp = 1;
			
			if (entityIn instanceof LivingEntity && ((LivingEntity) entityIn).getEffect(NostrumEffects.magicResist) == null) {
				LivingEntity living = (LivingEntity) entityIn;
				living.addEffect(new MobEffectInstance(NostrumEffects.frostbite,
						45, amp));
			}
		}
		
		super.stepOn(worldIn, pos, entityIn);
    }

}
