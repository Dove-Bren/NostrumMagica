package com.smanzana.nostrummagica.blocks;

import java.util.Collections;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.smanzana.nostrummagica.NostrumMagica;
import com.smanzana.nostrummagica.config.ModConfig;
import com.smanzana.nostrummagica.effects.NostrumEffects;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.BreakableBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.Tags;

public class CursedIce extends BreakableBlock {

	public static final String ID = "cursed_ice";
	private static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 2);
	
	public CursedIce() {
		super(Block.Properties.create(Material.ICE)
				.hardnessAndResistance(3.0f)
				.slipperiness(0.68F)
				.sound(SoundType.GLASS)
				.tickRandomly()
				.noDrops()
				);
		//this.setLightOpacity(14);
		
		this.setDefaultState(this.stateContainer.getBaseState().with(LEVEL, 0));
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(LEVEL);
	}
	
	/**
	 * Returns a state that's 0 decay but at the appropriate level
	 * @param level
	 * @return
	 */
	public BlockState getState(int level) {
		return getDefaultState().with(LEVEL, Math.max(Math.min(2, level - 1), 0));
	}
	
	@OnlyIn(Dist.CLIENT)
    public BlockRenderLayer getRenderLayer() {
        return BlockRenderLayer.TRANSLUCENT;
    }
	
	@Override
	@OnlyIn(Dist.CLIENT)
	public boolean isSideInvisible(BlockState state, BlockState adjacentBlockState, Direction side) {
		final Block adjacentBlock = adjacentBlockState.getBlock();
		
		return !(Tags.Blocks.GLASS.contains(adjacentBlock) || Tags.Blocks.STAINED_GLASS.contains(adjacentBlock)
				|| BlockTags.ICE.contains(adjacentBlock) || adjacentBlock == this);
	}
	
	@Override
	public void tick(BlockState state, World worldIn, BlockPos pos, Random rand) {
		int level = state.get(LEVEL);
		
		// Don't grow is in Sorcery dim
		if (worldIn.getDimension().getType().getId() == ModConfig.config.sorceryDimensionIndex()) {
			return;
		}
		
		if (NostrumMagica.rand.nextFloat() <= 0.2f * (float) (level + 1)) {
			List<BlockPos> targets = Lists.newArrayList(pos.add(1, 0, 0),
									pos.add(0, 0, 1),
									pos.add(-1, 0, 0),
									pos.add(0, 0, -1),
									pos.add(0, 1, 0),
									pos.add(0, -1, 0));
			Collections.shuffle(targets);
			
			for (BlockPos target : targets)
			if (!worldIn.isAirBlock(target)) {
				BlockState bs = worldIn.getBlockState(target);
				Block b = bs.getBlock();
				if (!BlockTags.ICE.contains(b) && !(b == this)) {
					if (bs.getBlockHardness(worldIn, target) >= 0.0f &&
							bs.getBlockHardness(worldIn, target) <= Math.pow(2.0f, level)) {
						worldIn.setBlockState(target, Blocks.ICE.getDefaultState());
						return;
					}
					
				} else if (BlockTags.ICE.contains(b)) {
					// It's ice. Convert to cursed ice
					worldIn.setBlockState(target, getDefaultState());
					return;
				}
			}
		}
    }
	
	public void onEntityWalk(World worldIn, BlockPos pos, Entity entityIn) {
		
		if (!worldIn.isRemote) {
			int amp = 0;
			if (worldIn.getBlockState(pos).get(LEVEL) == 2)
				amp = 1;
			
			if (entityIn instanceof LivingEntity && ((LivingEntity) entityIn).getActivePotionEffect(NostrumEffects.magicResist) == null) {
				LivingEntity living = (LivingEntity) entityIn;
				living.addPotionEffect(new EffectInstance(NostrumEffects.frostbite,
						45, amp));
			}
		}
		
		super.onEntityWalk(worldIn, pos, entityIn);
    }

}
