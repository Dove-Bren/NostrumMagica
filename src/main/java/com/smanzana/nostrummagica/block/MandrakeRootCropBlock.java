package com.smanzana.nostrummagica.block;

import com.smanzana.nostrummagica.item.NostrumItems;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.item.Item;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public class MandrakeRootCropBlock extends CropBlock {

	private static final VoxelShape[] AABB = new VoxelShape[] {Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.125D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.1875D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.25D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.3125D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.375D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.4375D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8D, 16.0D), Block.box(0.0D, 0.0D, 0.0D, 16.0D, 16*0.5625D, 16.0D)};

	public static final String ID = "mandrake_crop";
	
	public MandrakeRootCropBlock() {
		super(Block.Properties.of(Material.PLANT)
				.noCollission().randomTicks().strength(0f).sound(SoundType.CROP));
	}
	
	@Override
	protected Item getBaseSeedId() {
		return NostrumItems.reagentSeedMandrake;
	}
	
	@Override
	public VoxelShape getShape(BlockState state, BlockGetter worldIn, BlockPos pos, CollisionContext context) {
		return AABB[((Integer)state.getValue(this.getAgeProperty())).intValue()];
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
