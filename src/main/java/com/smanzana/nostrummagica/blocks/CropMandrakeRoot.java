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

public class CropMandrakeRoot extends CropsBlock {

	private static final VoxelShape[] AABB = new VoxelShape[] {Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.125D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.1875D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.25D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.3125D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.375D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.4375D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8D, 16.0D), Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 16*0.5625D, 16.0D)};

	public static final String ID = "mandrake_crop";
	
	public CropMandrakeRoot() {
		super(Block.Properties.create(Material.PLANTS)
				.doesNotBlockMovement().tickRandomly().hardnessAndResistance(0f).sound(SoundType.CROP));
	}
	
	@Override
	protected Item getSeedsItem() {
		return NostrumItems.reagentSeedMandrake;
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
//        if (age >= getMaxAge()){
//        	cropCount = 2 + rand.nextInt(2) + fortune;
//        }
//        
//        if (cropCount != 0) {
//        	ret.add(getCrops(cropCount));
//        }
//        
//        int seedCount = 1;
//        if (age >= getMaxAge()) {
//        	seedCount += rand.nextInt(2) + fortune;
//        }
//        
//        ret.add(getSeeds(seedCount));
//        
//        if (NostrumMagica.aetheria.isEnabled()) {
//        	if (rand.nextInt(10) + fortune >= 9) {
//        		ret.add(NostrumMagica.aetheria.getResourceItem(AetherResourceType.FLOWER_MANDRAKE, 1));
//        	}
//        }
//    }

}
