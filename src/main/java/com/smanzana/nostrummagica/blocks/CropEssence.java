package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.items.NostrumItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.state.IProperty;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class CropEssence extends CropsBlock {

	public static final IntegerProperty AGE = IntegerProperty.create("age", 0, 3);
	private static final VoxelShape[] AABB = new VoxelShape[] {Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.165D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.275D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.7D, 16.0D), Block.makeCuboidShape(16.0 * 0.0D, 16.0 * 0.0D, 16.0 * 0.0D, 16.0D, 16.0 * 0.8D, 16.0D)};

	public static final String ID = "esscrop";
	
	public CropEssence() {
		super(Block.Properties.create(Material.PLANTS)
				.doesNotBlockMovement().tickRandomly().hardnessAndResistance(0f).sound(SoundType.CROP));
	}
	
	@Override
	public IntegerProperty getAgeProperty() {
		return AGE;
	}
	
	@Override
	protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
		builder.add(new IProperty[] {AGE});
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
	
//	protected ItemStack getRandomEssence(Random rand) {
//		EMagicElement elem = EMagicElement.values()[rand.nextInt(EMagicElement.values().length)];
//		return EssenceItem.getEssence(elem, 1);
//	}
//
//    @Override
//    public void getDrops(NonNullList<ItemStack> ret, IWorldReader world, BlockPos pos, BlockState state, int fortune) {
//        final int age = getAge(state);
//        Random rand = world instanceof World ? ((World)world).rand : NostrumMagica.rand;
//
//        int cropCount = 0;
//        if (age >= getMaxAge()) {
//        	cropCount = 1 + rand.nextInt(2) + fortune;
//        }
//        if (cropCount != 0) {
//        	for (int i = 0; i < cropCount; i++) {
//        		ret.add(getRandomEssence(rand));
//        	}
//        }
//        
//        int seedCount = 1;
//        if (age >= getMaxAge() && rand.nextBoolean() && rand.nextBoolean()) {
//        	seedCount += 1 + (fortune / 3);
//        }
//        
//        ret.add(getSeeds(seedCount));
//    }
}
