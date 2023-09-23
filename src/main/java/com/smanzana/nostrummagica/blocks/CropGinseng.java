package com.smanzana.nostrummagica.blocks;

import com.smanzana.nostrummagica.items.NostrumItems;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.item.Item;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;

public class CropGinseng extends CropsBlock {

	private static final VoxelShape[] AABB = new VoxelShape[] {Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.165D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.275D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.7D, 1.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 1.0D, 0.8D, 1.0D)};

	public static final String ID = "ginseng_crop";
	
	public CropGinseng() {
		super(Block.Properties.create(Material.PLANTS)
				.doesNotBlockMovement().tickRandomly().hardnessAndResistance(0f).sound(SoundType.CROP));
	}
	
	@Override
	protected Item getSeedsItem() {
		return NostrumItems.reagentSeedGinseng;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
		return AABB[((Integer)state.get(this.getAgeProperty())).intValue()];
	}

//    @Override
//    public void getDrops(NonNullList<ItemStack> ret, IBlockAccess world, BlockPos pos, BlockState state, int fortune) {
//        int age = getAge(state);
//        Random rand = world instanceof World ? ((World)world).rand : new Random();
//
//        int cropCount = 0;
//        if (age >= getMaxAge()) {
//        	cropCount = 3 + rand.nextInt(2) + fortune;
//        }
//        if (cropCount != 0) {
//        	ret.add(getCrops(cropCount));
//        }
//        
//        int seedCount = 1;
//        if (age >= getMaxAge()) {
//        	seedCount += (rand.nextBoolean() ? rand.nextInt(2) : 0) + fortune;
//        }
//        
//        ret.add(getSeeds(seedCount));
//        
//        if (NostrumMagica.aetheria.isEnabled()) {
//        	if (rand.nextInt(10) + fortune >= 9) {
//        		ret.add(NostrumMagica.aetheria.getResourceItem(AetherResourceType.FLOWER_GINSENG, 1));
//        	}
//        }
//    }
}
