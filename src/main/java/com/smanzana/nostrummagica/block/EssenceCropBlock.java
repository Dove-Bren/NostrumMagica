package com.smanzana.nostrummagica.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootParameterSets;
import net.minecraft.loot.LootParameters;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class EssenceCropBlock extends CropsBlock {

	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
	private static final VoxelShape[] AABB = new VoxelShape[] {Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.165D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.8D, 16.0D)};

	public static final String ID = "esscrop";
	
	public EssenceCropBlock() {
		super(Block.Properties.create(Material.PLANTS)
				.doesNotBlockMovement().tickRandomly().hardnessAndResistance(0f).sound(SoundType.CROP));
	}
	
	@Override
	public IntegerProperty getAgeProperty() {
		return AGE;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
	
	@Override
	public int getMaxAge() {
		return 3;
	}
	
	@Override
	protected Item getSeedsItem() {
		return NostrumItems.reagentSeedEssence;
	}
	
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AABB[((Integer)state.get(this.getAgeProperty())).intValue()];
	}
	
	protected ItemStack getRandomEssence(Random rand) {
		EMagicElement elem = EMagicElement.getRandom(rand);
		return EssenceItem.getEssence(elem, 1);
	}

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		final LootContext context = builder.withParameter(LootParameters.BLOCK_STATE, state).build(LootParameterSets.BLOCK);
		final List<ItemStack> loot = new ArrayList<>();
        final int age = getAge(state);
        final int fortune;
		if (context.has(LootParameters.TOOL)) {
			fortune = EnchantmentHelper.getEnchantmentLevel(Enchantments.FORTUNE, context.get(LootParameters.TOOL));
		} else {
			 fortune = 0;
		}
        
        final Random rand = context.getRandom();

        int cropCount = 0;
        if (age >= getMaxAge()) {
        	cropCount = 1 + rand.nextInt(2) + fortune;
        }
        if (cropCount != 0) {
        	for (int i = 0; i < cropCount; i++) {
        		loot.add(getRandomEssence(rand));
        	}
        }
        
        int seedCount = 1;
        if (age >= getMaxAge() && rand.nextBoolean() && rand.nextBoolean() && rand.nextBoolean()) {
        	seedCount += 1 + (fortune / 3);
        }
        
        loot.add(new ItemStack(this.getSeedsItem(), seedCount));
        return loot;
    }
}
