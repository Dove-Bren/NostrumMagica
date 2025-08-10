package com.smanzana.nostrummagica.block;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.smanzana.nostrummagica.item.EssenceItem;
import com.smanzana.nostrummagica.item.NostrumItems;
import com.smanzana.nostrummagica.spell.EMagicElement;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EssenceCropBlock extends CropBlock {

	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
	private static final VoxelShape[] AABB = new VoxelShape[] {Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.165D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.box(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.8D, 16.0D)};

	public static final String ID = "esscrop";
	
	public EssenceCropBlock() {
		super(Block.Properties.of(Material.PLANT)
				.noCollission().randomTicks().strength(0f).sound(SoundType.CROP));
	}
	
	@Override
	public IntegerProperty getAgeProperty() {
		return AGE;
	}
	
	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(AGE);
	}
	
	@Override
	public int getMaxAge() {
		return 3;
	}
	
	@Override
	public Item getBaseSeedId() {
		return NostrumItems.reagentSeedEssence;
	}
	
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AABB[((Integer)state.getValue(this.getAgeProperty())).intValue()];
	}
	
	protected ItemStack getRandomEssence(Random rand) {
		EMagicElement elem = EMagicElement.getRandom(rand);
		return EssenceItem.getEssence(elem, 1);
	}

    @Override
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
		final LootContext context = builder.withParameter(LootContextParams.BLOCK_STATE, state).create(LootContextParamSets.BLOCK);
		final List<ItemStack> loot = new ArrayList<>();
        final int age = getAge(state);
        final int fortune;
		if (context.hasParam(LootContextParams.TOOL)) {
			fortune = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.BLOCK_FORTUNE, context.getParamOrNull(LootContextParams.TOOL));
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
        
        loot.add(new ItemStack(this.getBaseSeedId(), seedCount));
        return loot;
    }
}
